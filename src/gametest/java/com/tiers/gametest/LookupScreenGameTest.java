package com.tiers.gametest;

import com.tiers.profile.PlayerProfile;
import com.tiers.profile.Status;
import com.tiers.screens.PlayerSearchResultScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Opens the player lookup screen with all four tierlists filled in and screenshots it at
 * several window sizes / GUI scales (both the list layout and the compact grid layout).
 * Run with `gradlew runClientGameTest`; screenshots land in build/run/clientGameTest/screenshots.
 */
public class LookupScreenGameTest implements FabricClientGameTest {
    private static final int[][] SIZES = {
            // window width, height, GUI scale
            {1920, 1080, 2},
            {1920, 1080, 3},
            {1920, 1080, 4},
            {1280, 720, 2},
            {1280, 720, 3},
            {854, 480, 2},
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        PlayerProfile profile = context.computeOnClient(minecraft -> new PlayerProfile(
                read("json/defaultProfileMojang.json"),
                read("json/defaultProfileMCTiers.json"),
                read("json/defaultProfilePvPTiers.json"),
                read("json/defaultProfileSubtiers.json"),
                read("sampleMCPVP.json")));

        if (profile.status != Status.READY || profile.profileMCPVP.status != Status.READY)
            throw new AssertionError("Sample profile did not parse: " + profile.status + " / MCPvP " + profile.profileMCPVP.status);
        if (profile.profileMCTiers.status != Status.READY || profile.profilePvPTiers.status != Status.READY || profile.profileSubtiers.status != Status.READY)
            throw new AssertionError("Upstream sample profiles did not parse");

        for (int[] size : SIZES) {
            context.getInput().resizeWindow(size[0], size[1]);
            context.runOnClient(minecraft -> {
                minecraft.options.guiScale().set(size[2]);
                minecraft.resizeGui();
            });
            context.setScreen(() -> new PlayerSearchResultScreen(profile));
            context.waitTicks(10);
            context.takeScreenshot("lookup_" + size[0] + "x" + size[1] + "_gui" + size[2]);

            context.setScreen(() -> com.tiers.screens.ConfigScreen.getConfigScreen(null));
            context.waitTicks(5);
            context.takeScreenshot("config_" + size[0] + "x" + size[1] + "_gui" + size[2]);
        }

        context.setScreen(net.minecraft.client.gui.screens.TitleScreen::new);
    }

    private static String read(String path) {
        try (InputStream stream = LookupScreenGameTest.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null)
                throw new AssertionError("Missing test resource " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
