package net.natte.regex_search.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.natte.regex_search.RegexSearch;
import net.natte.regex_search.client.render.HighlightRenderer;
import net.natte.regex_search.search.MarkedInventory;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record ItemSearchResultPacketS2C(List<MarkedInventory> inventories) implements CustomPacketPayload {
    public static final Type<ItemSearchResultPacketS2C> TYPE = new Type<>(RegexSearch.ID("item_search_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemSearchResultPacketS2C> STREAM_CODEC = StreamCodec.composite(
            MarkedInventory.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ItemSearchResultPacketS2C::inventories,
            ItemSearchResultPacketS2C::new
    );

    @Override
    public Type<ItemSearchResultPacketS2C> type() {
        return TYPE;
    }

    public static void receive(ItemSearchResultPacketS2C packet, IPayloadContext context){
        HighlightRenderer.setMarkedInventories(packet.inventories);
    }
}
