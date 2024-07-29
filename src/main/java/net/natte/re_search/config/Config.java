package net.natte.re_search.config;

import net.natte.re_search.search.context.SearchContext;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    public static int range = 12;
    public static int recursionLimit = -1;

    public static int maxInventories = 10;
    public static int maxSearchResults = -1;
    public static int maxSearchResultsPerInventory = 81;

    public static SearchContext searchContext = SearchContext.BLOCKS_AND_ENTITIES;


    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();


    private static final ModConfigSpec.IntValue RANGE = BUILDER.defineInRange("range", 12, -1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue RECURSION_LIMIT = BUILDER.defineInRange("recursionLimit", -1, -1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MAX_INVENTORIES = BUILDER.defineInRange("maxInventories", 10, -1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue MAX_SEARCH_RESULTS = BUILDER.defineInRange("maxSearchResults", -1, -1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue MAX_SEARCH_RESULTS_PER_INVENTORY = BUILDER.defineInRange("maxSearchResultsPerInventory", 81, -1, Integer.MAX_VALUE);

    private static final ModConfigSpec.ConfigValue<SearchContext> SEARCH_CONTEXT = BUILDER.defineEnum("searchContext", SearchContext.BLOCKS_AND_ENTITIES);


    public static final ModConfigSpec SPEC = BUILDER.build();

    public static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC && !(event instanceof ModConfigEvent.Unloading))
            loadConfig();
    }

    public static void loadConfig() {
        range = RANGE.get();
        recursionLimit = RECURSION_LIMIT.get();

        maxInventories = MAX_INVENTORIES.get();
        maxSearchResults = MAX_SEARCH_RESULTS.get();
        maxSearchResultsPerInventory = MAX_SEARCH_RESULTS_PER_INVENTORY.get();

        searchContext = SEARCH_CONTEXT.get();
    }
}
