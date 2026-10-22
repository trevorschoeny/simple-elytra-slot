package com.trevorschoeny.simpleelytraslot.ip;

import com.trevorschoeny.simpleelytraslot.ElytraSlotSettings;
import com.trevorschoeny.simpleelytraslot.SimpleElytraSlot;

import com.trevorschoeny.inventoryplus.api.InventoryPlusApi;

import com.trevlar.menukit.api.element.Button;
import com.trevlar.menukit.api.element.Checkbox;
import com.trevlar.menukit.api.element.Divider;
import com.trevlar.menukit.api.element.Flow;
import com.trevlar.menukit.api.element.PanelElement;
import com.trevlar.menukit.api.element.Tabs;
import com.trevlar.menukit.api.element.TextLabel;
import com.trevlar.menukit.api.element.TextRole;
import com.trevlar.menukit.api.element.Toggle;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * The Elytra Slot tab in Inventory Plus's settings menu, laid out as Simple Totem Slot's
 * tab: the frame IP's tabs have (title, description, Reset to Defaults, On/Off, a line),
 * then the settings, grayed while the mod is off. Reads {@link ElytraSlotSettings} every
 * frame and writes through its setters, which save.
 */
final class ElytraSettingsTab {

    private ElytraSettingsTab() {}

    /** The tab's id: IP's column switch opens it on Ctrl-click. */
    static final String TAB_ID = SimpleElytraSlot.MOD_ID;

    /** A divider's color on the inventory look, as on IP's own tabs. */
    private static final int LINE_COLOR = 0xFF8B8B8B;

    /** Call once from client init, before the menu can be opened. */
    static void register() {
        Tabs.addTo(InventoryPlusApi.SETTINGS_MENU,
                Tabs.tab(TAB_ID).label(Component.literal("Elytra Slot")).body(ElytraSettingsTab::body));
        // General's Reset everything resets this tab too.
        InventoryPlusApi.registerSettingsReset(ElytraSlotSettings::reset);
    }

    private static List<PanelElement> body() {
        BooleanSupplier off = () -> !ElytraSlotSettings.enabled();
        return List.of(
                TextLabel.builder().at(0, 0)
                        .text(Component.literal("Elytra Slot").withStyle(ChatFormatting.BOLD))
                        .role(TextRole.HEADING).build(),
                TextLabel.builder().at(0, 14)
                        .text(Component.literal("A slot for your elytra, so you can wear a chestplate "
                                + "and still glide."))
                        .role(TextRole.BODY).build(),
                Flow.builder().at(0, 30).gap(10, 4)
                        .add(Button.builder().label(Component.literal("Reset to Defaults")).size(0, 16)
                                .onClick(() -> InventoryPlusApi.confirmInSettings("Reset Elytra Slot to defaults?",
                                        "Every setting on this tab goes back to how a fresh install has it.",
                                        ElytraSlotSettings::reset))
                                .build())
                        .add(Toggle.builder().size(40, 16)
                                .state(ElytraSlotSettings::enabled, ElytraSlotSettings::setEnabled)
                                .label(() -> Component.literal(ElytraSlotSettings.enabled() ? "On" : "Off")).build())
                        .build(),
                Divider.horizontal().at(0, 50).color(LINE_COLOR).build(),
                checkbox(56, "Show the elytra slot",
                        ElytraSlotSettings::showSlot, ElytraSlotSettings::setShowSlot, off),
                checkbox(72, "Show the slot's elytra on players",
                        ElytraSlotSettings::showWings, ElytraSlotSettings::setShowWings, off),
                checkbox(88, "Mending repairs the slot's elytra",
                        ElytraSlotSettings::mending, ElytraSlotSettings::setMending, off),
                checkbox(104, "Show an elytra icon beside the hotbar",
                        ElytraSlotSettings::hudIcon, ElytraSlotSettings::setHudIcon, off),
                checkbox(120, "Show the Elytra Slot button",
                        ElytraSlotSettings::showButton, ElytraSlotSettings::setShowButton, off));
    }

    private static PanelElement checkbox(int y, String label, BooleanSupplier get, Consumer<Boolean> set,
                                         BooleanSupplier off) {
        return Checkbox.builder().at(0, y).label(Component.literal(label)).state(get, set)
                .disabledWhen(off).build();
    }
}
