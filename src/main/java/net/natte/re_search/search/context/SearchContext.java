package net.natte.re_search.search.context;

public enum SearchContext {
    BLOCKS,
    ENTITIES,
    BLOCKS_AND_ENTITIES;

    public boolean doesSearchBlocks() {
        return this != ENTITIES;
    }

    public boolean doesSearchEntities() {
        return this != BLOCKS;
    }
}
