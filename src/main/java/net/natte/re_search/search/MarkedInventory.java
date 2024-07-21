package net.natte.re_search.search;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record MarkedInventory(BlockPos blockPos, List<ItemStack> inventory, List<ItemStack> containers) {

    public static final StreamCodec<RegistryFriendlyByteBuf, MarkedInventory> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            MarkedInventory::blockPos,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,
            MarkedInventory::inventory,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,
            MarkedInventory::containers,
            MarkedInventory::new
    );

    public MarkedInventory(BlockPos blockPos){
        this(blockPos, new ArrayList<>(), new ArrayList<>());
    }

    public void addItem(ItemStack itemStack){
        this.inventory.add(itemStack);
    }

    public void addContainer(ItemStack itemStack){
        this.containers.add(itemStack);
    }

    public boolean isEmpty(){
        return inventory.isEmpty();
    }
}
