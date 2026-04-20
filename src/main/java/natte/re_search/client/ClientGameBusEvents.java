package natte.re_search.client;

import natte.re_search.RegexSearch;
import natte.re_search.screen.SearchScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import natte.re_search.render.HighlightRenderer;
import natte.re_search.render.WorldRendering;

@EventBusSubscriber(modid = RegexSearch.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientGameBusEvents {

    private ClientGameBusEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ClientModBusEvents.SEARCH_KEY != null && ClientModBusEvents.SEARCH_KEY.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new SearchScreen(mc.screen, mc));
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        HighlightRenderer.onRenderGui(event.getGuiGraphics(),
                event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            HighlightRenderer.onRenderWorld(event.getPoseStack(), event.getProjectionMatrix(), event.getCamera(),
                    event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            WorldRendering.renderOldHighlighter(event);
        }
    }
}
