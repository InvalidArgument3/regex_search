package net.natte.re_search.search;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.natte.re_search.query.Attribute;
import net.natte.re_search.search.context.Context;

import java.util.stream.Collectors;

public interface ItemAttributeGetter {
    String get(ItemStack itemStack, Context context);

    default ItemAttributeGetter of(Attribute attribute) {
        return switch (attribute) {
            case NAME -> new Name();
            case MOD -> new Mod();
            case ID -> new Id();
            case TOOLTIP -> new Tooltip();
            case TAG -> new Tag();
        };
    }

    class Name implements ItemAttributeGetter {

        @Override
        public String get(ItemStack itemStack, Context context) {
            return context.translate(itemStack.getHoverName());
        }
    }

    class Mod implements ItemAttributeGetter {
        @Override
        public String get(ItemStack itemStack, Context context) {
            return BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace();
        }
    }

    class Id implements ItemAttributeGetter {
        @Override
        public String get(ItemStack itemStack, Context context) {
            return BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath();
        }
    }

    class Tooltip implements ItemAttributeGetter {
        @Override
        public String get(ItemStack itemStack, Context context) {
            return itemStack
                    .getTooltipLines(Item.TooltipContext.of(context.level()), context.player(), context.tooltipFlag())
                    .stream()
                    .map(context::translate)
                    .collect(Collectors.joining("\n"));
        }
    }

    class Tag implements ItemAttributeGetter {

        @Override
        public String get(ItemStack itemStack, Context context) {
            // TODO: Collection<Tag>? Predicate<ItemStack>?
            return itemStack
                    .getTags()
                    .map(tk -> tk.location().toString())
                    .collect(Collectors.joining("\n"));
        }
    }
}
