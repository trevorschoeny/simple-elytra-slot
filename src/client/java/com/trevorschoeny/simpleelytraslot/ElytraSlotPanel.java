package com.trevorschoeny.simpleelytraslot;

import com.trevlar.menukit.api.panel.Contribution;
import com.trevlar.menukit.api.panel.ModeScope;
import com.trevlar.menukit.api.panel.OutsideRegion;
import com.trevlar.menukit.api.panel.PanelPosition;
import com.trevlar.menukit.api.panel.PanelStyle;
import com.trevlar.menukit.api.panel.SharedPanels;
import com.trevlar.slotkit.api.group.SlotGroupCategory;
import com.trevlar.slotkit.api.slot.ClientSlots;
import com.trevlar.slotkit.api.slot.SlotFlowElement;
import com.trevlar.slotkit.api.slot.SlotPlacement;

/**
 * The elytra slot's place on screen: its part of {@code trevlar:gear}, the panel it shares
 * with Simple Totem Slot. MenuKit builds that panel from whichever of the two mods are
 * installed, lays their slots out in one column by order (elytra 10, then totem 20), and
 * leaves out a hidden one, so the other closes the gap.
 *
 * <ul>
 *   <li><b>Survival and container screens:</b> the column sits on the offhand, the totem
 *       nearest it. On a screen without the offhand (a chest) it isn't placed.</li>
 *   <li><b>Creative's inventory tab:</b> its offhand sits under the tab row, with no room
 *       above for two slots, so the column goes right of the armour, centred on it. On an
 *       item tab there's no armour, so no column.</li>
 * </ul>
 *
 * <p>The declaration (placements, style, axis, gap) must stay identical to Simple Totem
 * Slot's, which carries the same one, so the panel builds the same with either mod alone.
 */
final class ElytraSlotPanel {

    private ElytraSlotPanel() {}

    /** The shared panel's id, agreed with Simple Totem Slot. */
    static final String SHARED_ID = "trevlar:gear";

    /** Where the elytra slot sits in the shared column: first, above the totem (20). */
    static final int ORDER = 10;

    static void register() {
        SharedPanels.join(SHARED_ID, Contribution.builder(ORDER, ModeScope.BOTH)
                .add(SlotFlowElement.of(ElytraSlot.slot()))
                // Hidden while the mod is off or the slot is set hidden, and wherever the
                // server has no slot to show.
                .visibleWhen(() -> ElytraSlotSettings.enabled() && ElytraSlotSettings.showSlot()
                        && ClientSlots.isShown(ElytraSlot.GROUP_ID))
                // The shared declaration. gap(0) on the placements and the flow keeps the
                // slots on vanilla's 18 px pitch, against the box and between each other.
                .in(ModeScope.SURVIVAL, SlotPlacement.group(SlotGroupCategory.PLAYER_OFFHAND,
                        PanelPosition.region(OutsideRegion.TOP_CENTER).gap(0)))
                .in(ModeScope.CREATIVE, SlotPlacement.group(SlotGroupCategory.PLAYER_ARMOR,
                        PanelPosition.region(OutsideRegion.RIGHT_CENTER).gap(0)))
                .style(PanelStyle.NONE)
                .vertical()
                .gap(0)
                .build());
    }
}
