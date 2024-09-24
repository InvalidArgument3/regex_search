package net.natte.re_search.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.natte.re_search.query.Token;

public class Styles {
    private static final Style SPACE = Style.EMPTY;

    private static final Style NAME_PRIMARY = Style.EMPTY;
    private static final Style ID_PRIMARY = Style.EMPTY.withColor(0x8d7eed);
    private static final Style MOD_PRIMARY = Style.EMPTY.withColor(0xffa8f3);
    private static final Style TOOLTIP_PRIMARY = Style.EMPTY.withColor(0xffe0ad);
    private static final Style TAG_PRIMARY = Style.EMPTY.withColor(0x9efff4);

    private static final Style NAME_SECONDARY = Style.EMPTY;
    private static final Style ID_SECONDARY = Style.EMPTY.withColor(0xc7c2e8);
    private static final Style MOD_SECONDARY = Style.EMPTY.withColor(0xffd1f8);
    private static final Style TOOLTIP_SECONDARY = Style.EMPTY.withColor(0xfff4e5);
    private static final Style TAG_SECONDARY = Style.EMPTY.withColor(0xe0fffa);


    private static final Style NEGATE = Style.EMPTY.withColor(0xe43b3b);
//    private static final Style LEFTOVER = Style.EMPTY.withColor(0x525252);
    private static final Style LEFTOVER = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
    private static final Style ERROR = Style.EMPTY.withColor(0x4d0000).withUnderlined(true).withItalic(true);

    private static final Style REGEX_SLASH = Style.EMPTY.withColor(0xd16969);

    private static final Style REGEX_RED = Style.EMPTY.withColor(0xd16969);
    private static final Style REGEX_ORANGE = Style.EMPTY.withColor(0xce9178);
    private static final Style REGEX_YELLOW = Style.EMPTY.withColor(0xd7ba7d );
    private static final Style REGEX_LIGHT_YELLOW = Style.EMPTY.withColor(0xdcdcaa);

    public static Style getStyle(Token token) {
        return switch (token.primary()) {
            case MOD, TOOLTIP, ID, TAG -> getAttributePrimary(token.primary());

            case NEGATE -> NEGATE;
            case QUOTE -> getAttributePrimary(token.secondary());
            case REGEX_SLASH -> REGEX_SLASH;

            case REGEX_SPECIAL -> REGEX_LIGHT_YELLOW;
            case REGEX_ESCAPED -> REGEX_YELLOW;
            case REGEX_GROUPING -> REGEX_ORANGE;

            case LITERAL -> getAttributeSecondary(token.secondary());
            case REGEX -> getAttributeSecondary(token.secondary());

            case SPACE -> SPACE;
            case LEFTOVER -> LEFTOVER;
            default -> ERROR;
        };
    }

    private static Style getAttributePrimary(Token.Type type) {
        return switch (type) {
            case NAME -> NAME_PRIMARY;
            case MOD -> MOD_PRIMARY;
            case ID -> ID_PRIMARY;
            case TOOLTIP -> TOOLTIP_PRIMARY;
            case TAG -> TAG_PRIMARY;
            default -> ERROR;
        };
    }

    private static Style getAttributeSecondary(Token.Type type) {
        return switch (type) {
            case NAME -> NAME_SECONDARY;
            case MOD -> MOD_SECONDARY;
            case ID -> ID_SECONDARY;
            case TOOLTIP -> TOOLTIP_SECONDARY;
            case TAG -> TAG_SECONDARY;
            default -> ERROR;
        };
    }
}
