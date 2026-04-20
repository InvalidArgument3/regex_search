package natte.re_search.search;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class Filter {

    private final SearchOptions searchOptions;
    private final ServerPlayer player;
    private Predicate<ItemStack> predicate;

    public Filter(SearchOptions searchOptions, ServerPlayer player) {
        this.searchOptions = searchOptions;
        this.player = player;
        this.predicate = itemStack -> !itemStack.isEmpty();

        parseFilterExpression();
    }

    private void parseFilterExpression() {
        if (searchOptions.searchMode == 0) {
            Pattern pattern = Pattern.compile(searchOptions.expression,
                    searchOptions.isCaseSensitive ? 0 : Pattern.CASE_INSENSITIVE);

            add(itemStack -> {
                String name = itemStack.getItem().getName(itemStack).getString();
                return pattern.matcher(name).find();
            });

        } else if (searchOptions.searchMode == 1) {
            String string = searchOptions.expression;
            Predicate<String> stringPredicate = StringMatcher.overCaseFold((a, b) -> b.contains(a),
                    this.searchOptions.isCaseSensitive, string);
            Predicate<ItemStack> p = name(stringPredicate);
            p = p.or(mod(stringPredicate));
            p = p.or(id(stringPredicate));
            p = p.or(tag(stringPredicate));
            p = p.or(tooltip(stringPredicate));
            add(p);
        }
        if (searchOptions.searchMode == 2) {
            String[] words = searchOptions.expression.split(" ");
            for (String word : words) {
                if (word.length() == 0)
                    continue;
                add(parseWord(word));
            }
        }
    }

    private Predicate<ItemStack> parseWord(String word) {
        if (word.length() == 0) {
            return itemStack -> true;
        }
        char c = word.charAt(0);
        String string = word.substring(1);
        if (c == '-') {
            return negate(parseWord(string));
        }
        Predicate<String> stringPredicate = StringMatcher.preparePredicate(string, this.searchOptions);

        if (c == '@') {
            return string.contains(":") ? modColonId(stringPredicate) : mod(stringPredicate);
        }
        if (c == '*') {
            return id(stringPredicate);
        }
        if (c == '$') {
            return string.contains(":") ? tagColonId(stringPredicate) : tag(stringPredicate);
        }
        if (c == '#') {
            return tooltip(stringPredicate);
        } else {
            return name(StringMatcher.preparePredicate(word, this.searchOptions));
        }

    }

    public boolean test(ItemStack itemStack) {
        return this.predicate.test(itemStack);
    }

    private void add(Predicate<ItemStack> next) {
        this.predicate = this.predicate.and(next);
    }

    public Predicate<ItemStack> mod(Predicate<String> stringPredicate) {
        return itemStack -> stringPredicate.test(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace());
    }

    public Predicate<ItemStack> modColonId(Predicate<String> stringPredicate) {
        return itemStack -> stringPredicate.test(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());
    }

    public Predicate<ItemStack> id(Predicate<String> stringPredicate) {
        return itemStack -> stringPredicate.test(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath());
    }

    public Predicate<ItemStack> tag(Predicate<String> stringPredicate) {
        return itemStack -> BuiltInRegistries.ITEM.getResourceKey(itemStack.getItem())
                .flatMap(BuiltInRegistries.ITEM::getHolder)
                .map(holder -> holder.tags().anyMatch(tagKey -> stringPredicate.test(tagKey.location().getPath())))
                .orElse(false);
    }

    public Predicate<ItemStack> tagColonId(Predicate<String> stringPredicate) {
        return itemStack -> BuiltInRegistries.ITEM.getResourceKey(itemStack.getItem())
                .flatMap(BuiltInRegistries.ITEM::getHolder)
                .map(holder -> holder.tags().anyMatch(tagKey -> stringPredicate.test(tagKey.location().toString())))
                .orElse(false);
    }

    public Predicate<ItemStack> tooltip(Predicate<String> stringPredicate) {
        Item.TooltipContext ctx = Item.TooltipContext.of(player.level());
        return itemStack -> itemStack.getTooltipLines(ctx, player, TooltipFlag.Default.NORMAL).stream()
                .anyMatch(line -> stringPredicate.test(line.getString()));

    }

    public Predicate<ItemStack> name(Predicate<String> stringPredicate) {
        return itemStack -> stringPredicate.test(itemStack.getHoverName().getString());
    }

    public Predicate<ItemStack> negate(Predicate<ItemStack> inner) {
        return itemStack -> !inner.test(itemStack);
    }

}
