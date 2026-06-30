package com.dezolated.customgearmodifiers;

import com.dezolated.customgearmodifiers.client.GearTierTooltip;
import com.dezolated.customgearmodifiers.command.GearTierCommands;
import com.dezolated.customgearmodifiers.geartier.GearTierReloadListener;
import com.dezolated.customgearmodifiers.network.ForgeNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class CustomGearModifiers {

    public CustomGearModifiers() {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
        Constants.LOG.info("Initializing {} on Forge", Constants.MOD_NAME);
        CommonClass.init();
        ForgeNetwork.register();

        // AddReloadListenerEvent fires on the Forge (game) event bus whenever server datapacks
        // (re)load, so we register our shared common listener there.
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        // ItemTooltipEvent only fires client-side; GearTierTooltip (which touches client classes) is
        // therefore never loaded on a dedicated server.
        MinecraftForge.EVENT_BUS.addListener(this::onItemTooltip);
        // Sync the tier registry to players on join and on /reload (fires for one player on join,
        // and once per online player after a reload).
        MinecraftForge.EVENT_BUS.addListener(this::onDatapackSync);
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new GearTierReloadListener());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        // Developer/admin command for inspecting and applying tiers in-game.
        GearTierCommands.register(event.getDispatcher());
    }

    private void onItemTooltip(ItemTooltipEvent event) {
        GearTierTooltip.appendTooltipLines(event.getItemStack(), event.getToolTip());
    }

    private void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            ForgeNetwork.sendTo(event.getPlayer());
        } else {
            event.getPlayerList().getPlayers().forEach(ForgeNetwork::sendTo);
        }
    }
}
