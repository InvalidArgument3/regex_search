package net.natte.re_search.config;

import com.google.gson.annotations.Expose;

public class Config {


    @Expose public static int range = 12;
    @Expose public static int recursionLimit = -1;

    @Expose public static int maxInventories = 10;
    @Expose public static int maxSearchResults = -1;
    @Expose public static int maxSearchResultsPerInventory = 81;

    @Expose public static boolean searchBlocks = true;
    @Expose public static boolean searchEntities = true;

}
