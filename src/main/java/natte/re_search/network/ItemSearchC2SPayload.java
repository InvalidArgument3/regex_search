package natte.re_search.network;

import natte.re_search.RegexSearch;
import natte.re_search.search.SearchOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ItemSearchC2SPayload(SearchOptions options) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ItemSearchC2SPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RegexSearch.MOD_ID, "item_search"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemSearchC2SPayload> STREAM_CODEC = StreamCodec.composite(
            SearchOptions.STREAM_CODEC,
            ItemSearchC2SPayload::options,
            ItemSearchC2SPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
