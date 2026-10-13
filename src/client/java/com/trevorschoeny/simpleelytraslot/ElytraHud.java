package com.trevorschoeny.simpleelytraslot;

import com.trevlar.menukit.api.element.ItemDisplay;
import com.trevlar.menukit.api.hud.HudPanel;
import com.trevlar.menukit.api.panel.InsideRegion;
import com.trevlar.menukit.api.panel.PanelStyle;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A small elytra icon left of the hotbar while the slot holds one, or while one is worn
 * in the chest slot, so it means the same thing however the wings were equipped. Icon
 * only, no count. It sits one row above Simple Totem Slot's icon, so the two stack.
 */
final class ElytraHud {

    private ElytraHud() {}

    // BOTTOM_CENTER follows the hotbar across GUI scales; the offsets put the 12px
    // icon about 2px left of the hotbar, one row (13px) above the totem icon's spot
    // (Inventory Max's layout).
    // ponytail: a fixed row, so with no totem mod the elytra icon keeps a gap below it; a shared HUD column if that bothers anyone.
    private static final int ICON_SIZE = 12;
    private static final int OFFSET_X = -99;
    private static final int OFFSET_Y = 1 - 13;

    static void register() {
        HudPanel.builder(ElytraSlot.PANEL_ID + "_hud")
                .region(InsideRegion.BOTTOM_CENTER).offset(OFFSET_X, OFFSET_Y)
                .padding(0).autoSize()
                .style(PanelStyle.NONE)
                .visibleWhen(() -> ElytraSlotSettings.hudIcon() && !shown().isEmpty())
                .element(ItemDisplay.builder().at(0, 0).item(ElytraHud::shown)
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
