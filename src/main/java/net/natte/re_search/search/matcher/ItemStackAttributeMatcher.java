package net.natte.re_search.search.matcher;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.natte.re_search.query.Attribute;
import net.natte.re_search.search.context.Context;

import java.util.stream.Collectors;

public class ItemStackAttributeMatcher implements ItemStackPredicate {

    private final Context context;
    private final StringMatcher stringMatcher;
    private final ItemStackPredicate attributeMatcher;

    public ItemStackAttributeMatcher(Context context, Attribute attribute, StringMatcher stringMatcher) {
        this.context = context;
        this.stringMatcher = stringMatcher;
        this.attributeMatcher = matcherOf(attribute);
    }

    private ItemStackPredicate matcherOf(Attribute attribute) {
        return switch (attribute) {
            case NAME -> name();
            case MOD -> mod();
            case ID -> id();
            case TOOLTIP -> tooltip();
            case TAG -> tag();
        };
    }

    @Override
    public boolean test(ItemStack stack) {
        return this.attributeMatcher.test(stack);
    }

    private ItemStackPredicate name() {
        return itemStack -> stringMatcher.test(context.translate(itemStack.getHoverName()));
    }

    private ItemStackPredicate mod() {
        return itemStack -> stringMatcher.test(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace());
    }

    private ItemStackPredicate id() {
        return itemStack -> stringMatcher.test(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath());
    }

    private ItemStackPredicate tooltip() {
        return itemStack -> stringMatcher.test(itemStack
                .getTooltipLines(context.tooltipContext(), context.player(), context.tooltipFlag())
                .stream()
                .map(context::translate)
                .collect(Collectors.joining("\n")));
    }

    private ItemStackPredicate tag() {
        return itemStack -> itemStack
                .getTags()
                .map(tk -> tk.location().toString())
                .anyMatch(stringMatcher);
    }
}
