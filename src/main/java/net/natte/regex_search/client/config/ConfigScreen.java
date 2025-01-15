package net.natte.regex_search.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigScreen extends ConfigurationScreen.ConfigurationSectionScreen {
    public ConfigScreen(Screen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
        super(parent, type, modConfig, title);
    }

    public static Screen createConfigScreen(ModContainer modContainer, Screen parent) {
        return new ConfigurationScreen(modContainer, parent, ConfigScreen::new);
    }

    @Override
    public void render(GuiGraphics graphics, int p_281550_, int p_282878_, float p_282465_) {
        super.render(graphics, p_281550_, p_282878_, p_282465_);
        graphics.drawString(Minecraft.getInstance().font, Component.literal("hello"), 100, 100, 0xffffff);
    }

    @Nullable
    @Override
    protected Element createIntegerValue(String key, ModConfigSpec.ValueSpec spec, Supplier<Integer> source, Consumer<Integer> target) {
        if (key.equals("selectedColorTheme"))
            return null;
        return super.createIntegerValue(key, spec, source, target);
    }

    @Nullable
    @Override
    protected <T> Element createList(String key, ModConfigSpec.ListValueSpec spec, ModConfigSpec.ConfigValue<List<T>> list) {
        return super.createList(key, spec, list);
    }
}
