package com.tiers.profile.types;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tiers.TiersClient;
import com.tiers.misc.Mode;
import com.tiers.profile.GameMode;
import com.tiers.profile.Status;
import com.tiers.textures.Icons;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MCPVP (mcpvp.com) tierlist.
 * <p>
 * MCPVP has no public JSON API, so this reads the public profile page
 * ({@code https://www.mcpvp.com/profile/<uuid>}, which redirects to {@code /@<name>})
 * and converts its ranking markup into the same JSON shape the other tierlists use.
 * That normalized JSON is what gets stored in {@link #originalJson}, so re-styling and
 * the default config-screen profile go through the JSON path directly.
 * <p>
 * Unknown players are redirected to the home page, which has no rankings, so they end up
 * as {@link Status#NOT_EXISTING}. If MCPVP changes its page layout, parsing fails gracefully
 * to "not existing" instead of crashing.
 */
public class MCPVPProfile extends SuperProfile {
    /** Marker used by the screens to pick this tierlist's header; drawn as text by {@link #drawLogo} (no MCPvP artwork is bundled). */
    public static final Identifier MCPVP_IMAGE = Identifier.fromNamespaceAndPath("minecraft", "textures/mcpvp_logo.png");

    /** Draws the "MCPvP" column header, centred on x, at roughly the height of the other tierlists' logos. */
    public static void drawLogo(net.minecraft.client.gui.GuiGraphicsExtractor graphics, net.minecraft.client.gui.Font font, int x, int y) {
        Component logo = Component.literal("MCPvP").withStyle(style -> style.withBold(true).withColor(0xFFDA55));
        graphics.centeredText(font, logo, x, y, 0xFFFFFFFF);
    }
    public static final String PROFILE_URL = "https://www.mcpvp.com/profile/";

    private static final HttpClient REDIRECT_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // <button class="ranking-kit" ... data-kit="smp" ...> ... <span ... data-tier-code="MT2">MT2</span> ... </button>
    private static final Pattern KIT_BUTTON = Pattern.compile("<button\\b([^>]*\\bclass=\"ranking-kit\"[^>]*)>(.*?)</button>", Pattern.DOTALL);
    private static final Pattern DATA_KIT = Pattern.compile("\\bdata-kit=\"([a-z0-9_\\-]+)\"");
    private static final Pattern TIER_CODE = Pattern.compile("\\bdata-tier-code=\"([A-Za-z]{2}\\d)\"");
    private static final Pattern OVERALL_TIER = Pattern.compile("class=\"overall-tier[^\"]*\"[^>]*\\bdata-tier-code=\"([A-Za-z]{2}\\d)\"");
    // Points can be fractional on mcpvp.com (e.g. "265.5", "0.125")
    private static final Pattern OVERALL_POINTS = Pattern.compile("data-overall-progress.*?class=\"points-value\">\\s*(\\d+(?:\\.\\d+)?)\\s*<", Pattern.DOTALL);
    private static final Pattern TITLE_NAME = Pattern.compile("<title>\\s*MCPVP\\s*\\|\\s*([A-Za-z0-9_]{1,16})\\s*</title>");

    private String overallTier;
    private String mcpvpPoints = "0";

    public MCPVPProfile(String apiUrl, String uuid, String extra) {
        super();
        addGamemodes();
        buildRequest(apiUrl, uuid, extra);
    }

    public MCPVPProfile(String json) {
        super();
        addGamemodes();
        parseJson(json);
    }

    private void addGamemodes() {
        gameModes.add(new GameMode(Mode.MCPVP_SWORD, "sword"));
        gameModes.add(new GameMode(Mode.MCPVP_SHIELD, "shield"));
        gameModes.add(new GameMode(Mode.MCPVP_POT, "pot"));
        gameModes.add(new GameMode(Mode.MCPVP_EARLY_GAME, "early_game"));
        gameModes.add(new GameMode(Mode.MCPVP_END_GAME, "end_game"));
        gameModes.add(new GameMode(Mode.MCPVP_MACE, "mace"));
        gameModes.add(new GameMode(Mode.MCPVP_LATE_GAME, "late_game"));
        gameModes.add(new GameMode(Mode.MCPVP_SMP, "smp"));
        gameModes.add(new GameMode(Mode.MCPVP_CART, "cart"));
        gameModes.add(new GameMode(Mode.MCPVP_CREEPER, "creeper"));
        gameModes.add(new GameMode(Mode.MCPVP_SPEAR, "spear"));
        gameModes.add(new GameMode(Mode.MCPVP_BOW, "bow"));
        gameModes.add(new GameMode(Mode.MCPVP_CRYSTAL, "crystal"));
        gameModes.add(new GameMode(Mode.MCPVP_DIAMOND_SMP, "diamond_smp"));
        gameModes.add(new GameMode(Mode.MCPVP_NETHERITE_POT, "netherite_pot"));
        gameModes.add(new GameMode(Mode.MCPVP_UHC, "uhc"));
    }

    @Override
    protected HttpClient client() {
        return REDIRECT_CLIENT;
    }

    @Override
    public boolean hasRegion() {
        return false;
    }

    @Override
    public void parseJson(String body) {
        String json = body == null ? "" : body.strip();

        try {
            if (!json.startsWith("{")) {
                json = htmlToJson(json);
                if (json == null) {
                    status = Status.NOT_EXISTING;
                    return;
                }
            }

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            overallTier = jsonObject.has("overall_tier") && !jsonObject.get("overall_tier").isJsonNull()
                    ? jsonObject.get("overall_tier").getAsString() : null;
            mcpvpPoints = jsonObject.has("points_exact") ? jsonObject.get("points_exact").getAsString()
                    : jsonObject.has("points") ? jsonObject.get("points").getAsString() : "0";
        } catch (RuntimeException exception) {
            TiersClient.LOGGER.warn("Tiers | Could not read MCPVP profile ({}): {}", uuid, exception.getMessage());
            status = Status.API_ISSUE;
            return;
        }

        super.parseJson(json);
    }

    /**
     * Converts an mcpvp.com profile page into tierlist JSON:
     * {"name", "region": null, "points", "overall": 0, "overall_tier", "rankings": {kit: {"code": "MT2"}}}.
     * Returns null when the page is not a ranked player profile.
     */
    static String htmlToJson(String html) {
        if (html == null || !html.contains("ranking-kit"))
            return null;

        JsonObject rankings = new JsonObject();
        Matcher button = KIT_BUTTON.matcher(html);
        while (button.find()) {
            Matcher kit = DATA_KIT.matcher(button.group(1));
            Matcher code = TIER_CODE.matcher(button.group(2));
            if (!kit.find() || !code.find())
                continue;

            String kitKey = kit.group(1).toLowerCase(Locale.ROOT).replace('-', '_');
            if (rankings.has(kitKey))
                continue;

            JsonObject ranking = new JsonObject();
            ranking.addProperty("code", code.group(1).toUpperCase(Locale.ROOT));
            rankings.add(kitKey, ranking);
        }

        if (rankings.isEmpty())
            return null;

        JsonObject jsonObject = new JsonObject();
        Matcher name = TITLE_NAME.matcher(html);
        jsonObject.addProperty("name", name.find() ? name.group(1) : "");
        jsonObject.add("region", com.google.gson.JsonNull.INSTANCE);

        Matcher points = OVERALL_POINTS.matcher(html);
        String exactPoints = points.find() ? points.group(1) : "0";
        jsonObject.addProperty("points", (int) Double.parseDouble(exactPoints)); // SuperProfile expects an int
        jsonObject.addProperty("points_exact", exactPoints);
        jsonObject.addProperty("overall", 0);

        Matcher overall = OVERALL_TIER.matcher(html);
        if (overall.find())
            jsonObject.addProperty("overall_tier", overall.group(1).toUpperCase(Locale.ROOT));

        jsonObject.add("rankings", rankings);
        return jsonObject.toString();
    }

    @Override
    protected Component getRegionText() {
        return Icons.colorText("N/A", "unknown");
    }

    @Override
    protected Component getRegionTooltip() {
        return Icons.colorText("MCPVP doesn't publish player regions", "unknown");
    }

    @Override
    protected Component getOverallText() {
        if (overallTier == null)
            return Icons.colorText("Unranked", "unknown");
        return Icons.colorText(overallTier, "mcpvp_" + overallTier.toLowerCase(Locale.ROOT));
    }

    @Override
    protected Component getOverallTooltip() {
        String text = (overallTier == null ? "Overall: unranked" : "Overall tier: " + overallTier) + "\n\nPoints: " + mcpvpPoints;
        return Component.literal(text).setStyle(getOverallText().getStyle());
    }
}
