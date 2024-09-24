package net.natte.re_search.client.screen;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.natte.re_search.query.QueryParser;
import net.natte.re_search.query.Token;
import net.natte.re_search.query.Word;
import net.natte.re_search.search.context.SearchMode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class SyntaxHighlighter {

    private static final Style SPACE_STYLE = Style.EMPTY;
    private static final Style NAME_STYLE = Style.EMPTY;
    private static final Style ID_STYLE = Style.EMPTY.withColor(0x8d7eed);
    private static final Style MOD_STYLE = Style.EMPTY.withColor(0xffa8f3);
    private static final Style TOOLTIP_STYLE = Style.EMPTY.withColor(0xffe0ad);
    private static final Style TAG_STYLE = Style.EMPTY.withColor(0x9efff4);
    private static final Style SPECIAL_STYLE = Style.EMPTY.withColor(0xf6bf57);
    private static final Style NEGATE_STYLE = Style.EMPTY.withColor(0xe43b3b);

    private final List<FormattedCharSequence> styledChars = new ArrayList<>();

    public List<String> tokens = new ArrayList<>();
    public List<Word> words = new ArrayList<>();

    private SearchMode searchMode = SearchMode.EXTENDED;

    public FormattedCharSequence provideRenderText(String original, int firstCharacterIndex) {
        return FormattedCharSequence.composite(styledChars.subList(firstCharacterIndex, firstCharacterIndex + original.length()));
//        int tokenIndex = 0;
//        int charIndex = 0;
//        while (true) {
//            FormattedCharSequence s = styledChars.get(tokenIndex++);
//            if (charIndex + s.accept())
//
////            continue a;
//                break continue;
//        }
//        return FormattedCharSequence.composite(styledChars);
    }

    public void oldRrefresh(String string) {

        // searchMode != extended, dont highlight
        if (searchMode != SearchMode.EXTENDED) {
            styledChars.clear();
            for (char c : string.toCharArray()) {
                styledChars.add(FormattedCharSequence.forward(String.valueOf(c), Style.EMPTY));
            }
            return;
        }

        Style style = SPACE_STYLE;
        boolean isSpaceStyle = true;
        styledChars.clear();
        String currentWord = "";
        tokens.clear();
        char quote = ' ';
        boolean isInQuote = false;
        String quotes = "\"'/`";
        for (char c : string.toCharArray()) {
            boolean special = (!isSpaceStyle && c == '$') || c == '^';
            boolean isNegate = isSpaceStyle && c == '-';

            if (isSpaceStyle) {
                isSpaceStyle = false;
                if (c == '@') {
                    style = MOD_STYLE;
                } else if (c == '*') {
                    style = ID_STYLE;
                } else if (c == '#') {
                    style = TOOLTIP_STYLE;
                } else if (c == '$') {
                    style = TAG_STYLE;
                } else if (c == '-') {
                    isNegate = true;
                    isSpaceStyle = true;
                } else {
                    style = NAME_STYLE;
                }
            }
            if (c == ' ') {
                style = SPACE_STYLE;
                isSpaceStyle = true;
                isNegate = false;
            }
            if (!isInQuote && quotes.indexOf(c) != -1) {
                isInQuote = true;
                quote = c;
            } else if (isInQuote && c == quote) {
                isInQuote = false;
            } else if (c == ' ') {
                if (isInQuote) {
                    currentWord += c;
                } else {
                    tokens.add(currentWord);
                    currentWord = "";

                }
            } else {
                currentWord += c;
            }
            styledChars.add(FormattedCharSequence.forward(String.valueOf(c),
                    special ? SPECIAL_STYLE : isNegate ? NEGATE_STYLE : style));

        }
        tokens.add(currentWord);
        tokens.add("--");
//        rw = /^(?<negate>[!-]?)(?<prefix>[@$*#]?)(?<content>(?<quote>["'`\/])(.*?)(\g<quote>)|([^ '"`\/]+)) */
        Pattern p = Pattern.compile("^([!-]?([\"'`/])(.*?)(\\2) *)*$");
        p.matcher(string).results().map(MatchResult::group).forEach(tokens::add);
//        words.add("--");
    }

    public void refresh(String string) {
        tokens.clear();
        styledChars.clear();
        List<Token> tokens_ = QueryParser.tokenize(string);
        for (Token token : tokens_) {
            String content = token.content();
            for (int i = 0; i < content.length(); ++i) {
                styledChars.add(FormattedCharSequence.forward(String.valueOf(content.charAt(i)), token.style()));
            }
            tokens.add("`" + token.content() + "` " + token.primary() + " " + token.secondary());
        }
        words = QueryParser.parse(string);

    }

    public void setMode(SearchMode searchMode) {
        this.searchMode = searchMode;
    }

}