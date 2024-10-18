//package net.natte.re_search.client;
//
//import net.minecraft.network.chat.Style;
//import net.natte.re_search.query.Token;
//
//public class Styles {
//
//    private static final Palette NAME_PALETTE = Palette.of(0xffffff, 0xcc3737, 0xfca955, 0xdcdcaa);
//    private static final Palette NAME_REGEX_PALETTE = Palette.of(0xffffff, 0xcc3737, 0xfca955, 0xdcdcaa);
//    private static final Palette MOD_PALETTE = Palette.of(0x5555ff, 0x9b5cdb, 0xf22bf2, 0xdcdcaa);
//    private static final Palette ID_PALETTE = Palette.of(0x6c5fc3, 0x8d7eed, 0xfca955, 0xdcdcaa);
//    private static final Palette TOOLTIP_PALETTE = Palette.of(0xffff55, 0xbdf486, 0xf4bd86, 0xdcdcaa);
//    private static final Palette TAG_PALETTE = Palette.of(0x55ff55, 0x41eace, 0x0098ea, 0xdcdcaa);
//
//    private static final Style SPACE = Style.EMPTY;
//
//    private static final Style NAME_PRIMARY = Style.EMPTY;
//    private static final Style ID_PRIMARY = Style.EMPTY.withColor(0x8d7eed);
//    private static final Style MOD_PRIMARY = Style.EMPTY.withColor(0xffa8f3);
//    private static final Style TOOLTIP_PRIMARY = Style.EMPTY.withColor(0xffe0ad);
//    private static final Style TAG_PRIMARY = Style.EMPTY.withColor(0x9efff4);
//
//    private static final Style NAME_SECONDARY = Style.EMPTY;
//    private static final Style ID_SECONDARY = Style.EMPTY.withColor(0xc7c2e8);
//    private static final Style MOD_SECONDARY = Style.EMPTY.withColor(0xffd1f8);
//    private static final Style TOOLTIP_SECONDARY = Style.EMPTY.withColor(0xfff4e5);
//    private static final Style TAG_SECONDARY = Style.EMPTY.withColor(0xe0fffa);
//
//
//    private static final Style NEGATE = Style.EMPTY.withColor(0xe43b3b);
//    private static final Style LEFTOVER = Style.EMPTY.withColor(0x525252);
//    //    private static final Style LEFTOVER = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
//    private static final Style ERROR = Style.EMPTY.withColor(0x4d0000).withUnderlined(true).withItalic(true);
//
//    private static final Style REGEX_SLASH = Style.EMPTY.withColor(0xd16969);
//
//    private static final Style REGEX_RED = Style.EMPTY.withColor(0xd16969);
//    private static final Style REGEX_ORANGE = Style.EMPTY.withColor(0xce9178);
//    private static final Style REGEX_YELLOW = Style.EMPTY.withColor(0xd7ba7d);
//    private static final Style REGEX_LIGHT_YELLOW = Style.EMPTY.withColor(0xdcdcaa);
//
//    public static Style getStyle(Token token) {
//        if(token.attributeType() == Token.AttributeType.OTHER)
//            return getSpecialStyle(token);
//
//        Palette palette = switch (token.attributeType()){
//            case NAME -> NAME_PALETTE;
//            case NAME_REGEX -> NAME_REGEX_PALETTE;
//            case MOD -> MOD_PALETTE;
//            case ID -> ID_PALETTE;
//            case TOOLTIP -> TOOLTIP_PALETTE;
//            case TAG -> TAG_PALETTE;
//            default -> throw new IllegalStateException("Unexpected value: " + token.attributeType());
//        };
////        f(int color, int regex, int escaped, int regexSpecial)
//        return switch (token.secondary()) {
//            case QUOTE -> palette.color();
//            case REGEX_SLASH -> palette.color();
//            case LITERAL -> palette.color();
//            case REGEX -> palette.regex();
//            case REGEX_SPECIAL -> palette.regexSpecial();
//            case REGEX_GROUPING -> palette.regexSpecial();
//            case REGEX_ESCAPED -> palette.escaped();
//            case PREFIX -> palette.color();
//            default -> throw new IllegalStateException("Unexpected value: " + token.secondary());
//        };
//    }
//
//    private static Style getSpecialStyle(Token token) {
//        return switch (token.secondary()){
//            case NEGATE -> NEGATE;
//            case SPACE -> SPACE;
//            case LEFTOVER -> LEFTOVER;
//            default -> throw new IllegalStateException("Unexpected value: " + token.secondary());
//        };
//    }
//
////    private static Style getAttributePrimary(Token.Type type) {
////        return switch (type) {
////            case NAME -> NAME_PRIMARY;
////            case MOD -> MOD_PRIMARY;
////            case ID -> ID_PRIMARY;
////            case TOOLTIP -> TOOLTIP_PRIMARY;
////            case TAG -> TAG_PRIMARY;
////            default -> ERROR;
////        };
////    }
////
////    private static Style getAttributeSecondary(Token.Type type) {
////        return switch (type) {
////            case NAME -> NAME_SECONDARY;
////            case MOD -> MOD_SECONDARY;
////            case ID -> ID_SECONDARY;
////            case TOOLTIP -> TOOLTIP_SECONDARY;
////            case TAG -> TAG_SECONDARY;
////            default -> ERROR;
////        };
////    }
//
//    record Palette(Style color, Style regex, Style escaped, Style regexSpecial) {
//        public static Palette of(int color, int regex, int escaped, int regexSpecial) {
//            return new Palette(
//                    Style.EMPTY.withColor(color),
//                    Style.EMPTY.withColor(regex),
//                    Style.EMPTY.withColor(escaped),
//                    Style.EMPTY.withColor(regexSpecial)
//            );
//        }
//    }
//
//    ;
//}
