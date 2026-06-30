package com.dezolated.customgearmodifiers;

import com.dezolated.customgearmodifiers.command.GearTierCommands;
import com.dezolated.customgearmodifiers.geartier.FabricGearTierReloadListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class CustomGearModifiers implements ModInitializer {

    @Override
    public void onInitialize() {

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.
        Constants.LOG.info("Initializing {} on Fabric", Constants.MOD_NAME);
        CommonClass.init();

        // Register the data-driven gear tier loader with Fabric's server-data reload pipeline.
        // It runs on every datapack (re)load, including /reload.
        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(new FabricGearTierReloadListener());

        // Developer/admin command for inspecting and applying tiers in-game.
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> GearTierCommands.register(dispatcher));
    }
}
