package net.natte.re_search.config;

import net.natte.re_search.RegexSearch;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    public static int range = 12;
    public static int recursionLimit = -1;

    public static int maxInventories = 10;
    public static int maxSearchResults = -1;
    public static int maxSearchResultsPerInventory = 81;

    public static boolean searchBlocks = true;
    public static boolean searchEntities = true;


    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();


    private static final ModConfigSpec.IntValue RANGE = BUILDER.defineInRange("range", 12, -1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue RECURSION_LIMIT = BUILDER.defineInRange("recursionLimit", -1, -1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MAX_INVENTORIES = BUILDER.defineInRange("maxInventories", 10, -1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue MAX_SEARCH_RESULTS = BUILDER.defineInRange("maxSearchResults", -1, -1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue MAX_SEARCH_RESULTS_PER_INVENTORY = BUILDER.defineInRange("maxSearchResultsPerInventory", 81, -1, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue DO_SEARCH_BLOCKS = BUILDER.define("doSearchBlocks", true);
    private static final ModConfigSpec.BooleanValue DO_SEARCH_ENTITIES = BUILDER.define("doSearchEntities", true);

    public static final ModConfigSpec SPEC = BUILDER.build();


    public static void onLoad(ModConfigEvent.Loading event) {
        loadConfig();
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        loadConfig();
    }

    public static void onUnLoad(ModConfigEvent.Unloading event) {
        saveConfig();
    }

    public static void loadConfig() {
        range = RANGE.get();
        recursionLimit = RECURSION_LIMIT.get();

        maxInventories = MAX_INVENTORIES.get();
        maxSearchResults = MAX_SEARCH_RESULTS.get();
        maxSearchResultsPerInventory = MAX_SEARCH_RESULTS_PER_INVENTORY.get();

        searchBlocks = DO_SEARCH_BLOCKS.get();
        searchEntities = DO_SEARCH_ENTITIES.get();
    }

    public static void saveConfig() {
        RANGE.set(range);
        RECURSION_LIMIT.set(recursionLimit);

        MAX_INVENTORIES.set(maxInventories);
        MAX_SEARCH_RESULTS.set(maxSearchResults);
        MAX_SEARCH_RESULTS_PER_INVENTORY.set(maxSearchResultsPerInventory);

        DO_SEARCH_BLOCKS.set(searchBlocks);
        DO_SEARCH_ENTITIES.set(searchEntities);
    }
}
