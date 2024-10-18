package net.natte.re_search.search.context;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.natte.re_search.search.SearchOptions;
import org.apache.commons.lang3.NotImplementedException;


public class Context {

    private final ServerPlayer player;
    private final TooltipFlag tooltipFlag;

    private Context(ServerPlayer player, SearchOptions searchOptions) {
        this.player = player;
        this.tooltipFlag = searchOptions.hasAdvancedTooltips() ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
    }

    public static Context create(ServerPlayer player, SearchOptions searchOptions) {
        return new Context(player, searchOptions);
    }

    public String translate(Component component) {
        throw new NotImplementedException();
    }

    public Level level() {
        throw new NotImplementedException();
    }

    public ServerPlayer player() {
        return player;
    }

    public TooltipFlag tooltipFlag() {
        return this.tooltipFlag;
    }
}
