package com.trevorschoeny.simpleelytraslot.checks.mixin;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Self-checks only: gives every rendered player a cape, so the third-person shots show whether
 * the cape hides under the slot's wings. A dev player has no cape of its own. The texture is
 * vanilla's elytra, which uses the cape layout.
 */
@Mixin(AvatarRenderer.class)
public class TestCapeMixin {

    private static final ClientAsset.ResourceTexture CAPE = new ClientAsset.ResourceTexture(
            Identifier.withDefaultNamespace("test_cape"),
            Identifier.withDefaultNamespace("textures/entity/equipment/wings/elytra.png"));

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = @At("TAIL"))
    private void simpleElytraSlotChecks$cape(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        PlayerSkin s = state.skin;
        state.skin = new PlayerSkin(s.body(), CAPE, s.elytra(), s.model(), s.secure());
        state.showCape = true;
    }
}
