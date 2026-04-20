package natte.re_search.client;

import natte.re_search.RegexSearch;
import natte.re_search.network.ItemSearchResultS2CPayload;
import natte.re_search.render.WorldRendering;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.mojang.brigadier.Command;

import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = RegexSearch.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientModBusEvents {

    public static KeyMapping SEARCH_KEY;

    private ClientModBusEvents() {
    }

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(RegexSearch.MOD_ID)
                .playToClient(ItemSearchResultS2CPayload.TYPE, ItemSearchResultS2CPayload.STREAM_CODEC,
                        ClientModBusEvents::handleSearchResult);
    }

    private static void handleSearchResult(ItemSearchResultS2CPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> WorldRendering.setMarkedInventories(payload.inventories()));
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        SEARCH_KEY = new KeyMapping("key.re_search.search", GLFW.GLFW_KEY_Y, "category.re_search.keybinds");
        event.register(SEARCH_KEY);
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                net.minecraft.commands.Commands.literal(RegexSearch.MOD_ID + "_client")
                        .then(net.minecraft.commands.Commands.literal("set_highlighter")
                                .then(net.minecraft.commands.Commands.literal("old").executes(ctx -> {
                                    natte.re_search.config.Config.isOldHighlighter = true;
                                    return Command.SINGLE_SUCCESS;
                                }))
                                .then(net.minecraft.commands.Commands.literal("default").executes(ctx -> {
                                    natte.re_search.config.Config.isOldHighlighter = false;
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }
}
