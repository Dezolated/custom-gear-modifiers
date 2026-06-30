package com.dezolated.customgearmodifiers.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

/**
 * Fabric client entry point. Registers the dynamic tier tooltip so it can react to the Alt key.
 */
public class CustomGearModifiersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, lines) ->
                GearTierTooltip.appendTooltipLines(stack, lines));
    }
}
