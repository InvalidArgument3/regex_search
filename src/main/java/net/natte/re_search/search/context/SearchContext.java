package net.natte.re_search.search.context;

public enum SearchContext {
    BLOCKS_AND_ENTITIES,
    BLOCKS,
    ENTITIES;

    public boolean doesSearchBlocks() {
        return this != ENTITIES;
    }

    public boolean doesSearchEntities() {
        return this != BLOCKS;
    }
}
