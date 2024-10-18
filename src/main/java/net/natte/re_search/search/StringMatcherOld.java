package net.natte.re_search.search;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class StringMatcherOld {

    public static Predicate<String> preparePredicate(String string, SearchOptions options) {
        return StringMatcherOld.overCaseFold(StringMatcherOld.parseBoundaries(string),
                options.isCaseSensitive(), StringMatcherOld.trimBoundary(string));

    }

    private static BiPredicate<String, String> parseBoundaries(String string) {
        if (string.isEmpty()) {
            return (filterString, itemString) -> itemString.contains(filterString);
        }
        boolean matchStart = string.charAt(0) == '^';
        boolean matchEnd = string.charAt(string.length() - 1) == '$';

        if (matchStart)
            return matchEnd ? String::equals : String::startsWith;
        else
            return matchEnd ? String::endsWith : String::contains;
    }

    public static String trimBoundary(String string) {
        if (string.startsWith("^"))
            string = string.substring(1);
        if (string.endsWith("$"))
            string = string.substring(0, string.length() - 1);
        return string;
    }

    public static Predicate<String> overCaseFold(BiPredicate<String, String> function, boolean isCaseSensitive,
                                                 String string) {
        if (isCaseSensitive)
            return a -> function.test(string, a);
        else {
            String lower = string.toLowerCase();
            return a -> function.test(lower, a.toLowerCase());
        }
    }
}
