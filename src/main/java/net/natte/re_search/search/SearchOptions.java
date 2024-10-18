package net.natte.re_search.search;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.natte.re_search.query.Word;
import net.natte.re_search.search.context.SearchContext;
import net.natte.re_search.search.context.SearchMode;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.List;

// TODO: add language code
public record SearchOptions(String query, boolean isCaseSensitive, SearchContext searchContext, boolean hasAdvancedTooltips) {

    public static final StreamCodec<FriendlyByteBuf, SearchOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SearchOptions::query,
            ByteBufCodecs.BOOL,
            SearchOptions::isCaseSensitive,
            NeoForgeStreamCodecs.enumCodec(SearchContext.class),
            SearchOptions::searchContext,
            ByteBufCodecs.BOOL,
            SearchOptions::hasAdvancedTooltips,
            SearchOptions::new
    );
}
