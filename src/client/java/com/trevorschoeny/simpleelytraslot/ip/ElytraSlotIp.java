package com.trevorschoeny.simpleelytraslot.ip;

import com.trevorschoeny.simpleelytraslot.ElytraSlotSettings;
import com.trevorschoeny.simpleelytraslot.SimpleElytraSlot;

import com.trevorschoeny.inventoryplus.api.InventoryPlusApi;

import net.minecraft.resources.Identifier;

/**
 * Everything Simple Elytra Slot does with Inventory Plus, loaded only when IP is: the
 * mod's On/Off in IP's inventory column, hidden until "Show the Elytra Slot button" is
 * on. Locks and Reach need nothing here: IP finds the slot through SlotKit. No elytra
 * restock in 1.0.0.
 */
public final class ElytraSlotIp {

    private ElytraSlotIp() {}

    public static void register() {
        InventoryPlusApi.addFeatureSwitch(SimpleElytraSlot.MOD_ID, "Elytra Slot",
                // Still Inventory Max's equipment button art until the elytra button art exists.
                Identifier.fromNamespaceAndPath(SimpleElytraSlot.MOD_ID, "elytra_slot_button"),
                ElytraSlotSettings::enabled, ElytraSlotSettings::setEnabled,
                ElytraSlotSettings::showButton, null);
    }
}
