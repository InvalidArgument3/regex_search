package net.natte.re_search.search;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface StringMatcher extends Predicate<String> {

    static StringMatcher regex(String content, SearchOptions searchOptions) {
        return new Regex(content, getRegexFlags(searchOptions));
    }

    static StringMatcher literal(String content, SearchOptions searchOptions) {
        return new Regex(content, getRegexFlags(searchOptions) | Pattern.LITERAL);
    }

    private static int getRegexFlags(SearchOptions searchOptions) {

        int flag = 0;
        if (!searchOptions.isCaseSensitive())
            flag |= Pattern.CASE_INSENSITIVE;
        flag |= Pattern.DOTALL;
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
