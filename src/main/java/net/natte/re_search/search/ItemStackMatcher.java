package net.natte.re_search.search;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.natte.re_search.query.QueryParser;
import net.natte.re_search.query.Word;
import net.natte.re_search.search.context.Context;

import java.util.List;

public class ItemStackMatcher implements ItemStackPredicate {

    private final SearchOptions searchOptions;
    private final Context context;

    private final ItemStackPredicate[] predicates;

    private ItemStackMatcher(SearchOptions searchOptions, Context context) {
        this.searchOptions = searchOptions;
        this.context = context;

        List<Word> words = QueryParser.parse(searchOptions.query());

        this.predicates = words.stream().map(this::createPredicate).toArray(ItemStackPredicate[]::new);
    }

    public static ItemStackMatcher create(SearchOptions searchOptions, ServerPlayer player) {
        return new ItemStackMatcher(searchOptions, Context.create(player, searchOptions));
    }

    private ItemStackPredicate createPredicate(Word word) {
        StringMatcher stringMatcher = createStringMatcher(word);
        return new ItemStackAttributeMatcher(context, word.attribute(), stringMatcher);
    }

    private StringMatcher createStringMatcher(Word word) {
        StringMatcher stringMatcher;
        if (word.isRegex())
            stringMatcher = StringMatcher.regex(word.content(), searchOptions);
        else
            stringMatcher = StringMatcher.literal(word.content(), searchOptions);
        return word.isNegate() ? stringMatcher.negate()::test : stringMatcher;
    }

    @Override
    public boolean test(ItemStack stack) {
        for (ItemStackPredicate predicate : predicates)
            if (!predicate.test(stack))
                return false;

        return true;
    }
}
