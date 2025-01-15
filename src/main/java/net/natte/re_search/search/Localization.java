package net.natte.re_search.search;

import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class Localization {
    private static final Function<Component, String> DEFAULT = Component::getString;

    public static Function<Component, String> getTranslator(String languageCode) {
        // TODO: use (or create?) lib
        return DEFAULT;
    }

}
