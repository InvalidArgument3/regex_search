package net.natte.re_search.query;

import net.minecraft.network.chat.Style;
import net.natte.re_search.client.ColorTheme;

public interface Token {
    default Style getStyle(ColorTheme colorTheme) {
        return colorTheme.getStyle(this);
    }

    String getContent();

    static Attribute attribute(String content, AttributeType attributeType, Type type) {
        return new Attribute(content, attributeType, type);
    }

    static Special special(String content, SpecialType specialType) {
        return new Special(content, specialType);
    }

    record Attribute(String content, AttributeType attributeType, Type type) implements Token {
        @Override
        public String getContent() {
            return content;
        }
    }

    record Special(String content, SpecialType specialType) implements Token {
        @Override
        public String getContent() {
            return content;
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
