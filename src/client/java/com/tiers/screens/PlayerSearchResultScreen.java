package com.tiers.screens;

import net.minecraft.util.FormattedCharSequence;
import com.mojang.blaze3d.platform.NativeImage;
import com.tiers.TiersClient;
import com.tiers.profile.GameMode;
import com.tiers.profile.PlayerProfile;
import com.tiers.profile.Status;
import com.tiers.profile.types.MCTiersProfile;
import com.tiers.profile.types.PvPTiersProfile;
import com.tiers.profile.types.MCPVPProfile;
import com.tiers.profile.types.SubtiersProfile;
import com.tiers.profile.types.SuperProfile;
import com.tiers.textures.ColorControl;
import com.tiers.textures.Icons;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.NonNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

import static com.tiers.TiersClient.LOGGER;

public class PlayerSearchResultScreen extends Screen {
    private final PlayerProfile playerProfile;
    private final Identifier playerAvatarTexture = Identifier.parse("");

    Button dimensionsWarning;

    private int separator;
    private boolean small;
    private int halfColumn = 64;
    private boolean tooSmall;
    private boolean imageReady;
    private boolean imageError;
    private boolean toastShown;

    public PlayerSearchResultScreen(PlayerProfile playerProfile) {
        super(Component.literal(playerProfile.name));
        this.playerProfile = playerProfile;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (!playerProfile.isPlayerValid()) {
            onClose();
            return;
        }

        if (playerProfile.nameChanged && !toastShown) {
            SystemToast.add(Minecraft.getInstance().gui.toastManager(), SystemToast.SystemToastId.NARRATOR_TOGGLE, Component.literal("Recent name change"), Component.literal("(" + playerProfile.name + " to " + playerProfile.inGameName + ") Data should be accurate"));
            toastShown = true;
        }

        int centerX = width / 2;
        int listY = (int) (height / 2.65);
        separator = height / 23;
        updateLayout();
        int avatarY = height / 55 + 12;

        super.extractRenderState(graphics, mouseX, mouseY, a);

        dimensionsWarning.visible = tooSmall;
        if (tooSmall) {
            dimensionsWarning.setMessage(Component.literal("⚠"));
            dimensionsWarning.setTooltip(Tooltip.create(Component.literal("Your window dimensions (" + width + "x" + height + ") are too small\nLower the GUI scale or make the window bigger! (min: " + MIN_WIDTH + "x" + MIN_HEIGHT + ")")));
        }

        if (playerProfile.status == Status.SEARCHING) {
            graphics.centeredText(font, Component.literal("Searching for " + playerProfile.name + "..."), centerX, listY, ColorControl.getColorMinecraftStandard("green"));
            return;
        }

        if (playerProfile.numberOfImageRequests == 0)
            playerProfile.savePlayerImage();

        drawPlayerAvatar(graphics, centerX, avatarY);
        if (!imageReady)
            graphics.centeredText(font, Component.literal("Loading " + playerProfile.name + "'s skin"), centerX, avatarY + 50, ColorControl.getColorMinecraftStandard("green"));

        graphics.centeredText(font, playerProfile.getFullName(), centerX, height / 55, CommonColors.WHITE);

        drawCategoryList(graphics, MCTiersProfile.MCTIERS_IMAGE, playerProfile.profileMCTiers, columnCenter(0), listY);
        drawCategoryList(graphics, PvPTiersProfile.PVPTIERS_IMAGE, playerProfile.profilePvPTiers, columnCenter(1), listY);
        drawCategoryList(graphics, SubtiersProfile.SUBTIERS_IMAGE, playerProfile.profileSubtiers, columnCenter(2), listY);
        drawCategoryList(graphics, MCPVPProfile.MCPVP_IMAGE, playerProfile.profileMCPVP, columnCenter(3), listY);
    }

    // ---- Four-column layout -------------------------------------------------------------
    // Columns are centred at 1/8, 3/8, 5/8 and 7/8 of the width. Wide windows show the
    // classic list (icon, kit name, tier); narrower ones switch to a compact grid of
    // icon + tier cells so all four tierlists always fit side by side.

    private static final int COLUMNS = 4;
    private static final int LIST_MODE_MIN_COLUMN = 195;
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 262;
    private static final int GRID_CELL = 28;
    private static final int GRID_CELL_MIN = 24;
    private boolean compactHeader;

    private void updateLayout() {
        int columnWidth = width / COLUMNS;
        small = columnWidth < LIST_MODE_MIN_COLUMN || height < 420;
        compactHeader = height < 300;
        tooSmall = width < MIN_WIDTH || height < MIN_HEIGHT;
        halfColumn = Math.max(40, Math.min(64, columnWidth / 2 - 4));
    }

    private int columnCenter(int column) {
        return width * (2 * column + 1) / (2 * COLUMNS);
    }

    private void centeredWrapped(GuiGraphicsExtractor graphics, Component text, int x, int y, int color) {
        int lineY = y;
        for (FormattedCharSequence line : font.split(text, halfColumn * 2)) {
            graphics.centeredText(font, line, x, lineY, color);
            lineY += 10;
        }
    }

    private int wrappedHeight(Component text) {
        return font.split(text, halfColumn * 2).size() * 10;
    }

    private void drawCategoryList(GuiGraphicsExtractor graphics, Identifier image, SuperProfile superProfile, int x, int y) {
        int logoY = (int) (y + 2.4 * separator) + 4 - 38;
        if (image == MCTiersProfile.MCTIERS_IMAGE) {
            int logoWidth = Math.min(128, halfColumn * 2);
            int logoHeight = logoWidth * 24 / 128;
            graphics.blit(RenderPipelines.GUI_TEXTURED, image, x - logoWidth / 2, logoY + (24 - logoHeight) / 2, 0, 0, logoWidth, logoHeight, logoWidth, logoHeight);
        } else if (image == PvPTiersProfile.PVPTIERS_IMAGE)
            graphics.blit(RenderPipelines.GUI_TEXTURED, image, x - 12, logoY, 0, 0, 24, 24, 24, 24);
        else if (image == SubtiersProfile.SUBTIERS_IMAGE)
            graphics.blit(RenderPipelines.GUI_TEXTURED, image, (int) (x - 15.5), logoY - 4, 0, 0, 31, 31, 31, 31);
        else if (image == MCPVPProfile.MCPVP_IMAGE)
            MCPVPProfile.drawLogo(graphics, font, x, logoY + 8);

        int messageY = (int) (y + 2.8 * separator);

        if (superProfile == null) {
            centeredWrapped(graphics, Component.literal("Loading from API..."), x, messageY, ColorControl.getColorMinecraftStandard("green"));
            return;
        }

        if (superProfile.status == Status.SEARCHING) {
            centeredWrapped(graphics, Component.literal("Searching..."), x, messageY, ColorControl.getColorMinecraftStandard("green"));
            return;
        } else if (superProfile.status == Status.NOT_EXISTING) {
            centeredWrapped(graphics, Component.literal("Unranked"), x, messageY, ColorControl.getColorMinecraftStandard("red"));
            return;
        } else if (superProfile.status == Status.TIMEOUTED || superProfile.status == Status.API_ISSUE) {
            Component headline = Component.literal(superProfile.status == Status.TIMEOUTED ? "Search timed out" : "Search failed: API issue");
            centeredWrapped(graphics, headline, x, messageY, ColorControl.getColorMinecraftStandard("red"));
            int nextY = messageY + wrappedHeight(headline) + 5;
            for (String line : superProfile.checkOutages()) {
                Component text = Component.literal(line);
                centeredWrapped(graphics, text, x, nextY, CommonColors.YELLOW);
                nextY += wrappedHeight(text);
            }

            if (superProfile.status == Status.API_ISSUE && TiersClient.issuesUrl != null && !superProfile.apiErrorShown) {
                addRenderableWidget(Button.builder(Component.literal("Report issue"), (_) -> ConfirmLinkScreen.confirmLinkNow(Minecraft.getInstance().gui.screen(), URI.create(TiersClient.issuesUrl), true))
                        .bounds(x - 40, nextY + 6, 80, 20).tooltip(Tooltip.create(Component.literal("Only report this if the tierlist's own website works fine"))).build());
                superProfile.apiErrorShown = true;
            }
            return;
        }

        if (!superProfile.drawn) {
            int headerY = (int) (y + 2.4 * separator);
            if (compactHeader) {
                addCompactHeader(superProfile, x, headerY);
                drawTierList(superProfile, x, headerY + 24);
            } else {
                addHeaderRow(Icons.GLOBE, Icons.colorText("Region", "region"), superProfile.displayedRegion, superProfile.regionTooltip, x, headerY);
                addHeaderRow(Icons.OVERALL, Icons.colorText("Overall", "overall"), superProfile.displayedOverall, superProfile.overallTooltip, x, headerY + 16);
                drawTierList(superProfile, x, headerY + 40);
            }

            if (superProfile instanceof MCTiersProfile && playerProfile.profileMCTiers.discordId != null)
                addRenderableWidget(Button.builder(Icons.DISCORD, (_) -> ConfirmLinkScreen.confirmLinkNow(Minecraft.getInstance().gui.screen(), URI.create("https://discord.com/users/" + playerProfile.profileMCTiers.discordId), true)).bounds(width - 20 - 5 - 24, height - 20 - 5, 20, 20).tooltip(Tooltip.create(Component.literal("Open " + playerProfile.targetName + "'s Discord profile"))).build());

            addRenderableWidget(Button.builder(Icons.NAMEMC, (_) -> ConfirmLinkScreen.confirmLinkNow(Minecraft.getInstance().gui.screen(), URI.create("https://namemc.com/profile/" + playerProfile.uuid), true)).bounds(width - 20 - 5, height - 20 - 5, 20, 20).tooltip(Tooltip.create(Component.literal("Open " + playerProfile.targetName + "'s NameMC page"))).build());

            superProfile.drawn = true;
        }
    }

    /** Short screens: region on the left and overall on the right of a single row ("🌐 NA     🏅 #1"). */
    private void addCompactHeader(SuperProfile superProfile, int x, int y) {
        int left = x - halfColumn;
        int right = x + halfColumn;

        StringWidget regionIcon = new StringWidget(Icons.GLOBE, font);
        regionIcon.setPosition(left, y + 2);
        regionIcon.setTooltip(Tooltip.create(Icons.colorText("Region", "region")));
        addRenderableWidget(regionIcon);

        StringWidget region = new StringWidget(superProfile.displayedRegion, font);
        region.setPosition(left + 14, y);
        region.setTooltip(Tooltip.create(superProfile.regionTooltip));
        addRenderableWidget(region);

        int overallWidth = font.width(superProfile.displayedOverall);
        StringWidget overall = new StringWidget(superProfile.displayedOverall, font);
        overall.setPosition(right - overallWidth, y);
        overall.setTooltip(Tooltip.create(superProfile.overallTooltip));
        addRenderableWidget(overall);

        StringWidget overallIcon = new StringWidget(Icons.OVERALL, font);
        overallIcon.setPosition(right - overallWidth - 14, y + 2);
        overallIcon.setTooltip(Tooltip.create(Icons.colorText("Overall", "overall")));
        addRenderableWidget(overallIcon);
    }

    /** One "icon  Label ........ value" row, sized to the column. The text label is dropped when it wouldn't fit. */
    private void addHeaderRow(Component icon, Component label, Component value, Component valueTooltip, int x, int y) {
        int left = x - halfColumn;
        int right = x + halfColumn;

        StringWidget iconWidget = new StringWidget(icon, font);
        iconWidget.setPosition(left, y + 2);
        iconWidget.setTooltip(Tooltip.create(label));
        addRenderableWidget(iconWidget);

        int valueWidth = font.width(value);
        if (left + 20 + font.width(label) + 6 <= right - valueWidth) {
            StringWidget labelWidget = new StringWidget(label, font);
            labelWidget.setPosition(left + 20, y);
            addRenderableWidget(labelWidget);
        }

        StringWidget valueWidget = new StringWidget(value, font);
        valueWidget.setPosition(right - valueWidth, y);
        valueWidget.setTooltip(Tooltip.create(valueTooltip));
        addRenderableWidget(valueWidget);
    }

    private void drawTierList(SuperProfile superProfile, int x, int y) {
        java.util.List<GameMode> ready = superProfile.gameModes.stream().filter(mode -> !mode.drawn && mode.status == Status.READY).toList();

        if (!small) {
            for (GameMode gameMode : ready) {
                drawListRow(gameMode, x - 64, y);
                y += 15;
            }
            return;
        }

        y -= 7;
        int available = (height - 30) - y;

        int perRow = Math.max(2, (halfColumn * 2) / GRID_CELL);
        int rows = (ready.size() + perRow - 1) / perRow;
        if (rows * 25 > available) { // tight: allow narrower cells to fit more kits per row
            perRow = Math.max(perRow, (halfColumn * 2) / GRID_CELL_MIN);
            rows = (ready.size() + perRow - 1) / perRow;
        }
        int cell = (halfColumn * 2) / perRow;

        // Fit the grid above the bottom button row: drop the inline "(peak)" line first
        // (it stays available in the tier tooltip), then tighten the row spacing.
        boolean showPeak = ready.stream().anyMatch(mode -> mode.hasPeak);
        int rowHeight = showPeak ? 36 : 29;
        if (rows * rowHeight > available && showPeak) {
            showPeak = false;
            rowHeight = 29;
        }
        if (rows * rowHeight > available)
            rowHeight = 25;

        for (int i = 0; i < ready.size(); i++) {
            int cellCenter = x - halfColumn + cell * (i % perRow) + cell / 2;
            drawGridCell(ready.get(i), cellCenter, y + (i / perRow) * rowHeight, showPeak);
        }
    }

    private void drawListRow(GameMode mode, int x, int y) {
        StringWidget icon = new StringWidget(mode.gamemode.getIcon(), font);
        icon.setPosition(x, y + 3);
        icon.setTooltip(Tooltip.create(mode.gamemode.getTextLabel()));
        addRenderableWidget(icon);

        StringWidget label = new StringWidget(mode.gamemode.getTextLabel(), font);
        label.setPosition(x + 20, y);
        addRenderableWidget(label);

        StringWidget tier = new StringWidget(mode.displayedTier, font);
        tier.setPosition(x + 114 - (mode.displayedTier.getString().length() - 3) * 3, y);
        tier.setTooltip(Tooltip.create(mode.tierTooltip));
        addRenderableWidget(tier);

        if (mode.hasPeak && mode.peakTierTooltip.getStyle().getColor() != null) {
            StringWidget peakTier = new StringWidget(Component.literal("(").append(mode.displayedPeakTier).append(")").setStyle(mode.displayedPeakTier.getStyle()), font);
            peakTier.setPosition(x + 136, y);
            peakTier.setTooltip(Tooltip.create(mode.peakTierTooltip));
            addRenderableWidget(peakTier);
        }

        mode.drawn = true;
    }

    private void drawGridCell(GameMode mode, int cellCenter, int y, boolean showPeak) {
        StringWidget icon = new StringWidget(mode.gamemode.getIcon(), font);
        icon.setPosition(cellCenter - font.width(mode.gamemode.getIcon()) / 2, y + 3);
        icon.setTooltip(Tooltip.create(mode.gamemode.getTextLabel()));
        addRenderableWidget(icon);

        StringWidget tier = new StringWidget(mode.displayedTier, font);
        tier.setPosition(cellCenter - font.width(mode.displayedTier) / 2, y + 15);
        Component tooltip = mode.tierTooltip;
        if (!showPeak && mode.hasPeak && mode.peakTierTooltip != null)
            tooltip = Component.empty().append(mode.tierTooltip).append("\n\n").append(mode.peakTierTooltip);
        tier.setTooltip(Tooltip.create(tooltip));
        addRenderableWidget(tier);

        if (showPeak && mode.hasPeak && mode.peakTierTooltip.getStyle().getColor() != null) {
            Component peakText = Component.literal("(").append(mode.displayedPeakTier).append(")").setStyle(mode.displayedPeakTier.getStyle());
            StringWidget peakTier = new StringWidget(peakText, font);
            peakTier.setPosition(cellCenter - font.width(peakText) / 2, y + 24);
            peakTier.setTooltip(Tooltip.create(mode.peakTierTooltip));
            addRenderableWidget(peakTier);
        }

        mode.drawn = true;
    }

    private void drawPlayerAvatar(GuiGraphicsExtractor graphics, int x, int y) {
        if (imageReady) {
            if (playerProfile.imageSaved == 1 || playerProfile.imageSaved == 2)
                graphics.blit(RenderPipelines.GUI_TEXTURED, playerAvatarTexture, x - width / 32, y, 0, 0, width / 16, (int) (width / 6.666), width / 16, (int) (width / 6.666));
            else if (playerProfile.imageSaved < 6 && playerProfile.imageSaved > 2)
                graphics.blit(RenderPipelines.GUI_TEXTURED, playerAvatarTexture, (int) (x - width / 22.5), y, 0, 0, (int) (width / 11.25), (int) (width / 6.666), (int) (width / 11.25), (int) (width / 6.666));
        } else if (playerProfile.imageSaved != 0) {
            loadPlayerAvatar();
        } else if (playerProfile.numberOfImageRequests >= 6)
            graphics.centeredText(font, Component.literal(playerProfile.name + "'s skin failed to load. Clear cache and retry"), x, y + 50, ColorControl.getColorMinecraftStandard("red"));
    }

    private void loadPlayerAvatar() {
        if (imageReady || imageError)
            return;

        try (FileInputStream fileInputStream = new FileInputStream(FabricLoader.getInstance().getGameDir().resolve("cache/tiers/players/" + playerProfile.uuid + ".png").toFile())) {
            Minecraft.getInstance().getTextureManager().register(playerAvatarTexture, new DynamicTexture(String::new, NativeImage.read(fileInputStream)));
            imageReady = true;
        } catch (IOException ignored) {
            LOGGER.warn("Error loading player skin");
            imageError = true;
        }
    }

    @Override
    public void init() {
        playerProfile.resetDrawnStatus();

        dimensionsWarning = Button.builder(Component.literal("ℹ"), (_) -> {
        }).bounds(width - 20 - 5, 5, 20, 20).tooltip(Tooltip.create(Component.literal("Your window dimensions (" + width + "x" + height + ") are small\nLower the GUI scale or make the window bigger to have a better experience (ideal: 575x420)"))).build();
        dimensionsWarning.active = false;
        dimensionsWarning.visible = small;
        if (tooSmall) {
            dimensionsWarning.setMessage(Component.literal("⚠"));
            dimensionsWarning.setTooltip(Tooltip.create(Component.literal("Your window dimensions (" + width + "x" + height + ") are too small\nLower the GUI scale or make the window bigger! (min: 430x262)")));
        }

        addRenderableWidget(dimensionsWarning);

        addRenderableWidget(Button.builder(Component.literal("Update"), (_) -> TiersClient.showUpdatedPlayerProfile(playerProfile, true)).bounds(5, height - 20 - 5, 68, 20).tooltip(Tooltip.create(Component.literal("Reload the player profile"))).build());
        addRenderableWidget(Button.builder(Icons.CYCLE, (_) -> playerProfile.updateTierlistProfiles(1)).bounds(5 + 68 + 4 + 0 * 24, height - 20 - 5, 20, 20).tooltip(Tooltip.create(Component.literal("Update MCTiers results"))).build());
        addRenderableWidget(Button.builder(Icons.CYCLE, (_) -> playerProfile.updateTierlistProfiles(2)).bounds(5 + 68 + 4 + 1 * 24, height - 20 - 5, 20, 20).tooltip(Tooltip.create(Component.literal("Update PvPTiers results"))).build());
        addRenderableWidget(Button.builder(Icons.CYCLE, (_) -> playerProfile.updateTierlistProfiles(3)).bounds(5 + 68 + 4 + 2 * 24, height - 20 - 5, 20, 20).tooltip(Tooltip.create(Component.literal("Update Subtiers results"))).build());
        addRenderableWidget(Button.builder(Icons.CYCLE, (_) -> playerProfile.updateTierlistProfiles(4)).bounds(5 + 68 + 4 + 3 * 24, height - 20 - 5, 20, 20).tooltip(Tooltip.create(Component.literal("Update MCPvP results"))).build());
    }
}