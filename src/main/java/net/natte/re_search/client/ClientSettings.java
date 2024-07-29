package net.natte.re_search.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Unit;
import net.natte.re_search.RegexSearch;
import net.natte.re_search.client.screen.SearchHistory;
import net.natte.re_search.search.context.SearchContext;
import net.natte.re_search.search.context.SearchMode;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientSettings {
    public static SearchMode searchMode = SearchMode.REGEX;
    public static boolean isCaseSensitive = false;

    public static SearchContext searchContext = SearchContext.BLOCKS_AND_ENTITIES;

    public static KeepMode keepMode = KeepMode.AUTO_SELECT;

    public static int autoHideTime = 20;

    public static SearchHistory searchHistory = new SearchHistory();

    private static final Codec<Unit> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.xmap(SearchMode::valueOf, SearchMode::name).fieldOf("searchMode").forGetter(u -> searchMode),
                    Codec.STRING.xmap(SearchContext::valueOf, SearchContext::name).fieldOf("searchContext").forGetter(u -> searchContext),
                    Codec.STRING.xmap(KeepMode::valueOf, KeepMode::name).fieldOf("keepMode").forGetter(u -> keepMode),
                    Codec.BOOL.fieldOf("isCaseSensitive").forGetter(u -> isCaseSensitive),
                    SearchHistory.CODEC.fieldOf("searchHistory").forGetter(u -> searchHistory)
            ).apply(instance, ClientSettings::fromCodec)
    );

    private static Unit fromCodec(SearchMode searchMode, SearchContext searchContext, KeepMode keepMode, boolean isCaseSensitive, SearchHistory searchHistory) {
        ClientSettings.searchMode = searchMode;
        ClientSettings.searchContext = searchContext;
        ClientSettings.keepMode = keepMode;
        ClientSettings.isCaseSensitive = isCaseSensitive;
        ClientSettings.searchHistory = searchHistory;
        return Unit.INSTANCE;
    }

    public static void save() {
        DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, Unit.INSTANCE);
        CompoundTag tag = (CompoundTag) result.getOrThrow();
        try {
            NbtIo.write(tag, path());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load() {
        CompoundTag tag;
        try {
            tag = NbtIo.read(path());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (tag != null)
            CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    private static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve("re_search_client_settings.nbt");
    }
}
