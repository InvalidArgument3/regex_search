package net.natte.regex_search.search.context;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.natte.regex_search.search.Localization;
import net.natte.regex_search.search.SearchOptions;

import java.util.function.Function;


public class Context {

    private final ServerPlayer player;
    private final TooltipFlag tooltipFlag;
    private final Function<Component, String> translator;
    private final Item.TooltipContext tooltipContext;

    private Context(SearchOptions searchOptions, ServerPlayer player) {
        this.player = player;
        this.tooltipFlag = searchOptions.hasAdvancedTooltips() ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
        this.tooltipContext = Item.TooltipContext.of(player.level());
        this.translator = Localization.getTranslator(searchOptions.languageCode());
    }

    public static Context create(SearchOptions searchOptions, ServerPlayer player) {
        return new Context(searchOptions, player);
    }

    public String translate(Component component) {
        return translator.apply(component);
    }

    public ServerPlayer player() {
        return player;
    }

    public TooltipFlag tooltipFlag() {
        return this.tooltipFlag;
    }

    public Item.TooltipContext tooltipContext() {
        return tooltipContext;
    }
}
