package net.natte.re_search.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.natte.re_search.RegexSearch;
import net.natte.re_search.client.ClientSettings;
import net.natte.re_search.client.KeepMode;
import net.natte.re_search.client.render.HighlightRenderer;
import net.natte.re_search.network.ItemSearchPacketC2S;
import net.natte.re_search.search.SearchOptions;
import net.natte.re_search.search.context.CaseSensitivity;
import net.natte.re_search.search.context.SearchContext;
import net.natte.re_search.search.context.SearchMode;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class SearchScreen extends Screen {

    private static final ResourceLocation WIDGET_TEXTURE = RegexSearch.ID("textures/gui/widgets.png");

    private final Screen parent;
    private final Minecraft client;

    private EditBox searchBox;

    private SyntaxHighlighter highlighter;


    private static final CycleableOption<CaseSensitivity> caseSensitivity = CycleableOption.create("case_sensitivity", CaseSensitivity.class, 0);
    private static final CycleableOption<SearchContext> SearchContext = CycleableOption.create("search_type", SearchContext.class, 40);
    private static final CycleableOption<SearchMode> searchMode = CycleableOption.create("search_mode", SearchMode.class, 80);
    private static final CycleableOption<KeepMode> keepMode = CycleableOption.create("keep_mode", KeepMode.class, 120);

    public SearchScreen(Screen parent, Minecraft client) {
        super(Component.translatable("screen.re_search.label"));
        this.parent = parent;
        this.client = client;
    }

    @Override
    protected void init() {

        int boxWidth = 120;
        int boxHeight = 18;
        int x = width / 2 - boxWidth / 2;
        int y = height / 2 - boxHeight / 2 + 50;

        int centerX = width / 2;
        int centerY = height / 2;

        searchBox = new EditBox(font, x, y, boxWidth, boxHeight, Component.empty());
        searchBox.setMaxLength(100);

        setInitialFocus(searchBox);
        addRenderableWidget(searchBox);

        highlighter = new SyntaxHighlighter();
        highlighter.setMode(ClientSettings.searchMode);
        searchBox.setFormatter(highlighter::provideRenderText);
        searchBox.setResponder(highlighter::refresh);

        this.addRenderableWidget(
                new TexturedCyclingButtonWidget<>(caseSensitivity.withState(ClientSettings.isCaseSensitive ? CaseSensitivity.SENSITIVE : CaseSensitivity.INSENSITIVE), centerX - 61, centerY + 71, WIDGET_TEXTURE, this::onCaseSensitiveButtonPress));

        this.addRenderableWidget(
                new TexturedCyclingButtonWidget<>(keepMode.withState(ClientSettings.keepMode), centerX - 27, centerY + 71, WIDGET_TEXTURE, this::onKeepModeButtonPress));

        this.addRenderableWidget(
                new TexturedCyclingButtonWidget<>(SearchContext.withState(ClientSettings.searchContext), centerX + 7, centerY + 71, WIDGET_TEXTURE, this::onSearchTypeButtonPress));

        this.addRenderableWidget(new TexturedCyclingButtonWidget<>(searchMode.withState(ClientSettings.searchMode),
                centerX + 41, centerY + 71, WIDGET_TEXTURE, this::onSearchModeButtonPress));

        ClientSettings.searchHistory.resetPosition();
        if (ClientSettings.keepMode != KeepMode.CLEAR) {
            searchBox.setValue(ClientSettings.searchHistory.getPrevious());
            if (ClientSettings.keepMode == KeepMode.AUTO_SELECT) {
                searchBox.setCursorPosition(searchBox.getValue().length());
                searchBox.setHighlightPos(0);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {

            String text = searchBox.getValue();
            if (text.isEmpty()) {
                HighlightRenderer.clear();
            } else {
                PacketDistributor.sendToServer(new ItemSearchPacketC2S(new SearchOptions(text, ClientSettings.isCaseSensitive, ClientSettings.searchMode,
                        ClientSettings.searchContext)));

                ClientSettings.searchHistory.add(text);
                HighlightRenderer.startRender();
            }
            onClose();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_UP) {
            searchBox.setValue(ClientSettings.searchHistory.getPrevious());
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            searchBox.setValue(ClientSettings.searchHistory.getNext());
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        client.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void onCaseSensitiveButtonPress(TexturedCyclingButtonWidget<CaseSensitivity> button) {
        ClientSettings.isCaseSensitive = button.state.next() == CaseSensitivity.SENSITIVE;
        button.refreshTooltip();
    }

    private void onSearchTypeButtonPress(TexturedCyclingButtonWidget<SearchContext> button) {
        ClientSettings.searchContext = button.state.next();
        button.refreshTooltip();
    }

    private void onKeepModeButtonPress(TexturedCyclingButtonWidget<KeepMode> button) {
        ClientSettings.keepMode = button.state.next();
        button.refreshTooltip();
    }

    private void onSearchModeButtonPress(TexturedCyclingButtonWidget<SearchMode> button) {
        ClientSettings.searchMode = button.state.next();
        button.refreshTooltip();

        highlighter.setMode(ClientSettings.searchMode);
        highlighter.refresh(searchBox.getValue());
    }
}
