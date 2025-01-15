package net.natte.regex_search.search.matcher;

import net.natte.regex_search.query.Word;
import net.natte.regex_search.search.SearchOptions;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public interface StringMatcher extends Predicate<String> {
    static Optional<StringMatcher> of(Word word, SearchOptions searchOptions) {
        try {
            return Optional.of(new Regex(word.content(), getRegexFlags(word, searchOptions)));
        } catch (PatternSyntaxException e) {
            return Optional.empty();
        }
    }

    private static int getRegexFlags(Word word, SearchOptions searchOptions) {

        int flag = 0;
        flag |= Pattern.DOTALL;

        if (!searchOptions.isCaseSensitive())
            flag |= Pattern.CASE_INSENSITIVE;

        if (!word.isRegex())
            flag |= Pattern.LITERAL;

        return flag;
    }


    class Regex implements StringMatcher {
        final Matcher matcher;

        public Regex(String regex, int flags) {
            this.matcher = Pattern.compile(regex, flags).matcher("");
        }

        @Override
        public boolean test(String s) {
            return matcher.reset(s).find();
        }
    }
}
