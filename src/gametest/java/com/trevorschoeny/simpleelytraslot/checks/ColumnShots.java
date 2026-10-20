package com.trevorschoeny.simpleelytraslot.checks;

import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.ArrayList;
import java.util.List;

/**
 * The column above the offhand, on the survival inventory and creative's Survival Inventory tab:
 * where the elytra slot sits (checked), and how it looks (shot, for Trev). Run alone it checks the
 * elytra slot directly on the offhand; with {@code -PwithTotem} it checks the totem slot on the
 * offhand and the elytra slot on the totem slot, no gap. Shots are named for which it was:
 * {@code elytra-alone-*} or {@code elytra-with-totem-*}, each empty and filled.
 */
public class ColumnShots implements FabricClientGameTest {

    /** Vanilla's slot pitch: a slot's frame is 18 GUI pixels. */
    private static final int PITCH = 18;

    private final boolean totem = FabricLoader.getInstance().isModLoaded("simpletotemslot");
    private final String tag = totem ? "with-totem" : "alone";
    private final List<String> failures = new ArrayList<>();
    private ClientGameTestContext ctx;
    private TestServerContext server;

    @Override
    public void runTest(ClientGameTestContext ctx) {
        this.ctx = ctx;
        try (TestSingleplayerContext world = ctx.worldBuilder().create()) {
            server = world.getServer();
            world.getConnection().waitForChunksRender();
            ctx.getInput().resizeWindow(1920, 1080);
            ctx.runOnClient(mc -> { mc.options.guiScale().set(4); mc.resizeGui(); });
            for (String c : new String[]{"gamerule advance_time false", "gamerule show_advancement_messages false",
                    "time set 6000", "gamemode survival @a", "clear @a",
                    // Enchanted, so the shots also show whether the glint carries onto the wings.
                    "item replace entity @p inventory.0 with minecraft:elytra[enchantments={\"minecraft:unbreaking\":3}]",
                    "item replace entity @p inventory.1 with minecraft:totem_of_undying",
                    "item replace entity @p armor.chest with minecraft:diamond_chestplate",
                    "item replace entity @p hotbar.0 with minecraft:firework_rocket 16"}) command(c);

            survival("empty");
            creative("empty");

            // Fill the column the player's way: shift-click from the inventory, in survival.
            command("gamemode survival @a");
            openSurvival();
            Ui.shiftClick(ctx, Ui.inventory(9), "elytra");
            if (totem) Ui.shiftClick(ctx, Ui.inventory(10), "totem");
            ctx.setScreen(() -> null);
            ctx.waitTicks(2);

            survival("filled");
            creative("filled");
            thirdPerson();
        }
        if (!failures.isEmpty()) throw new AssertionError("column checks failed (" + tag + "):\n- " + String.join("\n- ", failures));
    }

    private void survival(String state) {
        command("gamemode survival @a");
        openSurvival();
        layout("survival");
        shoot("survival-" + state);
    }

    /** Creative's inventory, switched to its Survival Inventory tab, where the offhand is. */
    private void creative(String state) {
        command("gamemode creative @a");
        ctx.getInput().pressKey(o -> o.keyInventory);
        ctx.waitTicks(3);
        ctx.runOnClient(mc -> {
            if (!(mc.gui.screen() instanceof CreativeModeInventoryScreen screen))
                throw new AssertionError("the inventory key opened " + mc.gui.screen() + " in creative");
            CreativeModeTab inventoryTab = CreativeModeTabs.allTabs().stream()
                    .filter(t -> t.getType() == CreativeModeTab.Type.INVENTORY).findFirst().orElseThrow();
            ((FabricCreativeModeInventoryScreen) screen).setSelectedTab(inventoryTab);
        });
        ctx.waitTicks(3);
        layout("creative");
        shoot("creative-" + state);
    }

    /**
     * The column, read off the open screen. On the survival inventory: each slot in the offhand's
     * column, one pitch above the slot below it. Totem 10 sits on the offhand and elytra 20 on the
     * totem; with no totem slot the elytra slot closes the gap and sits on the offhand. On
     * creative's inventory tab the column sits right of the armor, centred on its height, at the
     * positions Simple Totem Slot's check expects too, and inside the window.
     */
    private void layout(String screen) {
        String problem = ctx.computeOnClient(mc -> {
            Slot elytra = Ui.find(mc, Ui.ELYTRA), totemSlot = Ui.find(mc, Ui.TOTEM);
            if (elytra == null || !elytra.isActive()) return "no elytra slot shown";
            if (totem && (totemSlot == null || !totemSlot.isActive())) return "no totem slot shown";
            if (screen.equals("creative")) {
                // Right of the armor, centred on its height: elytra over totem, or alone level with the offhand.
                String e = at(elytra, "elytra slot", 126, totem ? 11 : 20);
                if (e == null && totem) e = at(totemSlot, "totem slot", 126, 29);
                if (e == null) e = insideWindow(mc, elytra, "elytra slot");
                return e != null || !totem ? e : insideWindow(mc, totemSlot, "totem slot");
            }
            Slot offhand = Ui.find(mc, Ui.OFFHAND);
            if (offhand == null) return "no offhand slot";
            Slot below = offhand;
            if (totem) {
                String t = onTop(mc, totemSlot, offhand, "totem slot", "offhand");
                if (t != null) return t;
                below = totemSlot;
            }
            return onTop(mc, elytra, below, "elytra slot", totem ? "totem slot" : "offhand");
        });
        if (problem != null) failures.add(screen + ": " + problem);
    }

    /** The slot at (x, y) in the screen's own coordinates, or what's wrong. */
    private static String at(Slot slot, String name, int x, int y) {
        return slot.x == x && slot.y == y ? null : name + " at " + slot.x + "," + slot.y + ", expected " + x + "," + y;
    }

    /** The slot's 18 px frame lies inside the screen's window (its background image). */
    private static String insideWindow(Minecraft mc, Slot slot, String name) {
        int[] p = Ui.screenPos(mc, slot), w = Ui.window(mc);
        // A slot's frame is one pixel beyond its 16 px item square on each side.
        boolean inside = p[0] - 1 >= w[0] && p[1] - 1 >= w[1] && p[0] + 17 <= w[0] + w[2] && p[1] + 17 <= w[1] + w[3];
        return inside ? null : name + " frame at " + (p[0] - 1) + "," + (p[1] - 1) + " is outside the window "
                + w[0] + "," + w[1] + " " + w[2] + "x" + w[3];
    }

    private static String onTop(Minecraft mc, Slot upper, Slot lower, String upperName, String lowerName) {
        int[] u = Ui.screenPos(mc, upper), l = Ui.screenPos(mc, lower);
        if (u[0] != l[0] || u[1] != l[1] - PITCH)
            return upperName + " at " + u[0] + "," + u[1] + "; expected one slot above the " + lowerName
                    + " at " + l[0] + "," + (l[1] - PITCH);
        return null;
    }

    /** From behind in third person: the slot's wings, with their glint, over the chestplate. */
    private void thirdPerson() {
        command("gamemode survival @a");
        command("execute as @p at @s run tp @s ~ ~ ~ 0 10");
        ctx.runOnClient(mc -> mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK));
        ctx.waitTicks(10);
        ctx.takeScreenshot("elytra-" + tag + "-third-person");
        // Vanilla for comparison: the same enchanted elytra worn in the chest slot.
        command("item replace entity @p armor.chest with minecraft:elytra[enchantments={\"minecraft:unbreaking\":3}]");
        ctx.waitTicks(10);
        ctx.takeScreenshot("elytra-" + tag + "-third-person-vanilla-worn");
        command("item replace entity @p armor.chest with minecraft:diamond_chestplate");
        ctx.runOnClient(mc -> mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
    }

    private void openSurvival() {
        ctx.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
        ctx.waitTicks(3);
    }

    private void shoot(String name) {
        Ui.parkCursor(ctx);
        ctx.takeScreenshot("elytra-" + tag + "-" + name);
        ctx.setScreen(() -> null);
        ctx.waitTicks(2);
    }

    private void command(String c) {
        server.runCommand(c);
        ctx.waitTicks(2);
    }
}
