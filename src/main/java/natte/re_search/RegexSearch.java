package natte.re_search;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import natte.re_search.config.Config;
import natte.re_search.network.ItemSearchC2SPayload;
import natte.re_search.network.ItemSearchResultS2CPayload;
import natte.re_search.search.MarkedInventory;
import natte.re_search.search.SearchOptions;
import natte.re_search.search.Searcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Mod(RegexSearch.MOD_ID)
public class RegexSearch {

    public static final String MOD_ID = "re_search";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public RegexSearch(IEventBus modBus) {
        Config.init(Config.class);

        modBus.addListener(RegexSearch::registerPayloads);
        NeoForge.EVENT_BUS.addListener(RegexSearch::registerCommands);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(MOD_ID);
        registrar.playToServer(ItemSearchC2SPayload.TYPE, ItemSearchC2SPayload.STREAM_CODEC, RegexSearch::handleItemSearchC2S);
    }

    private static void handleItemSearchC2S(ItemSearchC2SPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            SearchOptions searchOptions = payload.options();
            List<MarkedInventory> inventories = Searcher.search(searchOptions, player);
            player.connection.send(new ClientboundCustomPayloadPacket(new ItemSearchResultS2CPayload(inventories)));
            if (inventories.isEmpty()) {
                player.displayClientMessage(Component.translatable("popup.re_search.no_matching_items_found"), true);
            }
        });
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal(MOD_ID)
                        .then(reloadConfigCommand("reload"))
                        .then(showConfigCommand("info")));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> reloadConfigCommand(String command) {
        return Commands.literal(command).requires(source -> source.hasPermission(2))
                .executes(context -> {
                    Config.read();
                    context.getSource().sendSuccess(
                            () -> Component.translatableWithFallback("config.re_search.reloaded", "Reloaded config"),
                            true);
                    return Command.SINGLE_SUCCESS;
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> showConfigCommand(String command) {
        return Commands.literal(command).executes(RegexSearch::showConfig);
    }

    private static int showConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        List<String> lines = new ArrayList<>();

        for (Field field : Config.class.getFields()) {
            String value = "(null)";
            try {
                value = field.get(null).toString();
            } catch (Exception e) {
                // ignore
            }
            if (!field.getName().equals("configClass")) {
                lines.add(field.getName() + ": " + value);
            }
        }

        source.sendSuccess(() -> Component.literal(String.join("\n", lines)), false);

        return Command.SINGLE_SUCCESS;
    }
}
