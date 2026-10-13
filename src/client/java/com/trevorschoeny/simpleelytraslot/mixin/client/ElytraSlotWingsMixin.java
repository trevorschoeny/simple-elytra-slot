package com.trevorschoeny.simpleelytraslot.mixin.client;

import com.trevorschoeny.simpleelytraslot.ElytraHolder;

import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Draws the slot's elytra on the player's back through vanilla's own wings layer.
 *
 * <p>{@code WingsLayer.submit} reads {@code chestEquipment} and draws wings if that item
 * has a wings equipment asset. An elytra does and a chestplate doesn't, so a chestplate
 * plus a slotted elytra would show no wings. This redirects that one field read: when
 * the chest item isn't a glider but the slot holds one, the layer gets the slot's
 * elytra. Only the wings layer is touched (the body armor layer reads
 * {@code chestEquipment} elsewhere), so the chestplate still draws on the body. Both show.
 *
 * <p>A worn elytra wins: if the chest item <em>is</em> a glider, the read is left alone.
 */
@Mixin(WingsLayer.class)
public class ElytraSlotWingsMixin {

    @Redirect(
            // Full descriptor: WingsLayer also carries a synthetic bridge named `submit`
            // (erased EntityRenderState param), so a bare name would match two methods.
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;chestEquipment:Lnet/minecraft/world/item/ItemStack;",
                    opcode = Opcodes.GETFIELD))
    private ItemStack simpleElytraSlot$wings(HumanoidRenderState state) {
        ItemStack chest = state.chestEquipment;
        ItemStack slot = ((ElytraHolder) state).simpleElytraSlot$getElytra();
        if (!chest.has(DataComponents.GLIDER) && slot.has(DataComponents.GLIDER)) {
            return slot;                                 // chest isn't a glider but the slot's item is
        }
        return chest;                                    // vanilla, including a worn elytra winning
    }
}
