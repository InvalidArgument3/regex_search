package net.natte.re_search.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.natte.re_search.RegexSearch;
import net.natte.re_search.client.render.HighlightRenderer;
import net.natte.re_search.client.screen.SearchScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OnlyIn(Dist.CLIENT)
@Mod(value = RegexSearch.MOD_ID, dist = Dist.CLIENT)
public class RegexSearchClient {

    public static final String MOD_ID = "re_search";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Minecraft client = Minecraft.getInstance();

    private static final KeyMapping openSearchScreenKeyBind = new KeyMapping("key.re_search.search", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "category.re_search.keybinds");

    public RegexSearchClient(IEventBus modBus, ModContainer modContainer) {

        modBus.addListener(this::registerKeyBinds);

        NeoForge.EVENT_BUS.addListener(this::onTick);
        NeoForge.EVENT_BUS.addListener(this::onLevelRender);
        NeoForge.EVENT_BUS.addListener(this::onHudRender);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modBus.addListener(ClientConfig::onLoad);
        modBus.addListener(ClientConfig::onUnLoad);
    }

    private void onLevelRender(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL)
            HighlightRenderer.onRenderWorld(event.getPoseStack(), event.getProjectionMatrix(), event.getCamera(), event.getPartialTick());
    }

    private void onHudRender(RenderGuiEvent.Pre event) {
        HighlightRenderer.onRenderGUI(event.getGuiGraphics().pose(), event.getPartialTick().getGameTimeDeltaTicks());
    }

    private void onTick(ClientTickEvent.Post event) {
        while (openSearchScreenKeyBind.consumeClick()) {
            client.setScreen(new SearchScreen(client.screen, client));
        }
    }

    private void registerKeyBinds(RegisterKeyMappingsEvent event) {
        event.register(openSearchScreenKeyBind);
    }
}