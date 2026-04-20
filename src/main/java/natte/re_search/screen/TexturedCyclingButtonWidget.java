package natte.re_search.screen;

import java.time.Duration;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class TexturedCyclingButtonWidget<T extends CycleableOption> extends AbstractButton {

    public T state;

    private static final int textureWidth = 256;
    private static final int textureHeight = 256;

    private final int hoveredVOffset;
    private final ResourceLocation texture;
    private final Consumer<TexturedCyclingButtonWidget<T>> pressAction;

    @SuppressWarnings("unchecked")
    public TexturedCyclingButtonWidget(T state, int x, int y, int width, int height, int hoveredVOffset,
            ResourceLocation texture, Consumer<TexturedCyclingButtonWidget<T>> pressAction) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.state = state;
        this.hoveredVOffset = hoveredVOffset;
        this.texture = texture;
        this.pressAction = pressAction;
        this.refreshTooltip();
        this.setTooltipDelay(Duration.ofMillis(700));
    }

    @Override
    public void onPress() {
        pressAction.accept(this);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int v = state.vOffset();
        if (this.isHovered()) {
            v += hoveredVOffset;
        }
        guiGraphics.blit(texture, getX(), getY(), state.uOffset(), v, width, height, textureWidth, textureHeight);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        this.defaultButtonNarrationText(narration);
    }

    public void refreshTooltip() {
        Tooltip tip = state.getTooltip();
        if (tip != null) {
            this.setTooltip(tip);
        }
    }
}
