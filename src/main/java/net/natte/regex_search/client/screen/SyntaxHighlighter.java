package net.natte.regex_search.client.screen;

import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.natte.regex_search.client.ColorTheme;
import net.natte.regex_search.query.QueryParser;
import net.natte.regex_search.query.Token;
import net.natte.regex_search.query.Word;

import java.util.ArrayList;
import java.util.List;

public class SyntaxHighlighter {

    private final List<FormattedCharSequence> styledChars = new ArrayList<>();

    public List<String> tokens = new ArrayList<>();
    public List<Word> words = new ArrayList<>();

    public FormattedCharSequence provideRenderText(String original, int firstCharacterIndex) {
        if (original.isEmpty()) {
            return FormattedCharSequence.EMPTY;
        }

        int start = Math.max(0, firstCharacterIndex);
        int end = start + original.length();
        if (end > styledChars.size()) {
            return FormattedCharSequence.forward(original, Style.EMPTY);
        }

        return FormattedCharSequence.composite(styledChars.subList(start, end));
    }


    public void refresh(String string) {
        tokens.clear();
        styledChars.clear();
        List<Token> tokens_ = QueryParser.tokenize(string);
        for (Token token : tokens_) {
            String content = token.getContent();
            for (int i = 0; i < content.length(); ++i) {
                styledChars.add(FormattedCharSequence.forward(String.valueOf(content.charAt(i)), token.getStyle(ColorTheme.get())));
            }
            tokens.add(token.toString());
        }
        if (styledChars.size() != string.length()) {
            styledChars.clear();
            for (int i = 0; i < string.length(); ++i) {
                styledChars.add(FormattedCharSequence.forward(String.valueOf(string.charAt(i)), Style.EMPTY));
            }
        }
        words = QueryParser.parse(string);
    }

}
