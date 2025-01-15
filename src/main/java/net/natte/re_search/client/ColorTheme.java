package net.natte.re_search.client;

import net.minecraft.network.chat.Style;
import net.natte.re_search.query.Token;

public class ColorTheme {

    public static final ColorTheme DEFAULT = new ColorTheme(
            Palette.of(0xffffff, 0xcc3737, 0xfca955, 0xdcdcaa),
            Palette.of(0xffffff, 0xcc3737, 0xfca955, 0xdcdcaa),
            Palette.of(0x5555ff, 0x9b5cdb, 0xf22bf2, 0xdcdcaa),
            Palette.of(0x6c5fc3, 0x8d7eed, 0xfca955, 0xdcdcaa),
            Palette.of(0xffff55, 0xbdf486, 0xf4bd86, 0xdcdcaa),
            Palette.of(0x55ff55, 0x41eace, 0x0098ea, 0xdcdcaa)
    );
//    private enum U {
//        DEFAULT("",  0xffffff, 0xcc3737, 0xff5555, 0xfca955, NameQuery::new, RegexNameQuery::new),
//        MOD(    "@", 0x5555ff, 0x5555ff, 0x9b5cdb, 0xf22bf2, ModQuery::new, RegexModQuery::new),
//        TOOLTIP("$", 0xffff55, 0xffff55, 0xbdf486, 0xf4bd86, TooltipQuery::new, RegexTooltipQuery::new),
//        TAG(    "#", 0x55ff55, 0x55ff55, 0x41eace, 0x0098ea, TagQuery::new, RegexTagQuery::new),
//                ;
//    }

    public static ColorTheme get() {
        return current;
    }

    public static void set(ColorTheme colorTheme) {
        current = colorTheme;
    }


    private static ColorTheme current = DEFAULT;

    static {
        set(new ColorTheme(
//                Palette.of(0xffffff, 0xcc3737, 0xfca955, 0xdcdcaa), // edit
                Palette.of(0xcc7832, 0xe8bf6a, 0xcc7832, 0xcc7832),

//                Palette.of(0xffffff, 0xcc3737, 0xfca955, 0xdcdcaa),  // edit
//                Palette.of(0xf58425, 0xf5a625, 0xf5be25, 0xf5be25),
//                Palette.of(0xcd9069, 0xcc7832, 0xe8be6a, 0xe8be6a),
//                Palette.of(0xcc7832, 0xe8bf6a, 0xd16969, 0xd16969),
                Palette.of(0xd16969, 0xe8bf6a, 0xcc7832, 0xcc7832),


//                Palette.of(0x5555ff, 0x9b5cdb, 0xc200ff, 0xf22bf2),
                Palette.of(0x5555ff, 0x9b5cdb, 0xf22bf2, 0xf22bf2),


//                Palette.of(0x6c5fc3, 0x8d7eed, 0xfca955, 0xdcdcaa), // edit
//                Palette.of(0xff62b8, 0xf370ff, 0xc570ff, 0xc570ff),
//                Palette.of(0xec1bff, 0xff2bbf, 0xff3d65, 0xff3d65),
//                Palette.of(0xff01ed, 0xff5fc3, 0xff3d65, 0xff3d65),
//                Palette.of(0xff01ed, 0xff83d3, 0xff3d8e, 0xff3d8e),
//                Palette.of(0xff01ed, 0xff74d3, 0xff3d8e, 0xff3d8e),,
                Palette.of(0xff26ed, 0xff74d3, 0xff3d8e, 0xff3d8e),

//                Palette.of(0xffff55, 0xbdf486, 0xf4bd86, 0xdcdcaa),
//                Palette.of(0xfefb54, 0xd7f476, 0xe8f568, 0xfff78f),
//                Palette.of(0xb6f557, 0xeeff38, 0xfefb54, 0xfff78f),
//                Palette.of(0xfefb54, 0xd7f476, 0x96f574, 0xf2ff83),
//                Palette.of(0xfffa43, 0xf2ff79, 0xb5f56c, 0x96f574),
//                Palette.of(0xfffa43, 0xf2ff79, 0xb5f56c, 0xb5f56c),,
                Palette.of(0xfffa43, 0xffff90, 0x96f574, 0x96f574),

//                Palette.of(0x55ff55, 0x41eace, 0x0098ea, 0x54fcff)
//                Palette.of(0x55ff55, 0x41eace, 0x0098ea, 0x0165ff)
                Palette.of(0x55ff55, 0x41eace, 0x0098ea, 0x0098ea)

        ));
    }

    private Palette name;
    private Palette regexName;
    private Palette mod;
    private Palette id;
    private Palette tooltip;
    private Palette tag;

    private Style space = Style.EMPTY;
    private Style negate = Style.EMPTY.withColor(0xe43b3b);
    private Style leftover = Style.EMPTY.withColor(0x525252);
//    private Style error = Style.EMPTY.withColor(0x4d0000).withUnderlined(true).withItalic(true);

    private ColorTheme(Palette name, Palette regexName, Palette mod, Palette id, Palette tooltip, Palette tag) {
        this.name = name;
        this.regexName = regexName;
        this.mod = mod;
        this.id = id;
        this.tooltip = tooltip;
        this.tag = tag;
    }

    public Style getStyle(Token token) {
        Style style = switch (token) {
            case Token.Attribute(
                    String content, Token.AttributeType attributeType, Token.Type type, boolean hasError
            ) -> {
                Palette palette = getPalette(attributeType);
                yield switch (type) {
                    case REGEX_SLASH, LITERAL, PREFIX -> palette.color();
                    case QUOTE, REGEX -> palette.regex();
                    case REGEX_SPECIAL, REGEX_GROUPING -> palette.regexSpecial();
                    case LITERAL_ESCAPED, REGEX_ESCAPED -> palette.escaped();
                };
            }
            case Token.Special(String content, Token.SpecialType specialType, boolean hasError) -> {
                yield switch (specialType) {
                    case NEGATE -> negate;
                    case SPACE -> space;
                    case LEFTOVER -> leftover;
                };
            }
            default -> throw new IllegalStateException("Unexpected value: " + token);
        };
        if (token.hasError())
            style = style.withUnderlined(true);

        return style;
    }

    private Palette getPalette(Token.AttributeType attributeType) {
        return switch (attributeType) {
            case NAME -> name;
            case NAME_REGEX -> regexName;
            case MOD -> mod;
            case ID -> id;
            case TOOLTIP -> tooltip;
            case TAG -> tag;
        };
    }

    record Palette(Style color, Style regex, Style escaped, Style regexSpecial) {
        public static Palette of(int color, int regex, int escaped, int regexSpecial) {
            return new Palette(
                    Style.EMPTY.withColor(color),
                    Style.EMPTY.withColor(regex),
                    Style.EMPTY.withColor(escaped),
                    Style.EMPTY.withColor(regexSpecial)
            );
        }
    }

}

