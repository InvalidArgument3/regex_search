package net.natte.regex_search.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.natte.regex_search.RegexSearch;
import net.natte.regex_search.client.ClientSettings;
import net.natte.regex_search.client.KeepMode;
import net.natte.regex_search.client.RegexSearchClient;
import net.natte.regex_search.client.config.ClientConfig;
import net.natte.regex_search.client.config.ConfigScreen;
import net.natte.regex_search.client.render.HighlightRenderer;
import net.natte.regex_search.network.ItemSearchPacketC2S;
import net.natte.regex_search.search.SearchOptions;
import net.natte.regex_search.search.context.CaseSensitivity;
import net.natte.regex_search.search.context.SearchContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.EnumSet;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class SearchScreen extends Screen {

    private static final ResourceLocation WIDGET_TEXTURE = RegexSearch.ID("textures/gui/widgets.png");

    private final Screen parent;
    private final Minecraft client;

    private EditBox searchBox;

    private SyntaxHighlighter highlighter;


    private static final CycleableOption<CaseSensitivity> caseSensitivity = CycleableOption.create("case_sensitivity", CaseSensitivity.class, 0);
    private static final CycleableOption<SearchContext> SearchContext = CycleableOption.create("search_type", SearchContext.class, 40);
    private static final CycleableOption<KeepMode> keepMode = CycleableOption.create("keep_mode", KeepMode.class, 80);

    public SearchScreen(Screen parent, Minecraft client) {
        super(Component.translatable("screen.regex_search.label"));
        this.parent = parent;
        this.client = client;
    }

    @Override
    protected void init() {

        int boxWidth = 200;
        int boxHeight = 20;
        int x = width / 2 - boxWidth / 2;
        int y = height / 2 - boxHeight / 2 + 50;

        int centerX = width / 2;
        int centerY = height / 2;

        searchBox = new EditBox(font, x, y, boxWidth, boxHeight, Component.empty());
        searchBox.setMaxLength(100);

        setInitialFocus(searchBox);
        addRenderableWidget(searchBox);

        highlighter = new SyntaxHighlighter();
        searchBox.setFormatter(highlighter::provideRenderText);
        searchBox.setResponder(highlighter::refresh);

        this.addRenderableWidget(
                new TexturedCyclingButtonWidget<>(caseSensitivity.withState(ClientSettings.isCaseSensitive ? CaseSensitivity.SENSITIVE : CaseSensitivity.INSENSITIVE), centerX - 61, centerY + 71, WIDGET_TEXTURE, this::onCaseSensitiveButtonPress));

        this.addRenderableWidget(
                new TexturedCyclingButtonWidget<>(keepMode.withState(ClientSettings.keepMode), centerX - 27, centerY + 71, WIDGET_TEXTURE, this::onKeepModeButtonPress));

        this.addRenderableWidget(
                new TexturedCyclingButtonWidget<>(SearchContext.withState(ClientSettings.searchContext), centerX + 7, centerY + 71, WIDGET_TEXTURE, this::onSearchTypeButtonPress));
        this.addRenderableWidget(
                new TexturedCyclingButtonWidget<>(CycleableOption.create("settings", Unit.class, 120), centerX + 41, centerY + 71, WIDGET_TEXTURE, this::onSettingsButtonPress));

//        SpriteIconButton settingsButton = SpriteIconButton.builder(Component.translatable("option.regex_search.settings"), this::onSettingsButtonPress, true)
//                .size(20, 20)
//                .sprite(RegexSearch.ID("settings"),18,18)
//                .build();
//        settingsButton.setPosition(centerX + 41, centerY + 71);
//        this.addRenderableWidget(settingsButton);

//        new Button<>(SearchContext.withState(ClientSettings.searchContext), centerX + 7, centerY + 71, WIDGET_TEXTURE, this::onSearchTypeButtonPress))
//        ;

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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER && searchBox.isFocused()) {

            String query = searchBox.getValue();
            if (query.isEmpty()) {
                HighlightRenderer.clear();
            } else {
                PacketDistributor.sendToServer(new ItemSearchPacketC2S(new SearchOptions(query, ClientSettings.isCaseSensitive, ClientSettings.searchContext, client.options.advancedItemTooltips, ClientSettings.languageCode)));

                ClientSettings.searchHistory.add(query);
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

    private void onSettingsButtonPress(TexturedCyclingButtonWidget<Unit> button) {
        ModConfig modConfig = ModConfigs.getModConfigs(RegexSearch.MOD_ID).stream().filter(c -> c.getSpec() == ClientConfig.SPEC).findFirst().orElse(null);
        if (modConfig == null)
            return;
        Component title = Component.translatable("neoforge.configuration.uitext.title.client", "Regex Search");
        Minecraft.getInstance().setScreen(new ConfigScreen(Minecraft.getInstance().screen, ModConfig.Type.CLIENT, modConfig, title));
    }
//    public Component translatableConfig(ModConfig modConfig, String suffix, String fallback) {
//        return Component.translatable(("regex_search.configuration.section." + modConfig.getFileName().replaceAll("[^a-zA-Z0-9]+", ".").replaceFirst("^\\.", "").replaceFirst("\\.$", "").toLowerCase(Locale.ENGLISH) + suffix, fallback), mod.getModInfo().getDisplayName());
//    }
//    private static final String LANG_PREFIX = "neoforge.configuration.uitext.";
}
