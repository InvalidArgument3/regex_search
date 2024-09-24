package net.natte.re_search.query;

import net.minecraft.network.chat.Style;
import net.natte.re_search.client.Styles;

public class Token {

    private String content;
    private Type primary;
    private Type secondary;

    private Token(String content, Type primary, Type secondary) {
        this.content = content;
        this.primary = primary;
        this.secondary = secondary;
    }

    public static Token of(String content, Type primary) {
        return of(content, primary, primary);
    }

    public static Token of(String content, Type primary, Type secondary) {
        return new Token(content, primary, secondary);
    }

    public String content() {
        return content;
    }

    public Type primary() {
        return primary;
    }

    public Type secondary() {
        return secondary;
    }

    public Style style() {
        return Styles.getStyle(this);
    }

    public enum Type {
        NAME,
        MOD,
        ID,
        TOOLTIP,
        TAG,

        NEGATE,
        QUOTE,
        REGEX_SLASH,

        LITERAL,
        REGEX,
        REGEX_SPECIAL,
        REGEX_GROUPING,
        REGEX_ESCAPED,

        SPACE,
        LEFTOVER
    }
}
