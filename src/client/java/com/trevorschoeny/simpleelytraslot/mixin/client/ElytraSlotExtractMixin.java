package com.trevorschoeny.simpleelytraslot.mixin.client;

import com.trevorschoeny.simpleelytraslot.ElytraHolder;
import com.trevorschoeny.simpleelytraslot.ElytraSlot;
import com.trevorschoeny.simpleelytraslot.ElytraSlotSettings;

import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the slot's elytra into the render state alongside vanilla's own equipment
 * extraction. {@code extractHumanoidRenderState} is the shared static both the mob and
 * player renderers go through, so this covers every render context. Players only;
 * everything else keeps EMPTY, and so does a player while the mod is off.
 */
@Mixin(HumanoidMobRenderer.class)
public class ElytraSlotExtractMixin {

    @Inject(method = "extractHumanoidRenderState", at = @At("TAIL"))
    private static void simpleElytraSlot$stashElytra(LivingEntity livingEntity,
                                                     HumanoidRenderState state, float partialTick,
                                                     ItemModelResolver resolver, CallbackInfo ci) {
        ItemStack elytra = livingEntity instanceof Player player && ElytraSlotSettings.enabled()
                ? ElytraSlot.get(player).copy()
                : ItemStack.EMPTY;
        ((ElytraHolder) state).simpleElytraSlot$setElytra(elytra);
    }
}
