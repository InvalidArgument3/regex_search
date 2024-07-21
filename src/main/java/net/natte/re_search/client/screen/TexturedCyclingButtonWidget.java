package net.natte.re_search.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.time.Duration;
import java.util.function.Consumer;


@OnlyIn(Dist.CLIENT)
public class TexturedCyclingButtonWidget<E extends Enum<E>> extends Button {

    public CycleableOption<E> state;

    private final ResourceLocation texture;

    @SuppressWarnings("unchecked") // cast to TexturedCyclingButtonWidget<T> line 27, 
    public TexturedCyclingButtonWidget(CycleableOption<E> state, int x, int y, int width, int height, ResourceLocation texture,
                                       Consumer<TexturedCyclingButtonWidget<E>> pressAction) {
        super(x, y, width, height, CommonComponents.EMPTY, button -> pressAction.accept((TexturedCyclingButtonWidget<E>) button), DEFAULT_NARRATION);
        this.texture = texture;
        this.state = state;
        this.refreshTooltip();
        this.setTooltipDelay(Duration.ofMillis(700));
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.blit(this.texture, this.getX(), this.getY(), this.state.uOffset(), this.state.vOffset() + (this.isHoveredOrFocused() ? this.height : 0),
                this.width, this.height);
    }

    public void refreshTooltip() {
        this.setTooltip(state.getTooltip());
    }

}
