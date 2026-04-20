package natte.re_search.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public interface CycleableOption {

    Component getName();

    Component getInfo();

    int uOffset();

    int vOffset();

    default Tooltip getTooltip() {
        return Tooltip.create(
                getName().copy().append(Component.literal("\n")).append(getInfo().copy().withStyle(ChatFormatting.DARK_GRAY)));
    }
}
