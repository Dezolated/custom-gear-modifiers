package com.dezolated.customgearmodifiers.geartier;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Global, runtime-queryable registry of all data-driven {@link GearTier}s currently loaded from
 * datapacks. This is the single access point other features in the mod (weapon attributes, tooltip
 * rendering, loot generation, ...) should use to look up tier properties.
 *
 * <p>The contents are replaced wholesale every time datapacks (re)load, driven by
 * {@link GearTierReloadListener} on each platform. Storage is held in a {@code volatile} immutable
 * map so reads from the server thread, client thread, and worker threads stay consistent without
 * locking.
 *
 * <p>This class is deliberately free of any loader-specific or even Minecraft-server-specific state,
 * so it lives entirely in the common module and is shared by both Fabric and Forge.
 */
public final class GearTierManager {

    private static volatile Map<ResourceLocation, GearTier> tiers = Map.of();

    private GearTierManager() {
    }

    /**
     * Replaces the entire tier table. Called by the reload listener once parsing completes.
     *
     * @param newTiers the freshly parsed tiers; defensively copied into an immutable map.
     */
    public static void replaceAll(Map<ResourceLocation, GearTier> newTiers) {
        tiers = Map.copyOf(newTiers);
    }

    /**
     * @return the tier with the given id, or empty if no such tier is loaded.
     */
    public static Optional<GearTier> get(ResourceLocation id) {
        return Optional.ofNullable(tiers.get(id));
    }

    /**
     * Convenience overload that parses a {@code "namespace:path"} string. Returns empty if the string
     * is not a valid {@link ResourceLocation} or no matching tier exists.
     */
    public static Optional<GearTier> get(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        return location == null ? Optional.empty() : get(location);
    }

    /**
     * @return {@code true} if a tier with the given id is currently loaded.
     */
    public static boolean contains(ResourceLocation id) {
        return tiers.containsKey(id);
    }

    /**
     * @return an unmodifiable view of every loaded tier.
     */
    public static Collection<GearTier> all() {
        return Collections.unmodifiableCollection(tiers.values());
    }

    /**
     * @return an unmodifiable view of every loaded tier id.
     */
    public static Set<ResourceLocation> ids() {
        return Collections.unmodifiableSet(tiers.keySet());
    }

    /**
     * @return an unmodifiable view of the full id-to-tier mapping.
     */
    public static Map<ResourceLocation, GearTier> asMap() {
        return tiers;
    }

    /**
     * @return how many tiers are currently loaded.
     */
    public static int size() {
        return tiers.size();
    }
}
