package net.natte.re_search.search;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.natte.re_search.search.context.SearchContext;
import net.natte.re_search.search.context.SearchMode;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record SearchOptions(String expression, boolean isCaseSensitive, SearchMode searchMode,
                            SearchContext searchContext) {

    public static final StreamCodec<FriendlyByteBuf, SearchOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SearchOptions::expression,
            ByteBufCodecs.BOOL,
            SearchOptions::isCaseSensitive,
            NeoForgeStreamCodecs.enumCodec(SearchMode.class),
            SearchOptions::searchMode,
            NeoForgeStreamCodecs.enumCodec(SearchContext.class),
            SearchOptions::searchContext,
            SearchOptions::new
    );
}
