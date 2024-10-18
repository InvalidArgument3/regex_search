package net.natte.re_search.client;

import net.natte.re_search.client.ClientSettings;
import net.natte.re_search.client.ColorTheme;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue AUTO_HIDE_TIME = BUILDER
            .comment("How many seconds found items are rendered, -1 for no limit. (search \"\" to stop rendering)")
            .translation("clientconfig.re_search.auto_hide_time")
            .defineInRange("autoHideTime", 20, -1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue SELECTED_COLOR_THEME = BUILDER
            .defineInRange("selectedColorTheme", -1, -1, Integer.MAX_VALUE);

    private static final ModConfigSpec.ConfigValue<List<? extends ColorTheme>> CUSTOM_COLOR_THEMES = BUILDER
            .defineListAllowEmpty("colorThemes", ArrayList::new, () -> ColorTheme.DEFAULT, ColorTheme.class::isInstance);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC)
            ClientSettings.autoHideTime = AUTO_HIDE_TIME.get();
    }
}
