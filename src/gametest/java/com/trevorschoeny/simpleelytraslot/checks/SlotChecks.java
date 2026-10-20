package com.trevorschoeny.simpleelytraslot.checks;

import com.google.gson.JsonObject;
import com.trevorschoeny.simpleelytraslot.ElytraSlot;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import testgallery.SceneWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Checks for the standalone scene (test-gallery/scenes/simple-elytra-slot/standalone.json): every
 * station whose label states something readable (where an item ended up, whether the player is
 * gliding, an elytra's damage). Each fills the slot the way a player does, by shift-click from the
 * inventory, so the real path is what's tested. Binding runs last: a bound elytra can't come out.
 */
public class SlotChecks implements FabricClientGameTest {

    /** An elytra's durability is 432; at damage 431 one point is left. */
    private static final int LAST_POINT = 431;

    private ClientGameTestContext ctx;
    private SceneWorld.Opened scene;
    private final List<String> failures = new ArrayList<>();

    @Override
    public void runTest(ClientGameTestContext ctx) {
        this.ctx = ctx;
        try (SceneWorld.Opened scene = SceneWorld.open(ctx, "simple-elytra-slot", "standalone")) {
            this.scene = scene;
            command("gamerule fall_damage false");   // the flights end with a drop back to the station
            run("simpleelytraslot:gate", this::gate);
            run("simpleelytraslot:shift_click", this::shiftClick);
            run("simpleelytraslot:glide", this::glide);
            run("simpleelytraslot:stops_at_one", this::stopsAtOne);
            run("simpleelytraslot:worn_wins", this::wornWins);
            run("simpleelytraslot:mending", this::mending);
            run("simpleelytraslot:binding", this::binding);
        }
        if (!failures.isEmpty()) throw new AssertionError("elytra slot checks failed:\n- " + String.join("\n- ", failures));
    }

    /** Runs one station's check from a clean player, recording a failure instead of stopping. */
    private void run(String id, Runnable check) {
        JsonObject station = scene.station(id);
        try {
            scene.standAt(station, 30);
            for (String c : new String[]{"gamemode survival @p", "clear @p", "effect clear @p"}) command(c);
            emptySlot();
            check.run();
        } catch (AssertionError e) {
            failures.add(id + ": " + e.getMessage());
        } finally {
            ctx.setScreen(() -> null);
            ctx.takeScreenshot("elytra-check-" + id.substring(id.indexOf(':') + 1));
            scene.standAt(station, 30);   // back down from any flight
            ctx.waitTicks(5);
        }
    }

    // ── Stations ─────────────────────────────────────────────────────────

    /** Dirt is refused; an elytra is taken. */
    private void gate() {
        command("item replace entity @p inventory.0 with minecraft:dirt 16");
        command("item replace entity @p inventory.1 with minecraft:elytra");
        Ui.openInventory(ctx);
        Ui.click(ctx, Ui.inventory(9), "dirt stack");
        Ui.click(ctx, Ui.ELYTRA, "elytra slot");
        expect(slot().isEmpty(), "dirt went into the elytra slot");
        expect(ctx.computeOnClient(mc -> mc.player.containerMenu.getCarried().is(Items.DIRT)), "the dirt left the cursor");
        Ui.click(ctx, Ui.inventory(9), "dirt's slot");
        Ui.click(ctx, Ui.inventory(10), "elytra");
        Ui.click(ctx, Ui.ELYTRA, "elytra slot");
        expect(slot().is(Items.ELYTRA), "the elytra slot didn't take a dragged elytra");
    }

    /** A shift-clicked elytra lands in the slot, not the chest armor slot. */
    private void shiftClick() {
        fill("minecraft:elytra");
        expect(onServer(p -> p.getItemBySlot(EquipmentSlot.CHEST)).isEmpty(), "the elytra went to the chest armor slot");
    }

    /** With a chestplate on, the slot's elytra glides and wears down. */
    private void glide() {
        fill("minecraft:elytra");
        command("item replace entity @p armor.chest with minecraft:diamond_chestplate");
        expect(flyFor(45), "jumping in the air with the slot's elytra didn't start a glide");
        expect(slot().getDamageValue() > 0, "gliding didn't wear the slot's elytra");
    }

    /** At its last point of durability the slot's elytra won't glide, as vanilla's won't. */
    private void stopsAtOne() {
        fill("minecraft:elytra[damage=" + LAST_POINT + "]");
        expect(!flyFor(10), "an elytra with 1 durability left glided");
        expect(slot().getDamageValue() == LAST_POINT, "the last-point elytra was worn further");
    }

    /** An elytra worn in the chest slot is the one used; the slot's keeps its durability. */
    private void wornWins() {
        fill("minecraft:elytra");
        command("item replace entity @p armor.chest with minecraft:elytra");
        expect(flyFor(45), "no glide with two elytra");
        expect(onServer(p -> p.getItemBySlot(EquipmentSlot.CHEST)).getDamageValue() > 0, "the worn elytra wasn't worn down");
        expect(slot().getDamageValue() == 0, "the slot's elytra was worn while one was worn in the chest slot");
    }

    /** XP orbs repair a damaged Mending elytra in the slot. */
    private void mending() {
        fill("minecraft:elytra[damage=200,enchantments={\"minecraft:mending\":1}]");
        for (int i = 0; i < 5; i++) command("execute at @p run summon experience_orb ~ ~ ~ {Value:20}");
        ctx.waitTicks(40);
        expect(slot().getDamageValue() < 200, "XP orbs didn't repair the slot's Mending elytra");
    }

    /** A Curse of Binding elytra can't be taken out in survival, by click or shift-click. */
    private void binding() {
        fill("minecraft:elytra[enchantments={\"minecraft:binding_curse\":1}]");
        Ui.openInventory(ctx);
        Ui.click(ctx, Ui.ELYTRA, "elytra slot");
        expect(slot().is(Items.ELYTRA), "a bound elytra was picked out of the slot");
        expect(ctx.computeOnClient(mc -> mc.player.containerMenu.getCarried().isEmpty()), "a bound elytra came out onto the cursor");
        Ui.shiftClick(ctx, Ui.ELYTRA, "elytra slot");
        expect(slot().is(Items.ELYTRA), "a bound elytra was shift-clicked out of the slot");
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /** Puts {@code item} in the slot the player's way: into the inventory, then shift-click. */
    private void fill(String item) {
        command("item replace entity @p inventory.0 with " + item);
        Ui.openInventory(ctx);
        Ui.shiftClick(ctx, Ui.inventory(9), item);
        ctx.setScreen(() -> null);
        ctx.waitTicks(2);
        expect(slot().is(Items.ELYTRA), "shift-click didn't put " + item + " in the elytra slot");
    }

    /** Shift-clicks anything in the slot back out, so each station starts empty. */
    private void emptySlot() {
        if (slot().isEmpty()) return;
        Ui.openInventory(ctx);
        Ui.shiftClick(ctx, Ui.ELYTRA, "elytra slot");
        ctx.setScreen(() -> null);
        ctx.waitTicks(2);
        command("clear @p");
        expect(slot().isEmpty(), "couldn't empty the elytra slot before the check");
    }

    /**
     * Lifts the player high over the station, looking level, presses jump in the air as a
     * player does, and waits {@code ticks}. True if the server has the player gliding then.
     */
    private boolean flyFor(int ticks) {
        command("execute as @p at @s run tp @s ~ ~60 ~ 0 0");
        ctx.waitTicks(6);   // airborne and falling
        // Held a few ticks: the client starts a glide on a jump press it sees while airborne.
        ctx.getInput().holdKey(o -> o.keyJump);
        ctx.waitTicks(3);
        ctx.getInput().releaseKey(o -> o.keyJump);
        ctx.waitTicks(ticks);
        return onServer(ServerPlayer::isFallFlying);
    }

    /** The slot's elytra as the server holds it. */
    private ItemStack slot() { return onServer(ElytraSlot::get); }

    private <T> T onServer(Function<ServerPlayer, T> f) {
        return scene.world().getServer().computeOnServer(s -> f.apply(s.getPlayerList().getPlayers().get(0)));
    }

    private void command(String c) {
        scene.world().getServer().runCommand(c);
        ctx.waitTicks(2);
    }

    private static void expect(boolean ok, String failure) {
        if (!ok) throw new AssertionError(failure);
    }
}
