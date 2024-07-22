package net.natte.re_search.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.natte.re_search.RegexSearch;
import net.natte.re_search.search.MarkedInventory;
import net.natte.re_search.search.SearchOptions;
import net.natte.re_search.search.Searcher;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record ItemSearchPacketC2S(SearchOptions searchOptions) implements CustomPacketPayload {

    public static final Type<ItemSearchPacketC2S> TYPE = new Type<>(RegexSearch.ID("item_search"));

    public static final StreamCodec<FriendlyByteBuf, ItemSearchPacketC2S> STREAM_CODEC = SearchOptions.STREAM_CODEC.map(ItemSearchPacketC2S::new, ItemSearchPacketC2S::searchOptions);

    @Override
    public Type<ItemSearchPacketC2S> type() {
        return TYPE;
    }

    public static void receive(ItemSearchPacketC2S packet, IPayloadContext context) {

        SearchOptions searchOptions = packet.searchOptions();
        ServerPlayer player = (ServerPlayer) context.player();

        List<MarkedInventory> inventories = Searcher.search(searchOptions, player);

        context.reply(new ItemSearchResultPacketS2C(inventories));

        if (inventories.isEmpty())
            player.displayClientMessage(Component.translatable("popup.re_search.no_matching_items_found"), true);
    }
}
