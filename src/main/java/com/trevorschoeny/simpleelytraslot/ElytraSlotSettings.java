package com.trevorschoeny.simpleelytraslot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple Elytra Slot's settings, the one place they live: plain fields, loaded from and
 * saved to {@code config/simpleelytraslot/config.json}, the same shape as Simple Totem
 * Slot's. Every setter saves.
 *
 * <p>Each setting is read on the side whose code runs. The server reads On/Off, Show the
 * slot (placing into it) and Mending; the client reads On/Off, Show the slot (the panel),
 * the wings and the HUD icon. In singleplayer both read this one file; on a dedicated
 * server the server's own file decides the glide, placing and Mending.
 *
 * <p>Changed from the Elytra Slot tab in Inventory Plus's settings menu when IP is
 * installed. Without IP there's no menu and the defaults (or the file) apply.
 */
public final class ElytraSlotSettings {

    private ElytraSlotSettings() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // The defaults, named once so load and reset agree.
    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_SHOW_SLOT = true;
    private static final boolean DEFAULT_SHOW_WINGS = true;
    private static final boolean DEFAULT_MENDING = true;
    private static final boolean DEFAULT_HUD_ICON = true;
    private static final boolean DEFAULT_SHOW_BUTTON = false;

    // Off hides the slot and stops its glide, wings and mending; the elytra stays in the slot.
    private static volatile boolean enabled = DEFAULT_ENABLED;
    // Off hides the slot from the inventory while an elytra already in it still glides;
    // nothing new goes in until it's shown again.
    private static volatile boolean showSlot = DEFAULT_SHOW_SLOT;
    // Off stops drawing the slot's elytra on players (as wings) on this client. A cape
    // shows again in their place. Gliding is unaffected.
    private static volatile boolean showWings = DEFAULT_SHOW_WINGS;
    // Off keeps XP orbs from repairing a Mending elytra in the slot.
    private static volatile boolean mending = DEFAULT_MENDING;
    // The elytra icon beside the hotbar while the slot holds one (or one is worn).
    private static volatile boolean hudIcon = DEFAULT_HUD_ICON;
    // Inventory Plus's column switch for this mod. Hidden by default.
    private static volatile boolean showButton = DEFAULT_SHOW_BUTTON;

    public static boolean enabled()    { return enabled; }
    public static boolean showSlot()   { return showSlot; }
    public static boolean showWings()  { return showWings; }
    public static boolean mending()    { return mending; }
    public static boolean hudIcon()    { return hudIcon; }
    public static boolean showButton() { return showButton; }

    public static void setEnabled(boolean v)    { enabled = v; save(); }
    public static void setShowSlot(boolean v)   { showSlot = v; save(); }
    public static void setShowWings(boolean v)  { showWings = v; save(); }
    public static void setMending(boolean v)    { mending = v; save(); }
    public static void setHudIcon(boolean v)    { hudIcon = v; save(); }
    public static void setShowButton(boolean v) { showButton = v; save(); }

    /** Every setting back to its default, and saves: Reset to Defaults and IP's Reset everything. */
    public static void reset() {
        enabled = DEFAULT_ENABLED;
        showSlot = DEFAULT_SHOW_SLOT;
        showWings = DEFAULT_SHOW_WINGS;
        mending = DEFAULT_MENDING;
        hudIcon = DEFAULT_HUD_ICON;
        showButton = DEFAULT_SHOW_BUTTON;
        save();
    }

    private static Path filePath() {
        return FabricLoader.getInstance().getConfigDir().resolve(SimpleElytraSlot.MOD_ID).resolve("config.json");
    }

    /**
     * Reads the file once, at common init (both sides). A missing file, or a missing or
     * unreadable key, leaves that setting at its default. Nothing is written until a
     * setting changes.
     */
    public static void load() {
        Path path = filePath();
        if (!Files.exists(path)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            if (root.has("enabled")) enabled = root.get("enabled").getAsBoolean();
            if (root.has("showSlot")) showSlot = root.get("showSlot").getAsBoolean();
            if (root.has("showWings")) showWings = root.get("showWings").getAsBoolean();
            if (root.has("mending")) mending = root.get("mending").getAsBoolean();
            if (root.has("hudIcon")) hudIcon = root.get("hudIcon").getAsBoolean();
            if (root.has("showButton")) showButton = root.get("showButton").getAsBoolean();
        } catch (IOException | JsonSyntaxException | IllegalStateException | ClassCastException
                 | UnsupportedOperationException ex) {
            SimpleElytraSlot.LOGGER.error("[simpleelytraslot] failed to read {}, using defaults", path, ex);
        }
    }

    private static void save() {
        Path path = filePath();
        try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("enabled", enabled);
            root.addProperty("showSlot", showSlot);
            root.addProperty("showWings", showWings);
            root.addProperty("mending", mending);
            root.addProperty("hudIcon", hudIcon);
            root.addProperty("showButton", showButton);
            Files.writeString(path, GSON.toJson(root));
        } catch (IOException ex) {
            SimpleElytraSlot.LOGGER.error("[simpleelytraslot] failed to write {}, settings won't persist", path, ex);
        }
    }
}
