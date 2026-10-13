package com.trevorschoeny.simpleelytraslot.mixin.client;

import com.trevorschoeny.simpleelytraslot.ElytraHolder;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds the per-frame slot elytra to {@code HumanoidRenderState} (and so
 * {@code AvatarRenderState}, which extends it), carrying it from
 * {@link ElytraSlotExtractMixin extraction} to {@link ElytraSlotWingsMixin the wings layer}.
 */
@Mixin(HumanoidRenderState.class)
public class ElytraSlotRenderStateMixin implements ElytraHolder {

    @Unique
    private ItemStack simpleElytraSlot$elytra = ItemStack.EMPTY;

    @Override
    public ItemStack simpleElytraSlot$getElytra() {
        return this.simpleElytraSlot$elytra;
    }

    @Override
    public void simpleElytraSlot$setElytra(ItemStack stack) {
        this.simpleElytraSlot$elytra = stack;
    }
}
