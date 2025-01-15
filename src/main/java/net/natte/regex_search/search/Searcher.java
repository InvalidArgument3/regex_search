package net.natte.regex_search.search;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.natte.regex_search.config.Config;
import net.natte.regex_search.search.matcher.ItemStackMatcher;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Searcher {

    private int totalItems;

    private final SearchOptions searchOptions;
    private final ServerPlayer player;
    private final ItemStackMatcher matcher;

    private final int range;
    private final boolean doSearchBlocks;
    private final boolean doSearchEntities;

    private final List<MarkedInventory> resultInventories;


    private Searcher(SearchOptions searchOptions, ServerPlayer player) {
        this.searchOptions = searchOptions;
        this.player = player;
        this.matcher = ItemStackMatcher.create(searchOptions, player);

        this.range = Config.range;
        this.doSearchBlocks = Config.searchContext.doesSearchBlocks() && searchOptions.searchContext().doesSearchBlocks();
        this.doSearchEntities = Config.searchContext.doesSearchEntities() && searchOptions.searchContext().doesSearchEntities();

        resultInventories = new ArrayList<>();
    }

    public static List<MarkedInventory> search(SearchOptions searchOptions, ServerPlayer playerEntity) {
        return new Searcher(searchOptions, playerEntity).search();
    }

    private List<MarkedInventory> search() {

        totalItems = 0;

        if (doSearchBlocks)
            searchBlocks(player.level());

        if (doSearchEntities)
            searchEntities(player.level());

        return resultInventories;
    }

    private void searchBlocks(Level level) {
        for (BlockPos blockPos : BlockPos.withinManhattan(player.blockPosition(), range, range, range)) {
            if (resultInventories.size() == Config.maxInventories)
                break;

            MarkedInventory markedInventory = searchBlock(blockPos, level, Config.recursionLimit);

            if (!markedInventory.isEmpty())
                resultInventories.add(markedInventory);
        }
    }

    private void searchEntities(Level level) {
        List<Entity> entities = level.getEntities(player,
                AABB.ofSize(player.position(), range * 2, range * 2, range * 2));

        entities.sort(Comparator.comparing(entity -> entity.distanceToSqr(player)));

        for (Entity entity : entities) {
            if (resultInventories.size() == Config.maxInventories)
                break;

            MarkedInventory markedInventory = searchEntity(entity, Config.recursionLimit);

            if (!markedInventory.isEmpty())
                resultInventories.add(markedInventory);
        }
    }

    private boolean searchItem(ItemStack itemStack, MarkedInventory markedInventory, int recursionDepth) {
        if (itemStack.isEmpty())
            return false;

        if (markedInventory.inventory().size() == Config.maxSearchResultsPerInventory)
            return false;
        if (totalItems == Config.maxSearchResults)
            return false;

        if (recursionDepth == 0)
            return false;

        boolean foundAny = false;
        boolean itemStackMatches = matcher.test(itemStack);
        if (itemStackMatches) {
            markedInventory.inventory().add(itemStack);
            ++totalItems;
        }

        IItemHandler itemHandler = itemStack.getCapability(Capabilities.ItemHandler.ITEM);
        if (itemHandler != null) {
            int items = itemHandler.getSlots();
            for (int i = 0; i < items; ++i) {
                if (searchItem(itemHandler.getStackInSlot(i), markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
        }

        if (foundAny)
            markedInventory.addContainer(itemStack);
        if (itemStackMatches)
            foundAny = true;

        return foundAny;
    }

    private MarkedInventory searchBlock(BlockPos blockPos, Level world, int recursionDepth) {

        MarkedInventory markedInventory = new MarkedInventory(blockPos.immutable());

        if (recursionDepth == 0)
            return markedInventory;

        boolean foundAny = false;

        IItemHandler itemHandler = world.getCapability(Capabilities.ItemHandler.BLOCK, blockPos, null);
        if (itemHandler != null) {
            int items = itemHandler.getSlots();
            for (int i = 0; i < items; ++i) {
                if (searchItem(itemHandler.getStackInSlot(i), markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
        }

        if (foundAny)
            markedInventory.addContainer(world.getBlockState(blockPos).getBlock().asItem().getDefaultInstance());

        return markedInventory;
    }

    private MarkedInventory searchEntity(Entity entity, int recursionDepth) {

        MarkedInventory markedInventory = new MarkedInventory(entity.blockPosition());

        if (recursionDepth == 0)
            return markedInventory;

        boolean foundAny = false;

        switch (entity) {
            case ItemEntity itemEntity -> {
                searchItem(itemEntity.getItem(), markedInventory, recursionDepth - 1);
            }
            case ItemFrame itemFrame -> {
                if (searchItem(itemFrame.getItem(), markedInventory, recursionDepth - 1))
                    markedInventory.addContainer(Items.ITEM_FRAME.getDefaultInstance());

            }
            case ArmorStand armorStand -> {
                for (ItemStack itemStack : armorStand.getHandSlots()) {
                    if (searchItem(itemStack, markedInventory, recursionDepth - 1))
                        foundAny = true;
                }
                for (ItemStack itemStack : armorStand.getArmorSlots()) {
                    if (searchItem(itemStack, markedInventory, recursionDepth - 1))
                        foundAny = true;
                }
                if (foundAny)
                    markedInventory.addContainer(Items.ARMOR_STAND.getDefaultInstance());
            }
            case ContainerEntity containerEntity -> {
                for (ItemStack itemStack : containerEntity.getItemStacks()) {
                    if (searchItem(itemStack, markedInventory, recursionDepth - 1))
                        foundAny = true;
                }
                if (foundAny)
                    markedInventory.addContainer(entity.getPickResult());
            }
            default -> {
                IItemHandler itemHandler = entity.getCapability(Capabilities.ItemHandler.ENTITY);
                if (itemHandler != null) {
                    int items = itemHandler.getSlots();
                    for (int i = 0; i < items; ++i) {
                        searchItem(itemHandler.getStackInSlot(i), markedInventory, recursionDepth - 1);
                    }
                }
            }
        }
        return markedInventory;
    }
}
