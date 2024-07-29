package net.natte.re_search.client;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue AUTO_HIDE_TIME = BUILDER
            .comment("How many seconds found items are rendered, -1 for no limit. (search \"\" to stop rendering)")
            .translation("clientconfig.re_search.auto_hide_time")
            .defineInRange("autoHideTime", 20, -1, Integer.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC)
            ClientSettings.autoHideTime = AUTO_HIDE_TIME.get();
    }
}
