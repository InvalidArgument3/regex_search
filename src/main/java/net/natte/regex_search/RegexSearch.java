package net.natte.regex_search;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.natte.regex_search.config.Config;
import net.natte.regex_search.network.ItemSearchPacketC2S;
import net.natte.regex_search.network.ItemSearchResultPacketS2C;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


@Mod(RegexSearch.MOD_ID)
public class RegexSearch {

    public static final String MOD_ID = "regex_search";

    public RegexSearch(IEventBus modBus, ModContainer modContainer) {

        modBus.addListener(this::registerPackets);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        modBus.addListener(Config::onLoad);
    }

    private void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);
        registrar.playToServer(ItemSearchPacketC2S.TYPE, ItemSearchPacketC2S.STREAM_CODEC, ItemSearchPacketC2S::receive);
        registrar.playToClient(ItemSearchResultPacketS2C.TYPE, ItemSearchResultPacketS2C.STREAM_CODEC, ItemSearchResultPacketS2C::receive);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal(MOD_ID)
                        .then(Commands.literal("info")
                                .executes(RegexSearch::showConfig)));
    }

    private static int showConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        List<String> lines = new ArrayList<String>();

        for (Field field : Config.class.getFields()) {
            String value = "(null)";
            try {
                value = field.get(null).toString();
            } catch (Exception ignored) {
            }
            if (!field.getName().equals("configClass"))
                lines.add(field.getName() + ": " + value);
        }

        source.sendSystemMessage(Component.literal(String.join("\n", lines)));

        return Command.SINGLE_SUCCESS;
    }


    public static ResourceLocation ID(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}