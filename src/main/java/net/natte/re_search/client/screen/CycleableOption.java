package net.natte.re_search.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

public interface CycleableOption<E extends Enum<E>> {

    Component getName();

    Component getInfo();

    int uOffset();

    int vOffset();

    E next();

    E value();

    CycleableOption<E> withState(E state);

    default Tooltip getTooltip() {
        return Tooltip.create(getName().copy().append(Component.empty().append("\n").append(getInfo()).withStyle(ChatFormatting.DARK_GRAY)));
    }

    static <E extends Enum<E>> CycleableOption<E> create(String category, Class<E> enumClass, int vOffset) {
        return new OptionWithState<E>(category, enumClass, 20, 20, 0, vOffset);
    }

    class OptionWithState<E extends Enum<E>> implements CycleableOption<E> {

        String category;
        Class<E> enumClass;
        String[] values;
        int width;
        int height;
        int uOffset;
        int vOffset;

        int state;

        OptionWithState(String category, Class<E> enumClass, int width, int height, int uOffset, int vOffset) {

            this.category = category;
            this.enumClass = enumClass;
            this.width = width;
            this.height = height;
            this.uOffset = uOffset;
            this.vOffset = vOffset;

            this.values = Arrays.stream(enumClass.getEnumConstants()).map(e -> e.name().toLowerCase()).toArray(String[]::new);
            this.state = 0;
        }

        @Override
        public Component getName() {
            // TODO: cache/precompute
            return Component.translatable("option.re_search." + category + "." + values[state]);
        }

        @Override
        public Component getInfo() {
            // TODO: cache/precompute
            return Component.translatable("description.re_search." + category + "." + values[state]);
        }

        @Override
        public int uOffset() {
            return this.uOffset + this.width * state;
        }

        @Override
        public int vOffset() {
            return this.vOffset;
        }

        @Override
        public E next() {
            this.state = (this.state + 1) % this.values.length;
            return value();
        }

        @Override
        public E value() {
            return enumClass.getEnumConstants()[state];
        }

        @Override
        public CycleableOption<E> withState(E state) {
            this.state = state.ordinal();
            return this;
        }
    }
}
