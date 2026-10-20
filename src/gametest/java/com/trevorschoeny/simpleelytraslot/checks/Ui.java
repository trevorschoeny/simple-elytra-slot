package com.trevorschoeny.simpleelytraslot.checks;

import com.mojang.blaze3d.platform.InputConstants;
import com.trevlar.slotkit.api.group.SlotGroupId;
import com.trevlar.slotkit.api.slot.CreatedSlot;
import com.trevlar.slotkit.api.slot.ParityGroup;
import com.trevorschoeny.simpleelytraslot.ElytraSlot;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * Screen helpers shared by the checks: find a slot on the open container screen, and click or
 * shift-click it the way a hand does, through the mouse.
 */
final class Ui {

    private Ui() {}

    /** A slot of this SlotKit group (the elytra slot, or Simple Totem Slot's), on any screen. */
    static Predicate<Slot> created(SlotGroupId.Created group) {
        return s -> { CreatedSlot c = CreatedSlot.of(s); return c != null && c.panelId().equals(group.panelId()); };
    }

    static final Predicate<Slot> ELYTRA = created(ElytraSlot.GROUP_ID);
    // By id rather than TotemSlot's constant, so these checks compile without Simple Totem Slot.
    static final Predicate<Slot> TOTEM = created(ParityGroup.idOf("simpletotemslot:totem", "slot"));

    /**
     * The player's offhand as shown, on any screen. Creative's menu carries the inventory
     * menu's offhand twice: its own wrapper (shown on the Survival Inventory tab) and a
     * hidden copy, so only the active one counts.
     */
    static final Predicate<Slot> OFFHAND = s -> s.container instanceof Inventory && s.getContainerSlot() == 40 && s.isActive();

    /** The player's inventory slot {@code i} (9..35 main, 0..8 hotbar). */
    static Predicate<Slot> inventory(int i) {
        return s -> s.container instanceof Inventory && s.getContainerSlot() == i;
    }

    /** The open screen's slot matching {@code p}, or null. */
    static Slot find(Minecraft mc, Predicate<Slot> p) {
        if (!(mc.gui.screen() instanceof AbstractContainerScreen<?> screen)) return null;
        for (Slot s : screen.getMenu().slots) if (p.test(s)) return s;
        return null;
    }

    static Slot require(Minecraft mc, Predicate<Slot> p, String what) {
        Slot s = find(mc, p);
        if (s == null) throw new AssertionError("no " + what + " on " + mc.gui.screen());
        return s;
    }

    /** Screen position of a slot's top-left corner, in GUI units. */
    static int[] screenPos(Minecraft mc, Slot s) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.gui.screen();
        return new int[]{ field(screen, "leftPos") + s.x, field(screen, "topPos") + s.y };
    }

    /** The open container screen's window: left, top, width, height, in GUI units. */
    static int[] window(Minecraft mc) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.gui.screen();
        return new int[]{ field(screen, "leftPos"), field(screen, "topPos"), field(screen, "imageWidth"), field(screen, "imageHeight") };
    }

    static void hover(ClientGameTestContext ctx, Predicate<Slot> p, String what) {
        double[] at = ctx.computeOnClient(mc -> {
            int[] pos = screenPos(mc, require(mc, p, what));
            var w = mc.getWindow();
            double f = (double) w.getScreenWidth() / w.getGuiScaledWidth();
            return new double[]{ (pos[0] + 8) * f, (pos[1] + 8) * f };
        });
        ctx.getInput().setCursorPos(at[0], at[1]);
        ctx.waitTick();
    }

    static void click(ClientGameTestContext ctx, Predicate<Slot> p, String what) {
        hover(ctx, p, what);
        ctx.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
        ctx.waitTick();
        ctx.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
        ctx.waitTicks(3);
    }

    /**
     * Fabric's TestInput clicks carry no modifiers, and 26.3 reads shift from the click itself,
     * so this presses through the same MouseHandler hook with GLFW's shift bit (1) set.
     */
    static void shiftClick(ClientGameTestContext ctx, Predicate<Slot> p, String what) {
        hover(ctx, p, what);
        for (int action : new int[]{ 1, 0 }) {
            ctx.runOnClient(mc -> ((net.fabricmc.fabric.mixin.client.gametest.input.MouseHandlerAccessor) mc.mouseHandler)
                    .invokeOnButton(mc.getWindow().handle(), new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 1), action));
            ctx.waitTick();
        }
        ctx.waitTicks(3);
    }

    /** Parks the cursor where it hovers nothing, so no tooltip covers a screenshot. */
    static void parkCursor(ClientGameTestContext ctx) {
        ctx.getInput().setCursorPos(2, 2);
        ctx.waitTick();
    }

    private static int field(Object o, String name) {
        try {
            Class<?> c = o.getClass();
            while (c != null) {
                try { Field f = c.getDeclaredField(name); f.setAccessible(true); return f.getInt(o); }
                catch (NoSuchFieldException e) { c = c.getSuperclass(); }
            }
            throw new AssertionError("no field " + name);
        } catch (IllegalAccessException e) { throw new AssertionError(e); }
    }
}
