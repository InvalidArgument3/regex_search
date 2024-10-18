package net.natte.re_search.search;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
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
        this.attributeMatcher = of(attribute);
    }

    private ItemStackPredicate of(Attribute attribute) {
        return switch (attribute) {
            case NAME -> new Name();
            case MOD -> new Mod();
            case ID -> new Id();
            case TOOLTIP -> new Tooltip();
            case TAG -> new Tag();
        };
    }

    @Override
    public boolean test(ItemStack stack) {
        return this.attributeMatcher.test(stack);
    }

    class Name implements ItemStackPredicate {
        @Override
        public boolean test(ItemStack itemStack) {
            return stringMatcher.test(context.translate(itemStack.getHoverName()));
        }
    }

    class Mod implements ItemStackPredicate {
        @Override
        public boolean test(ItemStack itemStack) {
            return stringMatcher.test(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace());
        }
    }

    class Id implements ItemStackPredicate {
        @Override
        public boolean test(ItemStack itemStack) {
            return stringMatcher.test(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath());
        }
    }

    class Tooltip implements ItemStackPredicate {
        @Override
        public boolean test(ItemStack itemStack) {
            return stringMatcher.test(itemStack
                    .getTooltipLines(Item.TooltipContext.of(context.level()), context.player(), context.tooltipFlag())
                    .stream()
                    .map(context::translate)
                    .collect(Collectors.joining("\n")));
        }
    }

    class Tag implements ItemStackPredicate {
        @Override
        public boolean test(ItemStack itemStack) {
            return itemStack
                    .getTags()
                    .map(tk -> tk.location().toString())
                    .anyMatch(stringMatcher);
        }
    }
}
