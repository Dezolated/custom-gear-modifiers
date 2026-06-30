package com.dezolated.customgearmodifiers.geartier;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric requires reload listeners to be identifiable (so other mods can declare ordering against
 * them). This thin wrapper adds that identity on top of the shared common parsing logic in
 * {@link GearTierReloadListener}; all actual parsing and storage stays in the common module.
 */
public class FabricGearTierReloadListener extends GearTierReloadListener implements IdentifiableResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
        return GearTierReloadListener.LISTENER_ID;
    }
}
