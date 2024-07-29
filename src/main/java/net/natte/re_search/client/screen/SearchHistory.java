package net.natte.re_search.client.screen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class SearchHistory {

    public static final Codec<SearchHistory> CODEC = Codec.STRING.listOf().xmap(SearchHistory::new, SearchHistory::history);

    private final int historySize = 100;

    private final List<String> searchHistory;
    private int historyIndex = 0;

    public SearchHistory() {
        this.searchHistory = new ArrayList<>();
    }

    public SearchHistory(List<? extends String> searchHistory) {
        this.searchHistory = new ArrayList<>(searchHistory);
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
