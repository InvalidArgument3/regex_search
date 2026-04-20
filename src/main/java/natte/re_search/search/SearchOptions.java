package natte.re_search.search;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SearchOptions {

    public String expression;
    public boolean isCaseSensitive;
    public int searchMode;
    public boolean searchBlocks;
    public boolean searchEntities;

    public SearchOptions(String expression, boolean isCaseSensitive, int searchMode, boolean searchBlocks,
            boolean searchEntities) {
        this.expression = expression;
        this.isCaseSensitive = isCaseSensitive;
        this.searchMode = searchMode;
        this.searchBlocks = searchBlocks;
        this.searchEntities = searchEntities;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SearchOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            o -> o.expression,
            ByteBufCodecs.BOOL,
            o -> o.isCaseSensitive,
            ByteBufCodecs.INT,
            o -> o.searchMode,
            ByteBufCodecs.BOOL,
            o -> o.searchBlocks,
            ByteBufCodecs.BOOL,
            o -> o.searchEntities,
            SearchOptions::new);
}
