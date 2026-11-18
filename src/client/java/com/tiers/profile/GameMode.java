package com.tiers.profile;

import com.google.gson.JsonObject;
import com.tiers.misc.Mode;
import com.tiers.textures.ColorControl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

public class GameMode {
    public Status status = Status.SEARCHING;

    private String tier;
    private String peakTier;
    private String attained;

    public Component displayedTier;
    private String displayedTierUnformatted;
    public Component displayedPeakTier;
    private String displayedPeakTierUnformatted;
    public Component tierTooltip;
    public Component peakTierTooltip;

    public final Mode gamemode;
    public final String parsingName;
    public boolean hasPeak;
    public boolean drawn;

    public GameMode(Mode gamemode, String parsingName) {
        this.gamemode = gamemode;
        this.parsingName = parsingName;
    }

    /**
     * Tierlists that publish a ready-made tier code (e.g. MCPVP's "MT2" or "LT6") instead of
     * the tier + pos pair used by MCTiers / PvPTiers / Subtiers. Order: best first.
     */
    private static final java.util.List<String> CODE_TIER_ORDER = java.util.List.of(
            "HT1", "MT1", "LT1", "HT2", "MT2", "LT2", "HT3", "MT3", "LT3",
            "HT4", "MT4", "LT4", "HT5", "MT5", "LT5", "HT6", "MT6", "LT6", "ST6");
    private boolean codeBased;

    private void parseTierCode(String code) {
        codeBased = true;
        code = code.toUpperCase(Locale.ROOT).trim();
        if (!CODE_TIER_ORDER.contains(code)) {
            status = Status.NOT_EXISTING;
            return;
        }

        tier = code.substring(2);
        peakTier = tier;
        displayedTierUnformatted = code;
        displayedTier = Component.literal(code).setStyle(Style.EMPTY.withColor(getTierColor(code)));

        String level = switch (code.charAt(0)) {
            case 'H' -> "High ";
            case 'M' -> "Mid ";
            case 'L' -> "Low ";
            default -> "Sub ";
        };
        tierTooltip = Component.literal(level + "Tier " + tier + " (" + code + ")").setStyle(displayedTier.getStyle());
        hasPeak = false;
        status = Status.READY;
    }

    public void parseTiers(JsonObject jsonObject) {
        if (jsonObject.has("code") && !jsonObject.get("code").isJsonNull()) {
            parseTierCode(jsonObject.get("code").getAsString());
            return;
        }

        String pos;
        String peakPos;
        String retired;

        if (jsonObject.has("tier") && jsonObject.has("pos") &&
                jsonObject.has("attained") && jsonObject.has("retired")) {
            tier = jsonObject.get("tier").getAsString();
            pos = jsonObject.get("pos").getAsString();

            if (jsonObject.get("peak_tier").isJsonNull())
                peakTier = tier;
            else
                peakTier = jsonObject.get("peak_tier").getAsString();

            if (jsonObject.get("peak_pos").isJsonNull())
                peakPos = pos;
            else
                peakPos = jsonObject.get("peak_pos").getAsString();

            attained = jsonObject.get("attained").getAsString();
            retired = jsonObject.get("retired").getAsString();
        } else {
            status = Status.NOT_EXISTING;
            return;
        }

        displayedTierUnformatted = "";
        if (retired.equalsIgnoreCase("true"))
            displayedTierUnformatted = "R";
        displayedTierUnformatted += pos.equalsIgnoreCase("0") ? "HT" : "LT";
        displayedTierUnformatted += tier;

        displayedTier = Component.literal(displayedTierUnformatted).setStyle(Style.EMPTY.withColor(getTierColor(displayedTierUnformatted)));
        tierTooltip = getTierTooltip();

        if (!tier.equalsIgnoreCase(peakTier) || !(pos.equalsIgnoreCase(peakPos))) {
            displayedPeakTierUnformatted = peakPos.equalsIgnoreCase("0") ? "HT" : "LT";
            displayedPeakTierUnformatted += peakTier;

            displayedPeakTier = Component.literal(displayedPeakTierUnformatted).setStyle(Style.EMPTY.withColor(getTierColor(displayedPeakTierUnformatted)));
            peakTierTooltip = getPeakTierTooltip();

            hasPeak = true;
        }

        status = Status.READY;
    }

    private Component getTierTooltip() {
        String tierTooltipString = "";
        if (displayedTierUnformatted.contains("R"))
            tierTooltipString += "Retired ";

        if (displayedTierUnformatted.contains("H"))
            tierTooltipString += "High ";
        else tierTooltipString += "Low ";

        tierTooltipString += "Tier " + tier + "\n\nPoints: " + getTierPoints(false) + "\nAttained: " + LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(attained)), ZoneId.systemDefault()).toString().replace("T", " ");

        return Component.literal(tierTooltipString).setStyle(Style.EMPTY.withColor(getTierColor(displayedTierUnformatted)));
    }

    private Component getPeakTierTooltip() {
        String peakTierTooltipString = "Peak: ";
        if (displayedPeakTierUnformatted.contains("R"))
            peakTierTooltipString += "Retired ";

        if (displayedPeakTierUnformatted.contains("H"))
            peakTierTooltipString += "High ";
        else peakTierTooltipString += "Low ";

        peakTierTooltipString += "Tier " + peakTier + "\n\nPoints: " + getTierPoints(true);

        return Component.literal(peakTierTooltipString).setStyle(Style.EMPTY.withColor(getTierColor(displayedPeakTierUnformatted)));
    }

    public int getTierPoints(boolean peak) {
        if (status == Status.NOT_EXISTING) return 0;
        if (codeBased) // No official per-kit points: rank by position in the tier ladder (only compared within one tierlist)
            return displayedTierUnformatted == null ? 0 : CODE_TIER_ORDER.size() - CODE_TIER_ORDER.indexOf(displayedTierUnformatted);
        String tier = displayedTierUnformatted;
        if (peak)
            tier = displayedPeakTierUnformatted;
        tier = tier.replace("R", "");

        if (tier.equalsIgnoreCase("HT1")) return 60;
        else if (tier.equalsIgnoreCase("LT1")) {
            if (gamemode.toString().contains("PVPTIERS"))
                return 44;
            else
                return 45;
        } else if (tier.equalsIgnoreCase("HT2")) {
            if (gamemode.toString().contains("PVPTIERS"))
                return 28;
            else
                return 30;
        } else if (tier.equalsIgnoreCase("LT2")) {
            if (gamemode.toString().contains("PVPTIERS"))
                return 16;
            else
                return 20;
        } else if (tier.equalsIgnoreCase("HT3")) return 10;
        else if (tier.equalsIgnoreCase("LT3")) return 6;
        else if (tier.equalsIgnoreCase("HT4")) return 4;
        else if (tier.equalsIgnoreCase("LT4")) return 3;
        else if (tier.equalsIgnoreCase("HT5")) return 2;
        else if (tier.equalsIgnoreCase("LT5")) return 1;
        return 0;
    }

    private int getTierColor(String tier) {
        if (gamemode.getCategory() == Mode.Category.MCPVP)
            return ColorControl.getColor("mcpvp_" + tier.toLowerCase(Locale.ROOT));
        if (tier.contains("R"))
            return ColorControl.getColor("retired");
        return ColorControl.getColor(tier.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return "\nGameMode{" +
                "\nstatus=" + status +
                "\ngamemode=" + (gamemode != null ? gamemode.name() : "null") +
                "\ntier=" + (tier != null ? tier : "null") +
                "\npeakTier=" + (peakTier != null ? peakTier : "null") +
                "\nattained=" + (attained != null ? attained : "null") +
                "\ndisplayedTier=" + (displayedTier != null ? displayedTier.getString() : "null") +
                "\ndisplayedTierUnformatted=" + (displayedTierUnformatted != null ? displayedTierUnformatted : "null") +
                "\ndisplayedPeakTier=" + (displayedPeakTier != null ? displayedPeakTier.getString() : "null") +
                "\ndisplayedPeakTierUnformatted=" + (displayedPeakTierUnformatted != null ? displayedPeakTierUnformatted : "null") +
                "\ntierTooltip=" + (tierTooltip != null ? tierTooltip.getString() : "null") +
                "\npeakTierTooltip=" + (peakTierTooltip != null ? peakTierTooltip.getString() : "null") +
                "\nparsingName=" + (parsingName != null ? parsingName : "null") +
                "\nhasPeak=" + hasPeak +
                "\ndrawn=" + drawn +
                "\n}";
    }
}