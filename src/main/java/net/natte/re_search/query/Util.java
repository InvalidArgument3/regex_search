package net.natte.re_search.query;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Util {
    public static boolean isValidRegex(String regex) {
        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }
}
