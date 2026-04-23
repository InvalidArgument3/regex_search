package net.natte.regex_search.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.natte.regex_search.RegexSearch;
import net.natte.regex_search.search.MarkedInventory;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.function.Consumer;

public record ItemSearchResultPacketS2C(List<MarkedInventory> inventories) implements CustomPacketPayload {
    public static final Type<ItemSearchResultPacketS2C> TYPE = new Type<>(RegexSearch.ID("item_search_result"));
    private static Consumer<List<MarkedInventory>> clientReceiver = ignored -> {
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemSearchResultPacketS2C> STREAM_CODEC = StreamCodec.composite(
            MarkedInventory.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ItemSearchResultPacketS2C::inventories,
            ItemSearchResultPacketS2C::new
    );

    @Override
    public Type<ItemSearchResultPacketS2C> type() {
        return TYPE;
    }

    public static void setClientReceiver(Consumer<List<MarkedInventory>> receiver) {
        clientReceiver = receiver;
    }

    public static void receive(ItemSearchResultPacketS2C packet, IPayloadContext context){
        clientReceiver.accept(packet.inventories);
    }
}
