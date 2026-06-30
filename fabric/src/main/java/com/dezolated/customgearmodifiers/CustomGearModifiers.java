package com.dezolated.customgearmodifiers;

import com.dezolated.customgearmodifiers.command.GearTierCommands;
import com.dezolated.customgearmodifiers.geartier.FabricGearTierReloadListener;
import com.dezolated.customgearmodifiers.geartier.GearTierManager;
import com.dezolated.customgearmodifiers.geartier.GearTierNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.network.FriendlyByteBuf;
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

        // Sync the tier registry to each player on join and on /reload, so client-side visuals
        // (tooltip card, name, loot beam) can resolve tiers in multiplayer.
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            FriendlyByteBuf buf = PacketByteBufs.create();
            GearTierNetwork.writeTiers(buf, GearTierManager.asMap());
            ServerPlayNetworking.send(player, GearTierNetwork.SYNC_TIERS, buf);
        });
    }
}
