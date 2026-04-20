package natte.re_search.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import natte.re_search.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public class Searcher {

    private static int totalItems;

    public static List<MarkedInventory> search(SearchOptions searchOptions, ServerPlayer player) {

        List<MarkedInventory> inventories = new ArrayList<>();
        totalItems = 0;

        Level world = player.level();
        Filter filter = new Filter(searchOptions, player);
        int range = Config.range;
        if (Config.searchBlocks) {
            BlockPos center = player.blockPosition();
            for (BlockPos blockPos : BlockPos.betweenClosed(center.offset(-range, -range, -range),
                    center.offset(range, range, range))) {
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
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            AABB aabb = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);
            List<Entity> entities = world.getEntities(player, aabb, e -> true);

            entities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)));

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
        if (markedInventory.inventory.size() == Config.maxSearchResultsPerInventory)
            return false;
        if (totalItems == Config.maxSearchResults)
            return false;

        if (recursionDepth == 0)
            return false;

        boolean foundAny = false;
        boolean itemStackMatches = predicate.test(itemStack);
        if (itemStackMatches) {
            markedInventory.inventory.add(itemStack);
            ++totalItems;
        }

        if (itemStack.getItem() instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
                ItemContainerContents contents = itemStack.get(DataComponents.CONTAINER);
                if (contents != null) {
                    for (ItemStack inner : contents.nonEmptyItems()) {
                        if (search(inner, predicate, markedInventory, recursionDepth - 1))
                            foundAny = true;
                    }
                }
            }
        } else if (itemStack.is(Items.BUNDLE)) {
            BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
            if (bundleContents != null) {
                for (ItemStack inner : bundleContents.items()) {
                    if (search(inner, predicate, markedInventory, recursionDepth - 1))
                        foundAny = true;
                }
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

        IItemHandler handler = world.getCapability(Capabilities.ItemHandler.BLOCK, blockPos, null);
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); ++i) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.isEmpty())
                    continue;
                if (search(stack, predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
        } else if (blockState.hasBlockEntity()) {
            BlockEntity tileEntity = world.getBlockEntity(blockPos);

            if (tileEntity instanceof LecternBlockEntity lectern) {
                if (search(lectern.getBook(), predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
        }

        if (blockState.is(Blocks.CAMPFIRE) || blockState.is(Blocks.SOUL_CAMPFIRE)) {
            BlockEntity be = world.getBlockEntity(blockPos);
            if (be instanceof CampfireBlockEntity campfire) {
                for (ItemStack itemStack : campfire.getItems()) {
                    if (search(itemStack, predicate, markedInventory, recursionDepth - 1))
                        foundAny = true;
                }
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

        if (entity instanceof ItemEntity itemEntity) {

            if (search(itemEntity.getItem(), predicate, markedInventory, recursionDepth - 1)) {
                foundAny = true;
            }
        }

        if (entity instanceof ItemFrame itemFrame) {
            if (search(itemFrame.getItem(), predicate, markedInventory, recursionDepth - 1)) {
                foundAny = true;
                markedInventory.addContainer(Items.ITEM_FRAME.getDefaultInstance());
            }
        } else if (entity instanceof ArmorStand armorStand) {
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
        } else if (entity instanceof AbstractMinecartContainer cart) {
            for (int i = 0; i < cart.getContainerSize(); ++i) {
                ItemStack itemStack = cart.getItem(i);
                if (search(itemStack, predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
            if (foundAny)
                markedInventory.addContainer(entity.getPickResult() != null ? entity.getPickResult()
                        : Items.MINECART.getDefaultInstance());
        } else if (entity instanceof ChestBoat boat) {
            for (int i = 0; i < boat.getContainerSize(); ++i) {
                ItemStack itemStack = boat.getItem(i);
                if (search(itemStack, predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
            if (foundAny)
                markedInventory.addContainer(
                        entity.getPickResult() != null ? entity.getPickResult() : Items.OAK_BOAT.getDefaultInstance());
        }
        return foundAny;

    }
}
