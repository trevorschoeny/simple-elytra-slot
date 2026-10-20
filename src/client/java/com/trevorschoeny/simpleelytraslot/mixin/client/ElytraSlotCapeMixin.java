package com.trevorschoeny.simpleelytraslot.mixin.client;

import com.trevorschoeny.simpleelytraslot.ElytraHolder;

import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hides the cape while the slot's elytra is drawn on the back, as vanilla hides it under a
 * worn elytra.
 *
 * <p>{@code CapeLayer.submit} skips the cape when {@code chestEquipment} has a wings layer.
 * With a chestplate on and an elytra in the slot, the chest item has none, so the cape would
 * draw through the wings {@link ElytraSlotWingsMixin} adds. This redirects that first read
 * ({@code ordinal = 0}) with the wings mixin's rule, so the cape layer sees the slot's elytra.
 * The second read, which offsets the cape over a chestplate, is left alone; it only runs when
 * the cape is drawn.
 */
@Mixin(CapeLayer.class)
public class ElytraSlotCapeMixin {

    @Redirect(
            // Full descriptor: CapeLayer also carries a synthetic bridge named `submit`.
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;chestEquipment:Lnet/minecraft/world/item/ItemStack;",
                    opcode = Opcodes.GETFIELD, ordinal = 0))
    private ItemStack simpleElytraSlot$wingsHideCape(AvatarRenderState state) {
        ItemStack chest = state.chestEquipment;
        ItemStack slot = ((ElytraHolder) state).simpleElytraSlot$getElytra();
        if (!chest.has(DataComponents.GLIDER) && slot.has(DataComponents.GLIDER)) {
            return slot;                                 // the slot's wings are drawn: no cape
        }
        return chest;                                    // vanilla
    }
}
