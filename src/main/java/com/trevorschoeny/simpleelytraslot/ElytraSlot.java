package com.trevorschoeny.simpleelytraslot;

import com.trevlar.slotkit.api.group.SlotGroupCategory;
import com.trevlar.slotkit.api.group.SlotGroupId;
import com.trevlar.slotkit.api.slot.ParityGroup;
import com.trevlar.slotkit.api.slot.SlotSpec;
import com.trevlar.slotkit.api.storage.Storage;
import com.trevlar.slotkit.api.storage.StorageAttachment;
import com.trevlar.slotkit.api.storage.StorageAttachment.PlayerStorageAttachment;
import com.trevlar.slotkit.api.window.GatingContext;
import com.trevlar.slotkit.api.window.SlotGate;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The elytra slot: its storage and what it accepts. The panel that shows it is the
 * client's {@code ElytraSlotPanel}.
 *
 * <p>One real synced slot backed by a 1-stack player attachment, declared as a SlotKit
 * {@link ParityGroup} at common init, so SlotKit builds it on every container menu on
 * both sides with no mixin of ours. Vanilla's equipment loops never see it, so the
 * two vanilla mechanics it needs are added by mixin: the glide
 * ({@code ElytraSlotGlideMixin}) and the wings on the back (the client
 * {@code ElytraSlot*} render mixins). Curse of Binding and Mending are SlotKit's
 * (declared on the group), and the death drop is SlotKit's attachment default, which
 * follows vanilla and keepInventory.
 *
 * <p>Kept the same shape as Simple Totem Slot's {@code TotemSlot}: the two mods share
 * one column above the offhand and differ only in the item and the order number.
 */
public final class ElytraSlot {

    private ElytraSlot() {}

    /**
     * The group's owner id, also the client panel's id. SlotKit derives the group's id
     * from it ({@link #GROUP_ID}); it was the 1.x container panel id, so the id is unchanged.
     */
    public static final String PANEL_ID = SimpleElytraSlot.MOD_ID + ":elytra";
    public static final String GROUP = "slot";

    /**
     * The slot's category: its own, not {@code PLAYER_INVENTORY} or {@code PLAYER_ARMOR}.
     * A search for spares (a restock, a firework search) must not take the elytra a
     * player is flying on, and the armor category means vanilla's four slots.
     */
    public static final SlotGroupCategory CATEGORY = new SlotGroupCategory(SimpleElytraSlot.MOD_ID, "elytra");

    /**
     * Order in the column above the offhand: lower sits nearer the offhand. Totem 10,
     * elytra 20, room for a firework slot. The numbers are shared across the mods in
     * that column, so change them together.
     */
    public static final int COLUMN_ORDER = 20;

    /** The slot's group, for SlotKit lookups (operations, Reach, locks). */
    public static final SlotGroupId.Created GROUP_ID = ParityGroup.idOf(PANEL_ID, GROUP);

    /** The faint elytra outline on the empty slot ({@code textures/gui/sprites/container/slot/elytra.png}). */
    public static final Identifier EMPTY_ICON =
            Identifier.fromNamespaceAndPath(SimpleElytraSlot.MOD_ID, "container/slot/elytra");

    /** The elytra, saved with the player on the server. Drops on death as vanilla gear does. */
    private static PlayerStorageAttachment<NonNullList<ItemStack>> STORAGE;

    /** The registered group; the client panel shows its slot. Set by {@link #register}. */
    private static ParityGroup SLOT;

    public static ParityGroup slot() { return SLOT; }

    /** Call once from the main entrypoint, on both sides. */
    public static void register() {
        STORAGE = StorageAttachment.playerAttached(SimpleElytraSlot.MOD_ID, "elytra", 1);

        SLOT = ParityGroup.register(PANEL_ID, SlotSpec.at(GROUP, CATEGORY)
                .storage(p -> STORAGE.bind(p))
                .label("Elytra slot")
                .gate(GATE)
                .binding(true)
                // Worn gear, so XP orbs mend it as vanilla mends worn armor.
                .mending(true)
                // Shift-clicked elytra land here first. With this slot full, a second
                // one goes where vanilla sends it, the chest slot included.
                .shiftClickIn(true)
                // The outline is vanilla's own getNoItemIcon pass (SlotSpec.emptyIcon);
                // no mixin of ours needed.
                .emptyIcon(EMPTY_ICON));
    }

    /**
     * Elytra only, one at a time. Placing is refused while the mod is off, which also
     * stops shift-click routing into the hidden slot; taking out is always allowed
     * (binding aside).
     */
    private static final SlotGate GATE = new SlotGate() {
        @Override public boolean mayPlace(ItemStack stack, GatingContext ctx) {
            return ElytraSlotSettings.enabled() && isElytra(stack);
        }
        @Override public boolean mayPickup(Player player, GatingContext ctx) { return true; }
        @Override public int maxStackSize(ItemStack stack, int vanillaMax) { return Math.min(1, vanillaMax); }
    };

    /**
     * True for an elytra, the only item the slot takes. A literal item check rather than
     * the glider component, on purpose: the mod is called Elytra Slot.
     */
    public static boolean isElytra(ItemStack stack) {
        return stack.is(Items.ELYTRA);
    }

    /** The elytra in the slot, or EMPTY. A copy: change it through {@link #wear}. */
    public static ItemStack get(Player player) {
        return STORAGE.bind(player).getStack(0);
    }

    /**
     * Wears the slot's elytra down by {@code amount}, as vanilla wears a worn glider.
     * The storage read is a detached copy, so the damage only sticks (and syncs to the
     * client) once {@code setStack} writes it back. Vanilla applies the damage only on
     * the server, so {@code entity} must be server-side for it to take effect.
     */
    public static void wear(Player player, LivingEntity entity, int amount) {
        Storage storage = STORAGE.bind(player);
        ItemStack elytra = storage.getStack(0);
        if (elytra.isEmpty()) return;
        elytra.hurtAndBreak(amount, entity, EquipmentSlot.CHEST);
        storage.setStack(0, elytra);
    }
}
