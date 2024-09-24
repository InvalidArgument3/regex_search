package net.natte.re_search.query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {

    private static final Pattern WORD_PATTERN = Pattern.compile(
            "^" +
                    "(?>(?<negate>[!-]?))" +
                    "(?>(?<prefix>[@$*#]?))" +
                    "(" +
                    "(?<quote>[\"'])(?<text>.*?)(\\k<quote>)" +
                    "|/(?<slashregex>(\\\\.|[^\\\\/])*?)/" +
                    "|(?<regex>[^\"'/ ][^ ]*)" +
                    ")" +
                    "(?<space> *)");

    // used when actually parsing for the search
    public static List<Word> parse(final String query) {

        List<Word> words = new ArrayList<>();
        String processedString = query.stripLeading();
        do {
            Matcher matcher = WORD_PATTERN.matcher(processedString);
            boolean matched = matcher.find();
            if (!matched)
                break;

            words.add(Word.fromMatch(matcher.toMatchResult()));
            processedString = processedString.substring(matcher.end());

        } while (!processedString.isEmpty());

        return words;
    }

    // used for syntax highlighting
    public static List<Token> tokenize(final String query) {
        List<Token> tokens = new ArrayList<>();

        String processedString = query;
        if (processedString.indexOf(' ') == 0) {
            int length = processedString.length();
            processedString = processedString.stripLeading();
            tokens.add(Token.of(" ".repeat(length - processedString.length()), Token.Type.SPACE));
        }
        do {
            Matcher matcher = WORD_PATTERN.matcher(processedString);
            boolean matched = matcher.find();
            if (!matched) {
                tokens.add(Token.of(processedString, Token.Type.LEFTOVER));
                break;
            }

            findTokens(tokens, matcher.toMatchResult());
            processedString = processedString.substring(matcher.end());

        } while (!processedString.isEmpty());

        return tokens;
    }

    private static void findTokens(List<Token> tokens, MatchResult match) {
        String negate = match.group("negate");
        if (!negate.isEmpty())
            tokens.add(Token.of(negate, Token.Type.NEGATE));

        String prefix = match.group("prefix");
        Attribute attribute = Attribute.fromPrefix(prefix);

        if (attribute != Attribute.NAME) {
            Token.Type type = switch (attribute) {
                case MOD -> Token.Type.MOD;
                case ID -> Token.Type.ID;
                case TOOLTIP -> Token.Type.TOOLTIP;
                case TAG -> Token.Type.TAG;
                default -> throw new IllegalStateException("Unexpected value: " + prefix);
            };
            tokens.add(Token.of(prefix, type));
        }

        Token.Type attributeType = switch (attribute) {
            case NAME -> Token.Type.NAME;
            case MOD -> Token.Type.MOD;
            case ID -> Token.Type.ID;
            case TOOLTIP -> Token.Type.TOOLTIP;
            case TAG -> Token.Type.TAG;
        };

        String quote = match.group("quote");
        if (quote == null) {
            findRegexTokens(tokens, match, attributeType);
        } else {
            tokens.add(Token.of(quote, Token.Type.QUOTE, attributeType));
            tokens.add(Token.of(match.group("text"), Token.Type.LITERAL, attributeType));
            tokens.add(Token.of(quote, Token.Type.QUOTE, attributeType));
        }

        String space = match.group("space");
        tokens.add(Token.of(space, Token.Type.SPACE));
    }

    private static void findRegexTokens(List<Token> tokens, MatchResult match, Token.Type attributeType) {

        boolean slashSeparated = match.group("slashregex") != null;
        if (slashSeparated)
            tokens.add(Token.of("/", Token.Type.REGEX_SLASH, attributeType));

        String regex = slashSeparated ? match.group("slashregex") : match.group("regex");
        boolean escaped = false;
        for (char c : regex.toCharArray()) {
            if (escaped) {
                escaped = false;
                tokens.add(Token.of(String.valueOf(c), Token.Type.REGEX_ESCAPED, attributeType));
            } else if (c == '\\') {
                escaped = true;
                tokens.add(Token.of(String.valueOf(c), Token.Type.REGEX_ESCAPED, attributeType));
            } else if ("(){}[]".indexOf(c) != -1) {
                tokens.add(Token.of(String.valueOf(c), Token.Type.REGEX_GROUPING, attributeType));
            } else if ("|^$".indexOf(c) != -1) {
                tokens.add(Token.of(String.valueOf(c), Token.Type.REGEX_SPECIAL, attributeType));
            } else if (".*+?".indexOf(c) != -1) {
                tokens.add(Token.of(String.valueOf(c), Token.Type.REGEX_ESCAPED, attributeType));
            } else {
                tokens.add(Token.of(String.valueOf(c), Token.Type.REGEX, attributeType));
            }
        }

        if (slashSeparated)
            tokens.add(Token.of("/", Token.Type.REGEX_SLASH, attributeType));
    }
}
