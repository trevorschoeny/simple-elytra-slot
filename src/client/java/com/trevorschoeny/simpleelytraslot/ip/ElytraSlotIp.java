package com.trevorschoeny.simpleelytraslot.ip;

import com.trevorschoeny.simpleelytraslot.ElytraSlotSettings;
import com.trevorschoeny.simpleelytraslot.SimpleElytraSlot;

import com.trevorschoeny.inventoryplus.api.InventoryPlusApi;

import net.minecraft.resources.Identifier;

/**
 * Everything Simple Elytra Slot does with Inventory Plus, loaded only when IP is: the
 * Elytra Slot tab in IP's settings menu, and the mod's On/Off in IP's inventory column,
 * hidden until "Show the Elytra Slot button" is on (Ctrl-click opens the tab). Locks and
 * Reach need nothing here: IP finds the slot through SlotKit.
 */
public final class ElytraSlotIp {

    private ElytraSlotIp() {}

    public static void register() {
        ElytraSettingsTab.register();
        InventoryPlusApi.addFeatureSwitch(SimpleElytraSlot.MOD_ID, "Elytra Slot",
                // Placeholder art, to be replaced with the elytra slot's own button.
                Identifier.fromNamespaceAndPath(SimpleElytraSlot.MOD_ID, "elytra_slot_button"),
                ElytraSlotSettings::enabled, ElytraSlotSettings::setEnabled,
                ElytraSlotSettings::showButton, null);
    }
}
