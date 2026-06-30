package com.dezolated.customgearmodifiers.geartier;

import com.dezolated.customgearmodifiers.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

/**
 * Datapack reload listener that discovers and parses every gear tier definition under
 * {@code data/<namespace>/gear_tiers/*.json} and publishes the result to {@link GearTierManager}.
 *
 * <p>This lives in the common module so the parsing logic is shared. Each loader is responsible only
 * for <i>registering</i> an instance with its own server-data reload pipeline:
 * <ul>
 *   <li>Fabric: {@code ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(...)}
 *       (see {@code FabricGearTierReloadListener}).</li>
 *   <li>Forge: {@code AddReloadListenerEvent#addListener(...)}.</li>
 * </ul>
 *
 * <p>{@link SimpleJsonResourceReloadListener} handles file scanning and JSON parsing off-thread; we
 * only decode each {@link JsonElement} with {@link GearTier#CODEC} and stamp it with the id derived
 * from its file location.
 */
public class GearTierReloadListener extends SimpleJsonResourceReloadListener {

    /** The datapack subdirectory scanned for tier definitions: {@code data/<namespace>/gear_tiers/}. */
    public static final String DIRECTORY = "gear_tiers";

    /** Stable id used by loaders that require an identifiable reload listener (e.g. Fabric). */
    public static final ResourceLocation LISTENER_ID = new ResourceLocation(Constants.MOD_ID, DIRECTORY);

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public GearTierReloadListener() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, GearTier> parsed = new HashMap<>(resources.size());

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            GearTier.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> Constants.LOG.error("Failed to parse gear tier '{}': {}", id, error))
                    .ifPresent(tier -> parsed.put(id, tier.withId(id)));
        }

        GearTierManager.replaceAll(parsed);
        Constants.LOG.info("Loaded {} gear tier(s): {}", parsed.size(), parsed.keySet());
    }
}
