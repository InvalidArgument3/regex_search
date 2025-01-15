package net.natte.regex_search.query;

import net.minecraft.network.chat.Style;
import net.natte.regex_search.client.ColorTheme;

public interface Token {
    default Style getStyle(ColorTheme colorTheme) {
        return colorTheme.getStyle(this);
    }

    String getContent();

    boolean hasError();

    static Attribute attribute(String content, AttributeType attributeType, Type type) {
        return new Attribute(content, attributeType, type, false);
    }

    static Attribute attribute(String content, AttributeType attributeType, Type type, boolean hasError) {
        return new Attribute(content, attributeType, type, hasError);
    }

    static Special special(String content, SpecialType specialType) {
        return new Special(content, specialType, false);
    }

    static Special special(String content, SpecialType specialType, boolean hasError) {
        return new Special(content, specialType, hasError);
    }

    record Attribute(String content, AttributeType attributeType, Type type, boolean hasError) implements Token {
        @Override
        public String getContent() {
            return content;
        }

        @Override
        public boolean hasError() {
            return hasError;
        }
    }

    record Special(String content, SpecialType specialType, boolean hasError) implements Token {
        @Override
        public String getContent() {
            return content;
        }

        @Override
        public boolean hasError() {
            return hasError;
        }
    }

    enum AttributeType {
        NAME,
        NAME_REGEX,
        MOD,
        ID,
        TOOLTIP,
        TAG
    }

    enum Type {
        QUOTE,
        REGEX_SLASH,

        LITERAL,
        LITERAL_ESCAPED,
        REGEX,
        REGEX_SPECIAL,
        REGEX_GROUPING,
        REGEX_ESCAPED,

        PREFIX,
    }

    enum SpecialType {
        NEGATE,
        SPACE,
        LEFTOVER
    }

}
