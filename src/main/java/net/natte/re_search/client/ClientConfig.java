package net.natte.re_search.client;

import com.google.common.base.Predicates;
import net.natte.re_search.client.screen.SearchHistory;
import net.natte.re_search.search.context.SearchContext;
import net.natte.re_search.search.context.SearchMode;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<SearchMode> SEARCH_MODE = BUILDER.define("searchMode", SearchMode.REGEX);
    private static final ModConfigSpec.ConfigValue<KeepMode> KEEP_MODE = BUILDER.define("keepMode", KeepMode.AUTO_SELECT);
    private static final ModConfigSpec.ConfigValue<SearchContext> SEARCH_CONTEXT = BUILDER.define("searchContext", SearchContext.BLOCKS_AND_ENTITIES);

    private static final ModConfigSpec.BooleanValue IS_CASE_SENSITIVE = BUILDER.define("isCaseSensitive", false);

    private static final ModConfigSpec.IntValue AUTO_HIDE_TIME = BUILDER.defineInRange("autoHideTime", 20, -1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue SEARCH_HISTORY_SIZE = BUILDER.defineInRange("searchHistorySize", 100, 0, 1000);
    private static final ModConfigSpec.IntValue SEARCH_HISTORY_INDEX = BUILDER.defineInRange("searchHistoryIndex", 0, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> SEARCH_HISTORY = BUILDER.defineListAllowEmpty("searchHistory", List.of(), String::new, Predicates.alwaysTrue());

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static void onLoad(ModConfigEvent.Loading event) {
        ClientSettings.searchMode = SEARCH_MODE.get();
        ClientSettings.keepMode = KEEP_MODE.get();
        ClientSettings.searchContext = SEARCH_CONTEXT.get();

        ClientSettings.isCaseSensitive = IS_CASE_SENSITIVE.get();

        ClientSettings.autoHideTime = AUTO_HIDE_TIME.get();

        ClientSettings.searchHistory = new SearchHistory(SEARCH_HISTORY_SIZE.get(), SEARCH_HISTORY_INDEX.get(), SEARCH_HISTORY.get());
    }

    public static void onUnLoad(ModConfigEvent.Unloading event) {
        SEARCH_MODE.set(ClientSettings.searchMode);
        KEEP_MODE.set(ClientSettings.keepMode);
        SEARCH_CONTEXT.set(ClientSettings.searchContext);

        IS_CASE_SENSITIVE.set(ClientSettings.isCaseSensitive);

        AUTO_HIDE_TIME.set(ClientSettings.autoHideTime);

        SEARCH_HISTORY_SIZE.set(ClientSettings.searchHistory.size());
        SEARCH_HISTORY_INDEX.set(ClientSettings.searchHistory.index());
        SEARCH_HISTORY.set(ClientSettings.searchHistory.history());
    }
}
