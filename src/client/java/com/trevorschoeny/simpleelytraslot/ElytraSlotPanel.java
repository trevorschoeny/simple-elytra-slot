package com.trevorschoeny.simpleelytraslot;

import com.trevlar.menukit.api.panel.OutsideRegion;
import com.trevlar.menukit.api.panel.Panel;
import com.trevlar.menukit.api.panel.PanelPosition;
import com.trevlar.menukit.api.panel.PanelStyle;
import com.trevlar.slotkit.api.group.SlotGroupCategory;
import com.trevlar.slotkit.api.group.SlotGroupPanelAdapter;
import com.trevlar.slotkit.api.slot.ClientSlots;
import com.trevlar.slotkit.api.slot.SlotFlowElement;

/**
 * The panel that shows the elytra slot: a bare slot in the column above the offhand.
 *
 * <p>Every panel in that column is placed on the offhand by priority, lowest nearest,
 * and the gap closes when one is absent (no Simple Totem Slot: this slot sits directly
 * on the offhand). On a screen without the offhand (a chest) the panel isn't placed.
 */
final class ElytraSlotPanel {

    private ElytraSlotPanel() {}

    static void register() {
        Panel panel = Panel.builder(ElytraSlot.PANEL_ID)
                .add(SlotFlowElement.of(ElytraSlot.slot()))
                .style(PanelStyle.NONE)
                // gap(0) keeps stacked slot panels on vanilla's 18 px slot pitch.
                .position(PanelPosition.region(OutsideRegion.TOP_CENTER)
                        .priority(ElytraSlot.COLUMN_ORDER).gap(0))
                .build()
                // A slot isn't solid, so a transparent panel takes no clicks around it.
                .opaque(false)
                // Hidden while the mod is off, and wherever the server has no slot to show.
                .visibleWhen(() -> ElytraSlotSettings.enabled()
                        && ClientSlots.isShown(ElytraSlot.GROUP_ID));
        new SlotGroupPanelAdapter(panel, 0).on(SlotGroupCategory.PLAYER_OFFHAND);
    }
}
