package com.dezolated.customgearmodifiers.network;

import com.dezolated.customgearmodifiers.Constants;
import com.dezolated.customgearmodifiers.geartier.GearTier;
import com.dezolated.customgearmodifiers.geartier.GearTierManager;
import com.dezolated.customgearmodifiers.geartier.GearTierNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Forge networking for the tier registry sync. A single S2C message carries the whole tier table to
 * a player; the client applies it to {@link GearTierManager}. Sending is driven by
 * {@code OnDatapackSyncEvent} (fires on join and on {@code /reload}).
 */
public final class ForgeNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            GearTierNetwork.SYNC_TIERS, () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ForgeNetwork() {
    }

    /** Registers the sync message. Call once during mod construction. */
    public static void register() {
        CHANNEL.registerMessage(
                0,
                SyncTiersMessage.class,
                (msg, buf) -> GearTierNetwork.writeTiers(buf, msg.tiers()),
                buf -> new SyncTiersMessage(GearTierNetwork.readTiers(buf)),
                (msg, ctx) -> {
                    NetworkEvent.Context context = ctx.get();
                    // Runs on the receiving (client) side; apply on the main thread.
                    context.enqueueWork(() -> GearTierManager.replaceAll(msg.tiers()));
                    context.setPacketHandled(true);
                });
    }

    public static void sendTo(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncTiersMessage(GearTierManager.asMap()));
    }

    public record SyncTiersMessage(Map<ResourceLocation, GearTier> tiers) {
    }
}
