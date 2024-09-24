package net.natte.re_search.query;

import net.minecraft.network.chat.Component;

import java.util.regex.MatchResult;

public class Word {

    private final Attribute attribute;
    private final String content;
    private final boolean isRegex;
    private final boolean isNegate;

    private Word(Attribute attribute, String content, boolean isRegex, boolean isNegate) {
        this.attribute = attribute;
        this.content = content;
        this.isRegex = isRegex;
        this.isNegate = isNegate;
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
        }

        return new Word(attribute, content, isRegex, isNegate);
    }

    public Component toComponent() {
        return Component.literal(
                (isNegate ? "not " : "") + (attribute.name()) + " " + (isRegex ? '/' : '"') + content + (isRegex ? '/' : '"')
        );
    }
}
