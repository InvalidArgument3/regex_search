package natte.re_search.network;

import java.util.ArrayList;
import java.util.List;

import natte.re_search.RegexSearch;
import natte.re_search.search.MarkedInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ItemSearchResultS2CPayload(List<MarkedInventory> inventories) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ItemSearchResultS2CPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RegexSearch.MOD_ID, "item_search_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemSearchResultS2CPayload> STREAM_CODEC =
            StreamCodec.of(ItemSearchResultS2CPayload::encode, ItemSearchResultS2CPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ItemSearchResultS2CPayload payload) {
        buf.writeInt(payload.inventories.size());
        for (MarkedInventory inventory : payload.inventories) {
            buf.writeBlockPos(inventory.blockPos);
            buf.writeInt(inventory.containers.size());
            for (ItemStack stack : inventory.containers) {
                ItemStack.STREAM_CODEC.encode(buf, stack);
            }
            buf.writeInt(inventory.inventory.size());
            for (ItemStack stack : inventory.inventory) {
                ItemStack.STREAM_CODEC.encode(buf, stack);
            }
        }
    }

    private static ItemSearchResultS2CPayload decode(RegistryFriendlyByteBuf buf) {
        List<MarkedInventory> list = new ArrayList<>();
        int inventoriesSize = buf.readInt();
        for (int i = 0; i < inventoriesSize; ++i) {
            BlockPos blockPos = buf.readBlockPos();
            MarkedInventory inventory = new MarkedInventory(blockPos);
            int containersSize = buf.readInt();
            for (int j = 0; j < containersSize; ++j) {
                inventory.addContainer(ItemStack.STREAM_CODEC.decode(buf));
            }
            int inventorySize = buf.readInt();
            for (int j = 0; j < inventorySize; ++j) {
                inventory.addItem(ItemStack.STREAM_CODEC.decode(buf));
            }
            list.add(inventory);
        }
        return new ItemSearchResultS2CPayload(list);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
