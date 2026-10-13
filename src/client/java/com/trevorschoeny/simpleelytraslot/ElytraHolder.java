package com.trevorschoeny.simpleelytraslot;

import net.minecraft.world.item.ItemStack;

/**
 * Duck interface mixed onto {@code HumanoidRenderState} so the slot's elytra travels on
 * the render state from extraction to the wings layer. The render state is the only
 * link between the player (who has the slot) and {@code WingsLayer.submit} (which only
 * gets the render state), so the wings layer can draw the slot's elytra with vanilla's
 * own code in every place the player is rendered (third person, the inventory preview).
 */
public interface ElytraHolder {

    /** The slot's elytra captured for this frame, or EMPTY. */
    ItemStack simpleElytraSlot$getElytra();

    void simpleElytraSlot$setElytra(ItemStack stack);
}
