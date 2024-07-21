package net.natte.re_search.client.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class SearchHistory {

    public static final Codec<SearchHistory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("historySize").forGetter(h -> h.historySize),
                    Codec.INT.fieldOf("historyIndex").forGetter(h -> h.historyIndex),
                    Codec.STRING.listOf().fieldOf("searchHistory").forGetter(h -> h.searchHistory)
            ).apply(instance, SearchHistory::new));

    private final List<String> searchHistory;
    private int historyIndex;
    private final int historySize;

    public SearchHistory(int historySize) {
        this.searchHistory = new ArrayList<>();
        this.historyIndex = 0;
        this.historySize = historySize;
    }

    public SearchHistory(int historySize, int historyIndex, List<? extends String> searchHistory) {
        this.historySize = historySize;
        this.historyIndex = historyIndex;
        this.searchHistory = new ArrayList<>(searchHistory);

        this.historyIndex = Mth.clamp(this.historyIndex, 0, this.searchHistory.size() - 1);
    }

    public String getCurrent() {
        if (searchHistory.isEmpty()) {
            return "";
        }
        if (historyIndex >= searchHistory.size()) {
            return "";
        }
        return searchHistory.get(historyIndex);
    }

    public String getPrevious() {
        if (historyIndex > 0) {
            historyIndex -= 1;
        }
        return getCurrent();
    }

    public String getNext() {
        if (historyIndex + 1 < searchHistory.size()) {
            historyIndex += 1;
            return getCurrent();
        }
        return "";
    }

    public void add(String string) {
        searchHistory.add(string);
        historyIndex += 1;
        if (searchHistory.size() > historySize) {
            searchHistory.removeFirst();
            historyIndex -= 1;
        }
    }

    public void resetPosition() {
        historyIndex = searchHistory.size();
    }

    public int size() {
        return historySize;
    }

    public int index() {
        return historyIndex;
    }

    public List<String> history() {
        return searchHistory;
    }
}
