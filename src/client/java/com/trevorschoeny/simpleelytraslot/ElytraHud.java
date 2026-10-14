package com.trevorschoeny.simpleelytraslot;

import com.trevlar.menukit.api.element.ItemDisplay;
import com.trevlar.menukit.api.hud.HudPanel;
import com.trevlar.menukit.api.panel.OutsideRegion;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A small elytra icon left of the hotbar while the slot holds one, or while one is worn
 * in the chest slot, so it means the same thing however the wings were equipped. Icon
 * only, no count. Priority 20 stacks it above Simple Totem Slot's icon (priority 10)
 * in the same region (menukit docs/recipes.md, "Put an icon beside the hotbar").
 */
final class ElytraHud {

    private ElytraHud() {}

    private static final int ICON_SIZE = 12;

    static void register() {
        HudPanel.builder(ElytraSlot.PANEL_ID + "_hud")
                .region(OutsideRegion.LEFT_ALIGN_BOTTOM)
                .priority(20)
                .offset(0, -3)
                .visibleWhen(() -> ElytraSlotSettings.hudIcon() && !shown().isEmpty())
                .element(ItemDisplay.builder().item(ElytraHud::shown)
                        .size(ICON_SIZE, ICON_SIZE).hideCount().hideDurability().build())
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
