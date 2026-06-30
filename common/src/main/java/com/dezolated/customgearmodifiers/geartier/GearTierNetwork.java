package com.dezolated.customgearmodifiers.geartier;

import com.dezolated.customgearmodifiers.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared (loader-agnostic) serialization for syncing the gear tier registry from server to client.
 *
 * <p>Tier definitions are loaded from datapacks on the server only, so a remote client's
 * {@link GearTierManager} would otherwise be empty and the tier visuals (tooltip card, name, loot
 * beam) couldn't resolve. Each loader sends this payload to players on join and on {@code /reload};
 * the client decodes it and calls {@link GearTierManager#replaceAll(Map)}.
 */
public final class GearTierNetwork {

    /** Channel / packet id for the tier sync. */
    public static final ResourceLocation SYNC_TIERS = new ResourceLocation(Constants.MOD_ID, "sync_tiers");

    private GearTierNetwork() {
    }

    public static void writeTiers(FriendlyByteBuf buf, Map<ResourceLocation, GearTier> tiers) {
        buf.writeVarInt(tiers.size());
        for (GearTier tier : tiers.values()) {
            buf.writeResourceLocation(tier.id());
            buf.writeVarInt(tier.tier());
            buf.writeUtf(tier.displayName());
            buf.writeUtf(tier.hexColor());
            buf.writeFloat(tier.statMultiplier());
            buf.writeBoolean(tier.visualize());
            buf.writeBoolean(tier.lootBeam());
            buf.writeVarInt(tier.subStats().size());
            for (Map.Entry<String, SubStatRange> entry : tier.subStats().entrySet()) {
                SubStatRange range = entry.getValue();
                buf.writeUtf(entry.getKey());
                buf.writeUtf(range.label());
                buf.writeFloat(range.min());
                buf.writeFloat(range.max());
            }
        }
    }

    public static Map<ResourceLocation, GearTier> readTiers(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        Map<ResourceLocation, GearTier> tiers = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation id = buf.readResourceLocation();
            int rank = buf.readVarInt();
            String displayName = buf.readUtf();
            String hexColor = buf.readUtf();
            float multiplier = buf.readFloat();
            boolean visualize = buf.readBoolean();
            boolean lootBeam = buf.readBoolean();
            int subCount = buf.readVarInt();
            Map<String, SubStatRange> subStats = new LinkedHashMap<>(subCount);
            for (int j = 0; j < subCount; j++) {
                String key = buf.readUtf();
                String label = buf.readUtf();
                float min = buf.readFloat();
                float max = buf.readFloat();
                subStats.put(key, new SubStatRange(label, min, max));
            }
            tiers.put(id, new GearTier(id, rank, displayName, hexColor, multiplier, visualize, lootBeam, subStats));
        }
        return tiers;
    }
}
