package com.tiers.screens;

import com.mojang.blaze3d.platform.NativeImage;
import com.tiers.PlayerProfileQueue;
import com.tiers.TiersClient;
import com.tiers.misc.ConfigManager;
import com.tiers.profile.PlayerProfile;
import com.tiers.profile.Status;
import com.tiers.profile.types.MCTiersProfile;
import com.tiers.profile.types.PvPTiersProfile;
import com.tiers.profile.types.MCPVPProfile;
import com.tiers.profile.types.SubtiersProfile;
import com.tiers.textures.ColorControl;
import com.tiers.textures.Icons;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.NonNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.Stream;

import static com.tiers.TiersClient.LOGGER;

public class ConfigScreen extends Screen {
    public static PlayerProfile ownProfile;
    public static PlayerProfile defaultProfile;

    private boolean useOwnProfile;
    private String autoDetectKitBoundKey;
    private String cycleRightBoundKey;
    private String cycleLeftBoundKey;
    private final Identifier playerAvatarTexture = Identifier.parse("");
    private boolean imageReady;
    private boolean imageError;

    private Button toggleMod;
    private Button toggleRegion;
    private Button toggleIcons;
    private Button togglePeak;
    private Button toggleTab;
    private Button toggleChat;
    private Button toggleSeparatorMode;
    private Button cycleDisplayMode;
    private Button clearPlayerCache;
    private Button resetSettings;
    private Button autoKitDetect;
    private Button leftMCTiers;
    private Button centerMCTiers;
    private Button rightMCTiers;
    private Button leftPvPTiers;
    private Button centerPvPTiers;
    private Button rightPvPTiers;
    private Button leftSubtiers;
    private Button centerSubtiers;
    private Button rightSubtiers;
    private Button leftMCPVP;
    private Button centerMCPVP;
    private Button rightMCPVP;
    private Button activeRightMode;
    private Button activeLeftMode;
    private Button enableOwnProfile;
    private boolean buttonsReady;

    private int centerX;
    private int distance;

    private ConfigScreen() {
        super(Component.literal("Tiers config"));

        autoDetectKitBoundKey = String.valueOf(TiersClient.autoDetectKey.getTranslatedKeyMessage()).replace("literal{", "\"").replace("}", "\"");
        if (autoDetectKitBoundKey.length() != 3)
            autoDetectKitBoundKey = "the assigned keybind";

        cycleRightBoundKey = String.valueOf(TiersClient.cycleRightKey.getTranslatedKeyMessage()).replace("literal{", "\"").replace("}", "\"");
        if (cycleRightBoundKey.length() != 3)
            cycleRightBoundKey = "the assigned keybind";

        cycleLeftBoundKey = String.valueOf(TiersClient.cycleLeftKey.getTranslatedKeyMessage()).replace("literal{", "\"").replace("}", "\"");
        if (cycleLeftBoundKey.length() != 3)
            cycleLeftBoundKey = "the assigned keybind";

        loadPlayerAvatar();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        centerX = width / 2;
        distance = height / 14;

        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.centeredText(font, Component.literal("Tiers config"), centerX, height / 50, CommonColors.WHITE);

        drawIconShowcase(graphics);

        if (!useOwnProfile)
            graphics.blit(RenderPipelines.GUI_TEXTURED, playerAvatarTexture, centerX - height / 10 / 2, height - (int) (height / 4.166) - height / 54, 0, 0, height / 10, (int) (height / 4.166), height / 10, (int) (height / 4.166));
        else
            drawPlayerAvatar(graphics, centerX, height - (int) (height / 4.166) - height / 54);

        graphics.centeredText(font, useOwnProfile ? ownProfile.getFullName() : defaultProfile.getFullName(), centerX, height - (int) (height / 4.166) - height / 54 - 12, CommonColors.WHITE);

        int mcTiersLogoWidth = Math.min(128, columnSpacing() - 6);
        int mcTiersLogoHeight = mcTiersLogoWidth * 24 / 128;
        graphics.blit(RenderPipelines.GUI_TEXTURED, MCTiersProfile.MCTIERS_IMAGE, columnX(0) - mcTiersLogoWidth / 2, distance + 110 + 4 + (24 - mcTiersLogoHeight) / 2, 0, 0, mcTiersLogoWidth, mcTiersLogoHeight, mcTiersLogoWidth, mcTiersLogoHeight);
        graphics.blit(RenderPipelines.GUI_TEXTURED, PvPTiersProfile.PVPTIERS_IMAGE, columnX(1) - 12, distance + 110 + 4, 0, 0, 24, 24, 24, 24);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SubtiersProfile.SUBTIERS_IMAGE, columnX(2) - 15, distance + 110, 0, 0, 30, 30, 30, 30);
        MCPVPProfile.drawLogo(graphics, font, columnX(3), distance + 110 + 12);

        graphics.text(font, TiersClient.getRightIcon(), centerX + 90 + 32, distance + 75 + 9, CommonColors.WHITE);
        graphics.text(font, TiersClient.getLeftIcon(), centerX - 90 - 32 - 12, distance + 75 + 9, CommonColors.WHITE);

        checkUpdates();
    }

    private void drawIconShowcase(GuiGraphicsExtractor graphics) {
        for (int i = 0; i < 8; i++) {
            graphics.centeredText(font, Component.literal(String.valueOf((char) (0xF000 + i))).setStyle(Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "gamemodes/classic-medium")))), 15, 32 + 14 * i, CommonColors.WHITE);
            graphics.centeredText(font, Component.literal(String.valueOf((char) (0xF000 + i))).setStyle(Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "gamemodes/pvptiers-medium")))), 40, 32 + 14 * i, CommonColors.WHITE);
            graphics.centeredText(font, Component.literal(String.valueOf((char) (0xF000 + i))).setStyle(Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "gamemodes/mctiers-medium")))), 65, 32 + 14 * i, CommonColors.WHITE);
        }
    }

    private void checkUpdates() {
        if (!buttonsReady)
            return;

        toggleMod.setPosition(width / 2 - 88 - 2, distance);
        toggleRegion.setPosition(width / 2 - 88 - 2 + 28 + 2 + 1, distance);
        toggleIcons.setPosition(width / 2 - 88 - 2 + 28 + 2 + 28 + 2 + 1, distance);
        togglePeak.setPosition(width / 2 + 2 - 1, distance);
        toggleTab.setPosition(width / 2 + 2 + 28 + 2 - 1, distance);
        toggleChat.setPosition(width / 2 + 2 + 28 + 2 + 28 + 2 - 1, distance);
        toggleSeparatorMode.setPosition(width / 2 - 90, distance + 25);
        cycleDisplayMode.setPosition(width / 2 - 90, distance + 50);
        autoKitDetect.setPosition(width / 2 - 90, distance + 75);
        clearPlayerCache.setPosition(width - 88 - 5, height - 20 - 5);
        resetSettings.setPosition(5, height - 20 - 5);
        leftMCTiers.setPosition(columnX(0) - 10 - 24, distance + 145);
        centerMCTiers.setPosition(columnX(0) - 10, distance + 145);
        rightMCTiers.setPosition(columnX(0) - 10 + 24, distance + 145);
        leftPvPTiers.setPosition(columnX(1) - 10 - 24, distance + 145);
        centerPvPTiers.setPosition(columnX(1) - 10, distance + 145);
        rightPvPTiers.setPosition(columnX(1) - 10 + 24, distance + 145);
        leftSubtiers.setPosition(columnX(2) - 10 - 24, distance + 145);
        centerSubtiers.setPosition(columnX(2) - 10, distance + 145);
        rightSubtiers.setPosition(columnX(2) - 10 + 24, distance + 145);
        leftMCPVP.setPosition(columnX(3) - 10 - 24, distance + 145);
        centerMCPVP.setPosition(columnX(3) - 10, distance + 145);
        rightMCPVP.setPosition(columnX(3) - 10 + 24, distance + 145);
        activeRightMode.setPosition(centerX + 90 + 4, distance + 75);
        activeLeftMode.setPosition(centerX - 90 - 20 - 4, distance + 75);
        enableOwnProfile.setPosition(width - 20 - 5 - 88 - 4, height - 20 - 5);

        updateVisibilities();
    }

    @Override
    protected void init() {
        centerX = width / 2;
        distance = height / 14;

        toggleMod = Button.builder(TiersClient.toggleMod ? Icons.LEVER_ON : Icons.LEVER_OFF, (Button) -> {
            TiersClient.toggleMod();
            updateActiveButtonsState();
            Button.setMessage(TiersClient.toggleMod ? Icons.LEVER_ON : Icons.LEVER_OFF);
            Button.setTooltip(Tooltip.create(Component.literal((TiersClient.toggleMod ? "Disable Tiers" : "Enable Tiers"))));
        }).bounds(width / 2 - 88 - 2, distance, 29, 20).tooltip(Tooltip.create(Component.literal((TiersClient.toggleMod ? "Disable Tiers" : "Enable Tiers")))).build();

        toggleRegion = Button.builder(TiersClient.toggleRegion ? Icons.REGION : Icons.REGION_DISABLED, (buttonWidget) -> {
            TiersClient.toggleRegion();
            buttonWidget.setMessage(TiersClient.toggleRegion ? Icons.REGION : Icons.REGION_DISABLED);
            buttonWidget.setTooltip(Tooltip.create(Component.literal(TiersClient.toggleRegion ? "Disable regions from being displayed next to the tier" : "Enable region display next to the tier")));
        }).bounds(width / 2 - 88 - 2 + 28 + 2 + 1, distance, 28, 20).tooltip(Tooltip.create(Component.literal(TiersClient.toggleRegion ? "Disable regions from being displayed next to the tier" : "Enable region display next to the tier"))).build();

        toggleIcons = Button.builder(TiersClient.toggleIcons ? Icons.ICONS : Icons.ICONS_DISABLED, (buttonWidget) -> {
            TiersClient.toggleIcons();
            buttonWidget.setMessage(TiersClient.toggleIcons ? Icons.ICONS : Icons.ICONS_DISABLED);
            buttonWidget.setTooltip(Tooltip.create(Component.literal(TiersClient.toggleIcons ? "Disable the gamemode icon next to the tier" : "Enable the gamemode icon next to the tier")));
        }).bounds(width / 2 - 88 - 2 + 28 + 2 + 28 + 2 + 1, distance, 28, 20).tooltip(Tooltip.create(Component.literal(TiersClient.toggleIcons ? "Disable the gamemode icon next to the tier" : "Enable the gamemode icon next to the tier"))).build();

        togglePeak = Button.builder(TiersClient.togglePeak ? Icons.PEAK : Icons.PEAK_DISABLED, (buttonWidget) -> {
            TiersClient.togglePeak();
            buttonWidget.setMessage(TiersClient.togglePeak ? Icons.PEAK : Icons.PEAK_DISABLED);
            buttonWidget.setTooltip(Tooltip.create(Component.literal(TiersClient.togglePeak ? """
                    Disable peak tiers
                    
                    Peak tiers: If you had HT3 and got demoted to LT3, you'll see ^HT3""" : """
                    Enable peak tiers
                    
                    Peak tiers: If you had HT3 and got demoted to LT3, you'll see ^HT3""")));
        }).bounds(width / 2 + 2 - 1, distance, 28, 20).tooltip(Tooltip.create(Component.literal(TiersClient.togglePeak ? """
                Disable peak tiers
                
                Peak tiers: If you had HT3 and got demoted to LT3, you'll see ^HT3""" : """
                Enable peak tiers
                
                Peak tiers: If you had HT3 and got demoted to LT3, you'll see ^HT3"""))).build();

        toggleTab = Button.builder(TiersClient.toggleTab ? Icons.TAB : Icons.TAB_DISABLED, (buttonWidget) -> {
            TiersClient.toggleTab();
            buttonWidget.setMessage(TiersClient.toggleTab ? Icons.TAB : Icons.TAB_DISABLED);
            buttonWidget.setTooltip(Tooltip.create(Component.literal(TiersClient.toggleTab ? "Disable Tiers on the tablist" : "Enable Tiers on the tablist")));
        }).bounds(width / 2 + 2 + 28 + 2 - 1, distance, 28, 20).tooltip(Tooltip.create(Component.literal(TiersClient.toggleTab ? "Disable Tiers on the tablist" : "Enable Tiers on the tablist"))).build();

        toggleChat = Button.builder(TiersClient.toggleChat ? Icons.CHAT : Icons.CHAT_DISABLED, (buttonWidget) -> {
            TiersClient.toggleChat();
            buttonWidget.setMessage(TiersClient.toggleChat ? Icons.CHAT : Icons.CHAT_DISABLED);
            buttonWidget.setTooltip(Tooltip.create(Component.literal(TiersClient.toggleChat ? "Disable Tiers in chat" : "Enable Tiers in chat")));
        }).bounds(width / 2 + 2 + 28 + 2 + 28 + 2 - 1, distance, 29, 20).tooltip(Tooltip.create(Component.literal(TiersClient.toggleChat ? "Disable Tiers in chat" : "Enable Tiers in chat"))).build();

        toggleSeparatorMode = Button.builder(Component.literal(TiersClient.toggleAdaptiveSeparator ? "Disable Dynamic Separator" : "Enable Dynamic Separator"), (buttonWidget) -> {
            TiersClient.toggleAdaptiveSeparator();
            buttonWidget.setMessage(Component.literal(TiersClient.toggleAdaptiveSeparator ? "Disable Dynamic Separator" : "Enable Dynamic Separator"));
            buttonWidget.setTooltip(Tooltip.create(Component.literal(TiersClient.toggleAdaptiveSeparator ? "Make the Tiers separator gray" : "Make the Tiers separator match the tier / region color")));
        }).bounds(width / 2 - 90, distance + 25, 180, 20).tooltip(Tooltip.create(Component.literal(TiersClient.toggleAdaptiveSeparator ? "Make the Tiers separator gray" : "Make the Tiers separator match the tier / region color"))).build();

        cycleDisplayMode = Button.builder(Component.literal(TiersClient.displayMode.getCurrentMode()), (buttonWidget) -> {
            TiersClient.cycleDisplayMode();
            buttonWidget.setMessage(Component.literal(TiersClient.displayMode.getCurrentMode()));
        }).bounds(width / 2 - 90, distance + 50, 180, 20).tooltip(Tooltip.create(Component.literal(("""
                Selected: only the selected tier will be displayed
                
                Highest: only the highest tier will be displayed
                
                Adaptive Highest: the highest tier will be displayed if selected does not exist""")))).build();

        autoKitDetect = Button.builder(Component.literal(TiersClient.toggleAutoKitDetect ? "Disable auto kit detect" : "Enable auto kit detect"), (buttonWidget) -> {
            TiersClient.toggleAutoKitDetect();
            buttonWidget.setMessage(Component.literal(TiersClient.toggleAutoKitDetect ? "Disable auto kit detect" : "Enable auto kit detect"));
            buttonWidget.setTooltip(Tooltip.create(Component.literal((TiersClient.toggleAutoKitDetect ?
                    "Disable auto kit detect: you will need to press " + autoDetectKitBoundKey + " to auto-detect the current gamemode" :
                    "Enable auto kit detect: Tiers will always scan your inventory to display the current gamemode (instead of pressing " + autoDetectKitBoundKey + ")"))));
        }).bounds(width / 2 - 90, distance + 75, 180, 20).tooltip(Tooltip.create(Component.literal((TiersClient.toggleAutoKitDetect ?
                "Disable auto kit detect: you will need to press " + autoDetectKitBoundKey + " to auto-detect the current gamemode" :
                "Enable auto kit detect: Tiers will always scan your inventory to display the current gamemode (instead of pressing " + autoDetectKitBoundKey + ")")))).build();

        if (ownProfile.status == Status.READY) {
            enableOwnProfile = Button.builder(Icons.CYCLE, (buttonWidget) -> {
                useOwnProfile = !useOwnProfile;

                imageReady = false;
                loadPlayerAvatar();

                buttonWidget.setTooltip(Tooltip.create(Component.literal(useOwnProfile ? "Preview the default profile (" + defaultProfile.name + ")" : "Preview your player profile (" + ownProfile.name + ")")));
            }).bounds(width - 20 - 5 - 88 - 4, height - 20 - 5, 20, 20).tooltip(Tooltip.create(Component.literal(useOwnProfile ? "Preview the default profile (" + defaultProfile.name + ")" : "Preview your player profile (" + ownProfile.name + ")"))).build();
        } else {
            enableOwnProfile = Button.builder(Component.literal("⚠"), (_) -> {
                ownProfile = new PlayerProfile(Minecraft.getInstance().getGameProfile().name(), false);
                PlayerProfileQueue.putFirstInQueue(ownProfile);

                onClose();
            }).bounds(width - 20 - 5 - 88 - 4, height - 20 - 5, 20, 20).tooltip(Tooltip.create(Component.literal("Can't switch profiles: " + ownProfile.name + " is not found or fetched yet. Click to close screen and retry"))).build();
        }

        clearPlayerCache = Button.builder(Component.literal("Clear cache"), (_) -> TiersClient.clearCache(false)).bounds(width - 88 - 5, height - 20 - 5, 88, 20).tooltip(Tooltip.create(Component.literal("Clear all player cache"))).build();

        resetSettings = Button.builder(Component.literal("Reset settings"), (_) -> {
            this.onClose();
            TiersClient.resetSettings();
        }).bounds(5, height - 20 - 5, 88, 20).tooltip(Tooltip.create(Component.literal("Reset Tiers settings to default"))).build();

        leftMCTiers = positionButton("←", "Display MCTiers on the left", 0, TiersClient.DisplayStatus.LEFT, -24);
        centerMCTiers = positionButton("●", "Disable MCTiers", 0, TiersClient.DisplayStatus.OFF, 0);
        rightMCTiers = positionButton("→", "Display MCTiers on the right", 0, TiersClient.DisplayStatus.RIGHT, 24);
        leftPvPTiers = positionButton("←", "Display PvPTiers on the left", 1, TiersClient.DisplayStatus.LEFT, -24);
        centerPvPTiers = positionButton("●", "Disable PvPTiers", 1, TiersClient.DisplayStatus.OFF, 0);
        rightPvPTiers = positionButton("→", "Display PvPTiers on the right", 1, TiersClient.DisplayStatus.RIGHT, 24);
        leftSubtiers = positionButton("←", "Display Subtiers on the left", 2, TiersClient.DisplayStatus.LEFT, -24);
        centerSubtiers = positionButton("●", "Disable Subtiers", 2, TiersClient.DisplayStatus.OFF, 0);
        rightSubtiers = positionButton("→", "Display Subtiers on the right", 2, TiersClient.DisplayStatus.RIGHT, 24);
        leftMCPVP = positionButton("←", "Display MCPVP on the left", 3, TiersClient.DisplayStatus.LEFT, -24);
        centerMCPVP = positionButton("●", "Disable MCPVP", 3, TiersClient.DisplayStatus.OFF, 0);
        rightMCPVP = positionButton("→", "Display MCPVP on the right", 3, TiersClient.DisplayStatus.RIGHT, 24);

        activeRightMode = Button.builder(Icons.CYCLE, (_) -> {
            TiersClient.cycleRightMode();
            autoKitDetect.setMessage(Component.literal(TiersClient.toggleAutoKitDetect ? "Disable auto kit detect" : "Enable auto kit detect"));
            autoKitDetect.setTooltip(Tooltip.create(Component.literal((TiersClient.toggleAutoKitDetect ?
                    "Disable auto kit detect: you will need to press " + autoDetectKitBoundKey + " to auto-detect the current gamemode" :
                    "Enable auto kit detect: Tiers will always scan your inventory to display the right gamemode (instead of pressing " + autoDetectKitBoundKey + ")"))));
        }).bounds(centerX + 90 + 4, distance + 75, 20, 20).tooltip(Tooltip.create(Component.literal("Cycle active right gamemode (press " + cycleRightBoundKey + " in game)"))).build();

        activeLeftMode = Button.builder(Icons.CYCLE, (_) -> {
            TiersClient.cycleLeftMode();
            autoKitDetect.setMessage(Component.literal(TiersClient.toggleAutoKitDetect ? "Disable auto kit detect" : "Enable auto kit detect"));
            autoKitDetect.setTooltip(Tooltip.create(Component.literal((TiersClient.toggleAutoKitDetect ?
                    "Disable auto kit detect: you will need to press " + autoDetectKitBoundKey + " to auto-detect the current gamemode" :
                    "Enable auto kit detect: Tiers will always scan your inventory to display the right gamemode (instead of pressing " + autoDetectKitBoundKey + ")"))));
        }).bounds(centerX - 90 - 20 - 4, distance + 75, 20, 20).tooltip(Tooltip.create(Component.literal("Cycle active left gamemode (press " + cycleLeftBoundKey + " in game)"))).build();

        Button useClassicIcons = Button.builder(TiersClient.activeIcons == Icons.Type.CLASSIC ? Component.literal("●") : Component.empty(), (buttonWidget) -> {
            buttonWidget.setMessage(TiersClient.activeIcons == Icons.Type.CLASSIC ? Component.literal("●") : Component.empty());
            TiersClient.changeIcons(Icons.Type.CLASSIC, true);
        }).bounds(5, 5, 20, 20).tooltip(Tooltip.create(Component.literal("Use classic styled icons and colors"))).build();

        Button usePvPTiersIcons = Button.builder(TiersClient.activeIcons == Icons.Type.PVPTIERS ? Component.literal("●") : Component.empty(), (buttonWidget) -> {
            buttonWidget.setMessage(TiersClient.activeIcons == Icons.Type.PVPTIERS ? Component.literal("●") : Component.empty());
            TiersClient.changeIcons(Icons.Type.PVPTIERS, true);
        }).bounds(30, 5, 20, 20).tooltip(Tooltip.create(Component.literal("Use PvPTiers styled icons and colors"))).build();

        Button useMCTiersIcons = Button.builder(TiersClient.activeIcons == Icons.Type.MCTIERS ? Component.literal("●") : Component.empty(), (buttonWidget) -> {
            buttonWidget.setMessage(TiersClient.activeIcons == Icons.Type.MCTIERS ? Component.literal("●") : Component.empty());
            TiersClient.changeIcons(Icons.Type.MCTIERS, true);
        }).bounds(55, 5, 20, 20).tooltip(Tooltip.create(Component.literal("Use MCTiers styled icons and colors"))).build();

        switch (TiersClient.activeIcons) {
            case CLASSIC -> useClassicIcons.active = false;
            case PVPTIERS -> usePvPTiersIcons.active = false;
            case MCTIERS -> useMCTiersIcons.active = false;
        }

        updateVisibilities();
        updateActiveButtonsState();
        buttonsReady = true;

        Stream.of(toggleMod, toggleRegion, toggleIcons, togglePeak, toggleTab, toggleChat, toggleSeparatorMode, cycleDisplayMode, autoKitDetect, clearPlayerCache, resetSettings, leftMCTiers, centerMCTiers, rightMCTiers, leftPvPTiers, centerPvPTiers, rightPvPTiers, leftSubtiers, centerSubtiers, rightSubtiers, leftMCPVP, centerMCPVP, rightMCPVP, activeRightMode, activeLeftMode, enableOwnProfile, useClassicIcons, usePvPTiersIcons, useMCTiersIcons)
                .forEach(this::addRenderableWidget);
    }

    /** X centre of the n-th tierlist column (MCTiers, PvPTiers, Subtiers, MCPVP). */
    /** Distance between tierlist columns: 116px, shrinking on narrow screens so all four stay on screen. */
    private int columnSpacing() {
        return Math.max(76, Math.min(116, (width - 16) / 4));
    }

    private int columnX(int column) {
        return centerX + (column * 2 - 3) * columnSpacing() / 2;
    }

    private static TiersClient.DisplayStatus getTierlistPosition(int tierlist) {
        return switch (tierlist) {
            case 0 -> TiersClient.positionMCTiers;
            case 1 -> TiersClient.positionPvPTiers;
            case 2 -> TiersClient.positionSubtiers;
            default -> TiersClient.positionMCPVP;
        };
    }

    private static void setTierlistPosition(int tierlist, TiersClient.DisplayStatus position) {
        switch (tierlist) {
            case 0 -> TiersClient.positionMCTiers = position;
            case 1 -> TiersClient.positionPvPTiers = position;
            case 2 -> TiersClient.positionSubtiers = position;
            default -> TiersClient.positionMCPVP = position;
        }
    }

    /** Left / off / right button for a tierlist; only one tierlist can occupy each side. */
    private Button positionButton(String label, String tooltip, int tierlist, TiersClient.DisplayStatus target, int offset) {
        return Button.builder(Component.literal(label), (_) -> {
            if (target != TiersClient.DisplayStatus.OFF)
                for (int other = 0; other < 4; other++)
                    if (other != tierlist && getTierlistPosition(other) == target)
                        setTierlistPosition(other, TiersClient.DisplayStatus.OFF);
            setTierlistPosition(tierlist, target);
            updateActiveButtonsState();
            updateVisibilities();
            ConfigManager.saveConfig();
        }).bounds(columnX(tierlist) - 10 + offset, distance + 145, 20, 20).tooltip(Tooltip.create(Component.literal(tooltip))).build();
    }

    private void updateActiveButtonsState() {
        toggleRegion.active = TiersClient.toggleMod;
        toggleIcons.active = TiersClient.toggleMod;
        togglePeak.active = TiersClient.toggleMod;
        toggleTab.active = TiersClient.toggleMod;
        toggleChat.active = TiersClient.toggleMod;
        toggleSeparatorMode.active = TiersClient.toggleMod;
        cycleDisplayMode.active = TiersClient.toggleMod;
        autoKitDetect.active = TiersClient.toggleMod;
        leftMCTiers.active = TiersClient.toggleMod;
        centerMCTiers.active = TiersClient.toggleMod;
        rightMCTiers.active = TiersClient.toggleMod;
        leftPvPTiers.active = TiersClient.toggleMod;
        centerPvPTiers.active = TiersClient.toggleMod;
        rightPvPTiers.active = TiersClient.toggleMod;
        leftSubtiers.active = TiersClient.toggleMod;
        centerSubtiers.active = TiersClient.toggleMod;
        rightSubtiers.active = TiersClient.toggleMod;
        leftMCPVP.active = TiersClient.toggleMod;
        centerMCPVP.active = TiersClient.toggleMod;
        rightMCPVP.active = TiersClient.toggleMod;
        activeLeftMode.active = TiersClient.toggleMod;
        activeRightMode.active = TiersClient.toggleMod;

        switch (TiersClient.positionMCTiers) {
            case RIGHT -> rightMCTiers.active = false;
            case OFF -> centerMCTiers.active = false;
            case LEFT -> leftMCTiers.active = false;
        }

        switch (TiersClient.positionPvPTiers) {
            case RIGHT -> rightPvPTiers.active = false;
            case OFF -> centerPvPTiers.active = false;
            case LEFT -> leftPvPTiers.active = false;
        }

        switch (TiersClient.positionSubtiers) {
            case RIGHT -> rightSubtiers.active = false;
            case OFF -> centerSubtiers.active = false;
            case LEFT -> leftSubtiers.active = false;
        }

        switch (TiersClient.positionMCPVP) {
            case RIGHT -> rightMCPVP.active = false;
            case OFF -> centerMCPVP.active = false;
            case LEFT -> leftMCPVP.active = false;
        }
    }

    private void updateVisibilities() {
        activeRightMode.visible = TiersClient.positionMCTiers == TiersClient.DisplayStatus.RIGHT || TiersClient.positionPvPTiers == TiersClient.DisplayStatus.RIGHT || TiersClient.positionSubtiers == TiersClient.DisplayStatus.RIGHT || TiersClient.positionMCPVP == TiersClient.DisplayStatus.RIGHT;
        activeLeftMode.visible = TiersClient.positionMCTiers == TiersClient.DisplayStatus.LEFT || TiersClient.positionPvPTiers == TiersClient.DisplayStatus.LEFT || TiersClient.positionSubtiers == TiersClient.DisplayStatus.LEFT || TiersClient.positionMCPVP == TiersClient.DisplayStatus.LEFT;
    }

    private void drawPlayerAvatar(GuiGraphicsExtractor graphics, int x, int y) {
        if (imageReady) {
            if (ownProfile.imageSaved == 1 || ownProfile.imageSaved == 2)
                graphics.blit(RenderPipelines.GUI_TEXTURED, playerAvatarTexture, x - height / 10 / 2, y, 0, 0, height / 10, (int) (height / 4.166), height / 10, (int) (height / 4.166));
            else if (ownProfile.imageSaved < 6 && ownProfile.imageSaved > 2)
                graphics.blit(RenderPipelines.GUI_TEXTURED, playerAvatarTexture, x - height / 7 / 2, y, 0, 0, height / 7, (int) (height / 4.145), height / 7, (int) (height / 4.145));
        } else if (ownProfile.imageSaved != 0) {
            loadPlayerAvatar();
        } else if (ownProfile.numberOfImageRequests >= 6)
            graphics.centeredText(font, Component.literal(ownProfile.name + "'s skin failed to load. Restart game to retry"), x, y + 50, ColorControl.getColorMinecraftStandard("red"));
    }

    private void loadPlayerAvatar() {
        if (imageReady || imageError)
            return;

        try (FileInputStream fileInputStream = new FileInputStream(FabricLoader.getInstance().getGameDir().resolve("cache/tiers/" + (useOwnProfile ? ownProfile.uuid : defaultProfile.uuid) + ".png").toFile())) {
            Minecraft.getInstance().getTextureManager().register(playerAvatarTexture, new DynamicTexture(String::new, NativeImage.read(fileInputStream)));
            imageReady = true;
        } catch (IOException ignored) {
            LOGGER.warn("Error loading player skin");
            imageError = true;
        }
    }

    public static Screen getConfigScreen(Screen ignoredScreen) {
        return new ConfigScreen();
    }
}