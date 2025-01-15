package net.natte.regex_search.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {

    private static final Pattern WORD_PATTERN = Pattern.compile(
            "^" +
                    "(?>(?<negate>[!-]?))" +
                    "(?>(?<prefix>[@$:#]?))" +
                    "(" +
                    "(?<quote>[\"'])(?<text>(\\\\.|[^\\\\])*?)(\\k<quote>)" +
                    "|/(?<slashregex>(\\\\.|[^\\\\/])*?)/" +
                    "|(?<regex>[^\"'/ ][^ ]*)" +
                    ")" +
                    "(?<space> *)");

    private static final Pattern LITERALS_ESCAPE_PATTERN = Pattern.compile("^((?<escape>\\\\.)|(?<literal>[^\\\\]+))");
    public static final String ALLOWED_ESCAPES_IN_LITERAL = "nt\\";
    public static final Map<Character, Character> CHARACTER_ESCAPES = Map.of('n', '\n', 't', '\t', '\\', '\\');
    public static final Pattern ESCAPE_PATTERN = Pattern.compile("\\\\[nt'\"\\\\]");

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
            tokens.add(Token.special(" ".repeat(length - processedString.length()), Token.SpecialType.SPACE));
        }
        do {
            Matcher matcher = WORD_PATTERN.matcher(processedString);
            boolean matched = matcher.find();
            if (!matched) {
                tokens.add(Token.special(processedString, Token.SpecialType.LEFTOVER));
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
            tokens.add(Token.special(negate, Token.SpecialType.NEGATE));

        String prefix = match.group("prefix");
        Attribute attribute = Attribute.fromPrefix(prefix);

        if (attribute != Attribute.NAME) {
            Token.AttributeType type = switch (attribute) {
                case MOD -> Token.AttributeType.MOD;
                case ID -> Token.AttributeType.ID;
                case TOOLTIP -> Token.AttributeType.TOOLTIP;
                case TAG -> Token.AttributeType.TAG;
                default -> throw new IllegalStateException("Unexpected value: " + prefix);
            };
            tokens.add(Token.attribute(prefix, type, Token.Type.PREFIX));
        }

        Token.AttributeType attributeType = switch (attribute) {
            case NAME -> Token.AttributeType.NAME;
            case MOD -> Token.AttributeType.MOD;
            case ID -> Token.AttributeType.ID;
            case TOOLTIP -> Token.AttributeType.TOOLTIP;
            case TAG -> Token.AttributeType.TAG;
        };

        if (attributeType == Token.AttributeType.NAME && match.group("slashregex") != null)
            attributeType = Token.AttributeType.NAME_REGEX;

        String quote = match.group("quote");
        if (quote == null) {
            findRegexTokens(tokens, match, attributeType);
        } else {
            tokens.add(Token.attribute(quote, attributeType, Token.Type.QUOTE));
            findLiteralsEscapes(tokens, quote.charAt(0), match.group("text"), attributeType);
//            tokens.add(Token.attribute(match.group("text"), attributeType, Token.Type.LITERAL));
            tokens.add(Token.attribute(quote, attributeType, Token.Type.QUOTE));
        }

        String space = match.group("space");
        tokens.add(Token.special(space, Token.SpecialType.SPACE));
    }

    private static void findLiteralsEscapes(List<Token> tokens, char quote, String text, Token.AttributeType attributeType) {
        while (!text.isEmpty()) {
            Matcher matcher = LITERALS_ESCAPE_PATTERN.matcher(text);
            boolean matched = matcher.find();
            assert matched;
            MatchResult matchResult = matcher.toMatchResult();
            String escaped = matchResult.group("escape");
            String literal = matchResult.group("literal");
            if (escaped != null && escaped.charAt(1) != quote && ALLOWED_ESCAPES_IN_LITERAL.indexOf(escaped.charAt(1)) == -1) {
                literal = escaped;
            }

            tokens.add(literal != null
                    ? Token.attribute(literal, attributeType, Token.Type.LITERAL)
                    : Token.attribute(escaped, attributeType, Token.Type.LITERAL_ESCAPED));
            text = text.substring(matcher.end());
        }
    }

    private static void findRegexTokens(List<Token> tokens, MatchResult match, Token.AttributeType attributeType) {

        boolean slashSeparated = match.group("slashregex") != null;
        if (slashSeparated)
            tokens.add(Token.attribute("/", attributeType, Token.Type.REGEX_SLASH));

        String regex = slashSeparated ? match.group("slashregex") : match.group("regex");
        boolean hasError = !Util.isValidRegex(regex);
        boolean escaped = false;
        for (char c : regex.toCharArray()) {
            if (escaped) {
                escaped = false;
                tokens.add(Token.attribute(String.valueOf(c), attributeType, Token.Type.REGEX_ESCAPED, hasError));
            } else if (c == '\\') {
                escaped = true;
                tokens.add(Token.attribute(String.valueOf(c), attributeType, Token.Type.REGEX_ESCAPED, hasError));
            } else if ("(){}[]".indexOf(c) != -1) {
                tokens.add(Token.attribute(String.valueOf(c), attributeType, Token.Type.REGEX_GROUPING, hasError));
            } else if ("|^$".indexOf(c) != -1) {
                tokens.add(Token.attribute(String.valueOf(c), attributeType, Token.Type.REGEX_SPECIAL, hasError));
            } else if (".*+?".indexOf(c) != -1) {
                tokens.add(Token.attribute(String.valueOf(c), attributeType, Token.Type.REGEX_ESCAPED, hasError));
            } else {
                tokens.add(Token.attribute(String.valueOf(c), attributeType, Token.Type.REGEX, hasError));
            }
        }

        if (slashSeparated)
            tokens.add(Token.attribute("/", attributeType, Token.Type.REGEX_SLASH));
    }
}
