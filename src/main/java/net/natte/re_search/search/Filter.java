package net.natte.re_search.search;

import com.google.common.base.Predicates;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.natte.re_search.search.context.SearchMode;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Filter {

    private final SearchOptions searchOptions;
    private final ServerPlayer player;
    private final Predicate<ItemStack> predicate;

    public Filter(SearchOptions searchOptions, ServerPlayer player) {
        this.searchOptions = searchOptions;
        this.player = player;
//        this.predicate = itemStack -> !itemStack.isEmpty();

        this.predicate = parseFilterExpression();
    }

    private Predicate<ItemStack> parseFilterExpression() {
        switch (searchOptions.searchMode()) {
            case REGEX -> {
                // TODO: make regex -> extended+regex
                Pattern pattern = Pattern.compile(searchOptions.expression(),
                        searchOptions.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE);

                return itemStack -> {
                    String name = itemStack.getItem().getDescription().getString();
                    return pattern.matcher(name).find();
                };
            }

            case LITERAL -> {
                String string = searchOptions.expression();
                Predicate<String> predicate = StringMatcher.overCaseFold((a, b) -> b.contains(a),
                        this.searchOptions.isCaseSensitive(), string);
                Predicate<ItemStack> p = name(predicate);
                p = p.or(mod(predicate));
                p = p.or(id(predicate));
                p = p.or(tag(predicate));
                p = p.or(tooltip(predicate));
                return p;
            }
            case EXTENDED -> {
                String[] words = searchOptions.expression().split(" ");
                Predicate<ItemStack> p = Predicates.alwaysTrue();
                for (String word : words) {
                    if (word.isEmpty())
                        continue;
                    p = p.and(parseWord(word));
                }
                return p;
            }
        }
        return null;
    }

    private Predicate<ItemStack> parseWord(String word) {
        if (word.isEmpty()) {
            return itemStack -> true;
        }
        char c = word.charAt(0);
        String string = word.substring(1);
        if (c == '-') {
            return negate(parseWord(string));
        }
        Predicate<String> predicate = StringMatcher.preparePredicate(string, this.searchOptions);

        if (c == '@') {
            return string.contains(":") ? modColonId(predicate) : mod(predicate);
        }
        if (c == '*') {
            return id(predicate);
        }
        if (c == '$') {
            return string.contains(":") ? tagColonId(predicate) : tag(predicate);
        }
        if (c == '#') {
            return tooltip(predicate);
        } else {
            return name(StringMatcher.preparePredicate(word, this.searchOptions));
        }

    }

    public boolean test(ItemStack itemStack) {
        return this.predicate.test(itemStack);
    }

    public Predicate<ItemStack> mod(Predicate<String> predicate) {
        // @mod
        return itemStack -> predicate.test(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace());
    }

    public Predicate<ItemStack> modColonId(Predicate<String> predicate) {
        // @mod:item
        return itemStack -> predicate.test(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());
    }

    public Predicate<ItemStack> id(Predicate<String> predicate) {
        // *item
        return itemStack -> predicate.test(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath());
    }

    public Predicate<ItemStack> tag(Predicate<String> predicate) {
        // $tag
        return itemStack -> itemStack.getTags().anyMatch(tag -> predicate.test(tag.location().getPath()));
    }

    public Predicate<ItemStack> tagColonId(Predicate<String> predicate) {
        // $mod:tag
        return itemStack -> itemStack.getTags().anyMatch(tag -> predicate.test(tag.location().toString()));
    }

    public Predicate<ItemStack> tooltip(Predicate<String> predicate) {
        return itemStack -> itemStack.getTooltipLines(Item.TooltipContext.of(player.level()), player, TooltipFlag.ADVANCED).stream()
                .anyMatch(line -> predicate.test(line.getString()));

    }

    public Predicate<ItemStack> name(Predicate<String> predicate) {
        return itemStack -> predicate.test(itemStack.getHoverName().getString());
    }

    public Predicate<ItemStack> negate(Predicate<ItemStack> predicate) {
        return itemStack -> !predicate.test(itemStack);
    }

}
