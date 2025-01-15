package net.natte.re_search.search.matcher;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.natte.re_search.query.QueryParser;
import net.natte.re_search.query.Word;
import net.natte.re_search.search.SearchOptions;
import net.natte.re_search.search.context.Context;

import java.util.List;
import java.util.Optional;

public class ItemStackMatcher implements ItemStackPredicate {

    private final SearchOptions searchOptions;
    private final Context context;

    private final ItemStackPredicate[] predicates;

    private ItemStackMatcher(SearchOptions searchOptions, Context context) {
        this.searchOptions = searchOptions;
        this.context = context;

        List<Word> words = QueryParser.parse(searchOptions.query());

        this.predicates = words
                .stream()
                .map(this::createPredicate)
                .flatMap(Optional::stream)
                .toArray(ItemStackPredicate[]::new);
    }

    public static ItemStackMatcher create(SearchOptions searchOptions, ServerPlayer player) {
        return new ItemStackMatcher(searchOptions, Context.create(searchOptions, player));
    }

    private Optional<ItemStackPredicate> createPredicate(Word word) {
        return StringMatcher.of(word, searchOptions)
                .map(stringMatcher -> {
                    StringMatcher adjusted = word.isNegate() ? stringMatcher.negate()::test : stringMatcher;
                    return new ItemStackAttributeMatcher(context, word.attribute(), adjusted);
                });
    }

    @Override
    public boolean test(ItemStack stack) {
        for (ItemStackPredicate predicate : predicates)
            if (!predicate.test(stack))
                return false;

        return true;
    }
}
