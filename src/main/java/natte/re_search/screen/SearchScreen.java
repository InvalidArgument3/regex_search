package natte.re_search.screen;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import natte.re_search.RegexSearch;
import natte.re_search.config.Config;
import natte.re_search.network.ItemSearchC2SPayload;
import natte.re_search.render.HighlightRenderer;
import natte.re_search.render.WorldRendering;
import natte.re_search.search.MarkedInventory;
import natte.re_search.search.SearchOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;

public class SearchScreen extends Screen {

    private static final ResourceLocation WIDGET_TEXTURE = ResourceLocation.fromNamespaceAndPath(RegexSearch.MOD_ID,
            "textures/gui/widgets.png");

    private final Screen parent;
    private final Minecraft client;

    private EditBox searchBox;

    private SyntaxHighlighter highlighter;

    private static final SearchHistory searchHistory = new SearchHistory(100);

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
        highlighter.setMode(Config.searchMode);
        searchBox.setFormatter(highlighter::provideRenderText);
        searchBox.setResponder(highlighter::refresh);

        this.addRenderableWidget(new TexturedCyclingButtonWidget<>(
                CaseSensitivity.getSensitivity(Config.isCaseSensitive),
                centerX - 61, centerY + 71, 20, 20, 20, WIDGET_TEXTURE, this::onCaseSensitiveButtonPress));

        KeepMode keepMode = Config.keepLast ? Config.autoSelect ? KeepMode.HIGHLIGHT : KeepMode.KEEP : KeepMode.CLEAR;
        this.addRenderableWidget(
                new TexturedCyclingButtonWidget<>(keepMode, centerX - 27, centerY + 71, 20,
                        20, 20, WIDGET_TEXTURE, this::onKeepModeButtonPress));

        SearchType searchType = Config.searchBlocks ? Config.searchEntities ? SearchType.BOTH : SearchType.BLOCKS
                : SearchType.ENTITIES;
        this.addRenderableWidget(
                new TexturedCyclingButtonWidget<>(searchType, centerX + 7, centerY + 71, 20,
                        20, 20, WIDGET_TEXTURE, this::onSearchTypeButtonPress));

        this.addRenderableWidget(new TexturedCyclingButtonWidget<>(SearchMode.values()[Config.searchMode],
                centerX + 41, centerY + 71, 20, 20, 20, WIDGET_TEXTURE, this::onSearchModeButtonPress));

        searchHistory.resetPosition();
        if (Config.keepLast) {
            searchBox.setValue(searchHistory.getPrevious());
            if (Config.autoSelect) {
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
                WorldRendering.clearMarkedInventories();
                HighlightRenderer.setRenderedItems(new ArrayList<>());
            } else {
                if (client.getConnection() != null) {
                    client.getConnection().send(new ServerboundCustomPayloadPacket(
                            new ItemSearchC2SPayload(new SearchOptions(text, Config.isCaseSensitive, Config.searchMode,
                                    Config.searchBlocks, Config.searchEntities))));
                }
                searchHistory.add(text);
                HighlightRenderer.startRender();
            }
            onClose();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_UP) {
            searchBox.setValue(searchHistory.getPrevious());
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            searchBox.setValue(searchHistory.getNext());
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        Config.save();
        client.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void onCaseSensitiveButtonPress(TexturedCyclingButtonWidget<CaseSensitivity> button) {
        Config.isCaseSensitive = !Config.isCaseSensitive;
        Config.markDirty();

        button.state = Config.isCaseSensitive ? CaseSensitivity.SENSITIVE : CaseSensitivity.INSENSITIVE;
        button.refreshTooltip();
    }

    private void onSearchTypeButtonPress(TexturedCyclingButtonWidget<SearchType> button) {
        button.state = button.state == SearchType.BLOCKS ? SearchType.ENTITIES
                : button.state == SearchType.ENTITIES ? SearchType.BOTH : SearchType.BLOCKS;

        Config.searchBlocks = button.state.searchBlocks;
        Config.searchEntities = button.state.searchEntities;
        Config.markDirty();

        button.refreshTooltip();
    }

    private void onKeepModeButtonPress(TexturedCyclingButtonWidget<KeepMode> button) {
        button.state = button.state == KeepMode.CLEAR ? KeepMode.KEEP
                : button.state == KeepMode.KEEP ? KeepMode.HIGHLIGHT : KeepMode.CLEAR;

        Config.keepLast = button.state.keepLast;
        Config.autoSelect = button.state.autoSelect;
        Config.markDirty();

        button.refreshTooltip();
    }

    private void onSearchModeButtonPress(TexturedCyclingButtonWidget<SearchMode> button) {

        Config.searchMode = (Config.searchMode + 1) % 3;
        Config.markDirty();

        highlighter.setMode(Config.searchMode);
        highlighter.refresh(searchBox.getValue());
        button.state = SearchMode.values()[Config.searchMode];
        button.refreshTooltip();
    }
}

enum CaseSensitivity implements CycleableOption {
    SENSITIVE("sensitive", 0, 0),
    INSENSITIVE("insensitive", 20, 0);

    private final Component name;
    private final Component info;
    private final int uOffset;
    private final int vOffset;

    CaseSensitivity(String mode, int uOffset, int vOffset) {
        this.name = Component.translatable("option.re_search.case_sensitivity." + mode);
        this.info = Component.translatable("description.re_search.case_sensitivity." + mode);
        this.uOffset = uOffset;
        this.vOffset = vOffset;
    }

    @Override
    public int uOffset() {
        return this.uOffset;
    }

    @Override
    public int vOffset() {
        return this.vOffset;
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public Component getInfo() {
        return this.info;
    }

    public static CaseSensitivity getSensitivity(boolean isCaseSensitive) {
        return isCaseSensitive ? SENSITIVE : INSENSITIVE;
    }
}

enum SearchType implements CycleableOption {
    BOTH("both", true, true, 0, 40),
    BLOCKS("blocks", true, false, 20, 40),
    ENTITIES("entities", false, true, 40, 40);

    public final boolean searchBlocks;
    public final boolean searchEntities;

    private final Component name;
    private final Component info;
    private final int uOffset;
    private final int vOffset;

    SearchType(String mode, boolean searchBlocks, boolean searchEntities, int uOffset, int vOffset) {

        this.name = Component.translatable("option.re_search.search_type." + mode);
        this.info = Component.translatable("description.re_search.search_type." + mode);
        this.searchBlocks = searchBlocks;
        this.searchEntities = searchEntities;
        this.uOffset = uOffset;
        this.vOffset = vOffset;
    }

    @Override
    public int uOffset() {
        return this.uOffset;
    }

    @Override
    public int vOffset() {
        return this.vOffset;
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public Component getInfo() {
        return this.info;
    }
}

enum SearchMode implements CycleableOption {
    REGEX("regex", 0, 80),
    LITERAL("literal", 20, 80),
    EXTENDED("extended", 40, 80);

    private final Component name;
    private final Component info;
    private final int uOffset;
    private final int vOffset;

    SearchMode(String mode, int uOffset, int vOffset) {
        this.name = Component.translatable("option.re_search.search_mode." + mode);
        this.info = Component.translatable("description.re_search.search_mode." + mode);
        this.uOffset = uOffset;
        this.vOffset = vOffset;
    }

    @Override
    public int uOffset() {
        return this.uOffset;
    }

    @Override
    public int vOffset() {
        return this.vOffset;
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public Component getInfo() {
        return this.info;
    }
}

enum KeepMode implements CycleableOption {
    CLEAR("clear", false, false, 0, 120),
    KEEP("keep", true, false, 20, 120),
    HIGHLIGHT("keep_highlight", true, true, 40, 120);

    public final boolean keepLast;
    public final boolean autoSelect;

    private final Component name;
    private final Component info;
    private final int uOffset;
    private final int vOffset;

    KeepMode(String mode, boolean keepLast, boolean autoSelect, int uOffset, int vOffset) {
        this.keepLast = keepLast;
        this.autoSelect = autoSelect;

        this.name = Component.translatable("option.re_search.keep_mode." + mode);
        this.info = Component.translatable("description.re_search.keep_mode." + mode);
        this.uOffset = uOffset;
        this.vOffset = vOffset;
    }

    @Override
    public int uOffset() {
        return this.uOffset;
    }

    @Override
    public int vOffset() {
        return this.vOffset;
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public Component getInfo() {
        return this.info;
    }
}
