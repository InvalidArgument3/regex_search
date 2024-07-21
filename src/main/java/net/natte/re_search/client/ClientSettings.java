package net.natte.re_search.client;

import net.natte.re_search.client.screen.SearchHistory;
import net.natte.re_search.search.context.SearchContext;
import net.natte.re_search.search.context.SearchMode;

public class ClientSettings {
    public static SearchMode searchMode = SearchMode.REGEX;
    public static boolean isCaseSensitive = false;

    public static SearchContext searchContext = SearchContext.BLOCKS_AND_ENTITIES;

    public static KeepMode keepMode = KeepMode.AUTO_SELECT;

    public static int autoHideTime = 20;

    public static SearchHistory searchHistory = new SearchHistory(100);
}
