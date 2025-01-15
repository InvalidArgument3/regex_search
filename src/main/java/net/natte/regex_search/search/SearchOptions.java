package net.natte.regex_search.search;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.natte.regex_search.search.context.SearchContext;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record SearchOptions(String query, boolean isCaseSensitive, SearchContext searchContext, boolean hasAdvancedTooltips, String languageCode) {

    public static final StreamCodec<FriendlyByteBuf, SearchOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SearchOptions::query,
            ByteBufCodecs.BOOL,
            SearchOptions::isCaseSensitive,
            NeoForgeStreamCodecs.enumCodec(SearchContext.class),
            SearchOptions::searchContext,
            ByteBufCodecs.BOOL,
            SearchOptions::hasAdvancedTooltips,
            ByteBufCodecs.STRING_UTF8,
            SearchOptions::languageCode,
            SearchOptions::new
    );
}
