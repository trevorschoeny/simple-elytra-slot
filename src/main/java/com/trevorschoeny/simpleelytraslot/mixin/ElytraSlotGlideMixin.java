package com.trevorschoeny.simpleelytraslot.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.trevorschoeny.simpleelytraslot.ElytraSlot;
import com.trevorschoeny.simpleelytraslot.ElytraSlotSettings;

import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gliding from the elytra slot: the slot's elytra counts as if it were worn in the
 * chest slot. Universal: {@code canGlide} runs on both sides (the client predicts the
 * glide start, the server is authoritative), and vanilla applies
 * {@code updateFallFlying}'s durability loss only on the server.
 *
 * <p>Vanilla's whole gliding lifecycle depends on {@code canGlide()}, which calls
 * {@code canGlideUsing(getItemBySlot(slot), slot)} over the real equipment slots. This
 * widens <em>where</em> the elytra is found, reusing vanilla's checks and physics:
 *
 * <ul>
 *   <li><b>Start and keep gliding.</b> {@code Player.tryToStartFallFlying} and the
 *       per-tick {@code updateFallFlying} both check {@code canGlide()}. This injects at
 *       its returns: if vanilla found no worn glider but the player is airborne with an
 *       elytra in the slot, the glide is allowed. A worn elytra wins: vanilla checks the
 *       real chest slot first, so {@code canGlide} is already true and the slot's elytra
 *       stays unused.</li>
 *   <li><b>Durability and crash guard.</b> Vanilla wears the glider down in
 *       {@code updateFallFlying} by picking a random glide-capable <em>equipment</em>
 *       slot. Gliding on the slot's elytra, that list is empty and {@code Util.getRandom}
 *       would crash. So this wraps the pick: for a player with an empty list it damages
 *       the slot's elytra as vanilla would, and returns the BODY slot (always empty for
 *       players) so vanilla's own {@code hurtAndBreak} does nothing. Any other entity
 *       keeps vanilla's pick. At durability 1 the
 *       next-damage-breaks check in {@code canGlideUsing} fails, {@code canGlide} turns
 *       false and flight stops, as in vanilla.</li>
 * </ul>
 */
@Mixin(LivingEntity.class)
public abstract class ElytraSlotGlideMixin {

    @Inject(method = "canGlide", at = @At("RETURN"), cancellable = true)
    private void simpleElytraSlot$canGlide(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;               // vanilla already allows it (a worn glider wins)
        if (!ElytraSlotSettings.enabled()) return;        // mod off: vanilla glide rules
        LivingEntity self = (LivingEntity) (Object) this;
        // Vanilla's airborne guard: never glide on the ground, as a passenger, or under
        // levitation, slot elytra or not.
        if (self.onGround() || self.isPassenger() || self.hasEffect(MobEffects.LEVITATION)) return;
        if (!(self instanceof Player player)) return;
        ItemStack elytra = ElytraSlot.get(player);
        // Judge the slot's elytra as a chest-slot glider: vanilla's own check, slot CHEST.
        if (LivingEntity.canGlideUsing(elytra, EquipmentSlot.CHEST)) {
            cir.setReturnValue(true);
        }
    }

    @WrapOperation(
            method = "updateFallFlying",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/Util;getRandom(Ljava/util/List;Lnet/minecraft/util/RandomSource;)Ljava/lang/Object;"))
    private Object simpleElytraSlot$wear(List<EquipmentSlot> slots, RandomSource random,
                                         Operation<Object> original) {
        // A worn glider exists (vanilla wears it), or this isn't a player: vanilla's own pick.
        if (!slots.isEmpty() || !((Object) this instanceof Player player)) {
            return original.call(slots, random);
        }
        // A player gliding off the slot's elytra (no worn glider): vanilla's list is empty.
        ElytraSlot.wear(player, (LivingEntity) (Object) this, 1);
        return EquipmentSlot.BODY;                       // a player's BODY item is empty, so vanilla's hurtAndBreak does nothing
    }
}
