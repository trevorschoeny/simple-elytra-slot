package com.trevorschoeny.simpleelytraslot;

import com.trevlar.menukit.api.hud.HudPanel;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The slot's elytra on the HUD while it holds one, or while one is worn in the chest slot,
 * so it means the same thing however the wings were equipped: in a hotbar slot scaled to
 * fit MenuKit's status column, between the hearts and the hunger bar, just above the XP
 * level number. Priority 20, above Simple Totem Slot's (priority 10), as the two slots
 * stack in the inventory. No count, durability left on.
 */
final class ElytraHud {

    private ElytraHud() {}

    static void register() {
        HudPanel.builder(ElytraSlot.PANEL_ID + "_hud")
                .statusColumn()
                .priority(20)
                .visibleWhen(() -> ElytraSlotSettings.hudIcon() && !shown().isEmpty())
                // The column's shared scale, so every mod's icons match.
                .slot(0, 0).item(ElytraHud::shown).hideCount().scale(HudPanel.STATUS_SCALE).done()
                .build();
    }

    /** The slot's elytra, else a worn chest-slot elytra, else EMPTY (mod off, no player). */
    private static ItemStack shown() {
        Player p = Minecraft.getInstance().player;
        if (p == null || !ElytraSlotSettings.enabled()) return ItemStack.EMPTY;
        ItemStack slot = ElytraSlot.get(p);
        if (!slot.isEmpty()) return slot;
        ItemStack chest = p.getItemBySlot(EquipmentSlot.CHEST);
        return ElytraSlot.isElytra(chest) ? chest : ItemStack.EMPTY;
    }
}
