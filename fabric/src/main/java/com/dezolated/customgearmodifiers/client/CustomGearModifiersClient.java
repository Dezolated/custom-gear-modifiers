package com.dezolated.customgearmodifiers.client;

import com.dezolated.customgearmodifiers.geartier.GearTierManager;
import com.dezolated.customgearmodifiers.geartier.GearTierNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Fabric client entry point. Registers the dynamic tier tooltip and the receiver that applies the
 * tier registry synced from the server.
 */
public class CustomGearModifiersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, lines) ->
                GearTierTooltip.appendTooltipLines(stack, lines));

        // Receive the server's tier registry and apply it on the client thread.
        ClientPlayNetworking.registerGlobalReceiver(GearTierNetwork.SYNC_TIERS, (client, handler, buf, responseSender) -> {
            var tiers = GearTierNetwork.readTiers(buf);
            client.execute(() -> GearTierManager.replaceAll(tiers));
        });
    }
}
