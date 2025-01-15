package net.natte.re_search.query;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;

public record Word(Attribute attribute, String content, boolean isRegex, boolean isNegate) {

    public static final StreamCodec<FriendlyByteBuf, Word> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(Attribute.class),
            w -> w.attribute,
            ByteBufCodecs.STRING_UTF8,
            w -> w.content,
            ByteBufCodecs.BOOL,
            w -> w.isRegex,
            ByteBufCodecs.BOOL,
            w -> w.isNegate,
            Word::new
    );

    public Attribute attribute() {
        return attribute;
    }

    public static Word fromMatch(MatchResult match) {

        boolean isNegate = !match.group("negate").isEmpty();

        Attribute attribute = Attribute.fromPrefix(match.group("prefix"));

        boolean isRegex = true;
        String content = match.group("regex");
        if (content == null) {
            content = match.group("slashregex");
        }
        if (content == null) {
            content = match.group("text");
            isRegex = false;

            char quote = match.group("quote").charAt(0);
            content = QueryParser.ESCAPE_PATTERN.matcher(content).replaceAll(matchResult -> {
                String group = matchResult.group();
                char c = group.charAt(1);
                if (c == quote)
                    return Matcher.quoteReplacement(String.valueOf(quote));
                if (QueryParser.ALLOWED_ESCAPES_IN_LITERAL.indexOf(c) != -1) {
                    return Matcher.quoteReplacement(String.valueOf(QueryParser.CHARACTER_ESCAPES.get(c)));
                }
                return Matcher.quoteReplacement(group);
            });

        }

        return new Word(attribute, content, isRegex, isNegate);
    }

    public Component toComponent() {
        return Component.literal(
                (isNegate ? "not " : "") + (attribute.name()) + " " + (isRegex ? '/' : '"') + content + (isRegex ? '/' : '"')
        );
    }
}
