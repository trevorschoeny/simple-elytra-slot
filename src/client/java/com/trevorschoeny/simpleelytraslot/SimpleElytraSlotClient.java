package com.trevorschoeny.simpleelytraslot;

import com.trevorschoeny.simpleelytraslot.ip.ElytraSlotIp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Client init: the slot's panel, the HUD icon and, only with Inventory Plus installed, IP's column
 * switch. The {@code ip} package is touched nowhere else, so IP's classes never load
 * without IP. The wings on the back are mixins and need no init.
 */
public final class SimpleElytraSlotClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ElytraSlotPanel.register();
        ElytraHud.register();
        if (FabricLoader.getInstance().isModLoaded("inventoryplus")) ElytraSlotIp.register();
    }
}
