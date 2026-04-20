package natte.re_search.config;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import natte.re_search.RegexSearch;
import net.neoforged.fml.loading.FMLPaths;

public abstract class ConfigManager {

    public static Class<?> configClass;
    private static Path path;

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT).setPrettyPrinting().create();

    private static boolean dirty = false;

    public static void init(Class<?> config) {
        configClass = config;
        read();
    }

    public static void read() {
        path = FMLPaths.CONFIGDIR.get().resolve(RegexSearch.MOD_ID + ".json");
        try {
            gson.fromJson(Files.newBufferedReader(path), configClass);
        } catch (Exception e) {
            write();
        }
    }

    public static void write() {
        path = FMLPaths.CONFIGDIR.get().resolve(RegexSearch.MOD_ID + ".json");
        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            Files.write(path, gson.toJson(configClass.getDeclaredConstructor().newInstance()).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void markDirty() {
        dirty = true;
    }

    public static void writeIfDirty() {
        if (dirty) {
            write();
            dirty = false;
        }
    }

    public static void save() {
        writeIfDirty();
    }

    public static boolean isDirty() {
        return dirty;
    }
}
