package com.trevorschoeny.simpleelytraslot;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Elytra Slot: a slot above the offhand for an elytra, which glides as if worn,
 * so a chestplate and an elytra can be worn together. Common init declares the slot
 * (both sides); the glide is the {@code ElytraSlotGlideMixin}.
 */
public final class SimpleElytraSlot implements ModInitializer {

    public static final String MOD_ID = "simpleelytraslot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ElytraSlotSettings.load();
        ElytraSlot.register();
    }
}
