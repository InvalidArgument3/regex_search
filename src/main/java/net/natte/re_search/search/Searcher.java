package net.natte.re_search.search;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.natte.re_search.config.Config;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Searcher {

    private static int totalItems;

    public static List<MarkedInventory> search(SearchOptions searchOptions, ServerPlayer playerEntity) {

        List<MarkedInventory> inventories = new ArrayList<>();
        totalItems = 0;

        Player player = playerEntity;
        Level world = player.level();

        Filter filter = new Filter(searchOptions, playerEntity);
        int range = Config.range;
        if (Config.searchBlocks) {
            for (BlockPos blockPos : BlockPos.withinManhattan(player.blockPosition(), range, range, range)) {
                if (inventories.size() == Config.maxInventories)
                    break;
                MarkedInventory markedInventory = new MarkedInventory(blockPos.immutable());
                search(blockPos, world, filter, markedInventory, Config.recursionLimit);

                if (!markedInventory.isEmpty()) {
                    inventories.add(markedInventory);
                }

            }
        }

        if (Config.searchEntities) {
            List<Entity> entities = world.getEntities(player,
                    AABB.ofSize(player.position(), range * 2, range * 2, range * 2));

            entities.sort(Comparator.comparing(entity -> entity.distanceToSqr(player)));

            for (Entity entity : entities) {
                if (inventories.size() == Config.maxInventories)
                    break;

                MarkedInventory markedInventory = new MarkedInventory(entity.blockPosition());
                search(entity, filter, markedInventory, Config.recursionLimit);

                if (!markedInventory.isEmpty()) {
                    inventories.add(markedInventory);
                }
            }
        }

        return inventories;
    }

    private static boolean search(ItemStack itemStack, Filter predicate, MarkedInventory markedInventory,
                                  int recursionDepth) {
        if (itemStack.isEmpty())
            return false;

        if (markedInventory.inventory().size() == Config.maxSearchResultsPerInventory)
            return false;
        if (totalItems == Config.maxSearchResults)
            return false;

        if (recursionDepth == 0)
            return false;

        boolean foundAny = false;
        boolean itemStackMatches = predicate.test(itemStack);
        if (itemStackMatches) {
            markedInventory.inventory().add(itemStack);
            ++totalItems;
        }

        IItemHandler itemHandler = itemStack.getCapability(Capabilities.ItemHandler.ITEM);
        if (itemHandler != null) {
            int items = itemHandler.getSlots();
            for (int i = 0; i < items; ++i) {
                if (search(itemHandler.getStackInSlot(i), predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
        }

        if (foundAny)
            markedInventory.addContainer(itemStack);
        if (itemStackMatches)
            foundAny = true;

        return foundAny;
    }

    private static boolean search(BlockPos blockPos, Level world, Filter predicate,
                                  MarkedInventory markedInventory, int recursionDepth) {
        if (recursionDepth == 0)
            return false;

        boolean foundAny = false;

        BlockState blockState = world.getBlockState(blockPos);

        IItemHandler itemHandler = world.getCapability(Capabilities.ItemHandler.BLOCK, blockPos, null);
        if (itemHandler != null) {
            int items = itemHandler.getSlots();
            for (int i = 0; i < items; ++i) {
                if (search(itemHandler.getStackInSlot(i), predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
        }

        if (foundAny)
            markedInventory.addContainer(blockState.getBlock().asItem().getDefaultInstance());

        return foundAny;
    }

    private static boolean search(Entity entity, Filter predicate, MarkedInventory markedInventory,
                                  int recursionDepth) {
        if (recursionDepth == 0)
            return false;
        boolean foundAny = false;

        // item entity
        if (entity instanceof ItemEntity itemEntity) {

            if (search(itemEntity.getItem(), predicate, markedInventory, recursionDepth - 1)) {
                foundAny = true;
            }
        }

        // item frame
        if (entity instanceof ItemFrame itemFrame) {
            if (search(itemFrame.getItem(), predicate, markedInventory, recursionDepth - 1)) {
                foundAny = true;
                markedInventory.addContainer(Items.ITEM_FRAME.getDefaultInstance());
            }
        }

        // armor stand
        else if (entity instanceof ArmorStand armorStand) {
            for (ItemStack itemStack : armorStand.getHandSlots()) {
                if (search(itemStack, predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
            for (ItemStack itemStack : armorStand.getArmorSlots()) {
                if (search(itemStack, predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
            if (foundAny) {
                markedInventory.addContainer(Items.ARMOR_STAND.getDefaultInstance());
            }
        } else if (entity instanceof ContainerEntity vehicleInventory) {
            for (ItemStack itemStack : vehicleInventory.getItemStacks()) {
                if (search(itemStack, predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
            if (foundAny)
                markedInventory.addContainer(entity.getPickResult());
        } else {
            IItemHandler itemHandler = entity.getCapability(Capabilities.ItemHandler.ENTITY);
            if (itemHandler != null) {
                int items = itemHandler.getSlots();
                for (int i = 0; i < items; ++i) {
                    if (search(itemHandler.getStackInSlot(i), predicate, markedInventory, recursionDepth - 1))
                        foundAny = true;
                }
            }
        }
        return foundAny;
    }
}
