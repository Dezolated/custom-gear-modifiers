package com.dezolated.customgearmodifiers.geartier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Reads and writes {@link GearTier} data on an {@link ItemStack}'s NBT.
 *
 * <p>On Minecraft 1.20.1 items still store custom state as NBT (data components arrive in 1.20.5).
 * This helper only persists <i>data</i> — the tier id, rank, global multiplier, and each rolled
 * substat value. The visible tier display (name, multiplier, substat lines) is rendered dynamically
 * at tooltip time (see {@code GearTierTooltip}), so the item's own lore NBT is never modified.
 */
public final class GearTierItemHelper {

    /** NBT key holding the applied tier id as {@code "namespace:path"}. */
    public static final String TAG_TIER = "CustomGearTier";
    /** NBT key holding the tier's integer rank, for quick distinctions by other features/mods. */
    public static final String TAG_RANK = "CustomGearTierRank";
    /** NBT key holding the tier's global stat multiplier at apply time. */
    public static final String TAG_MULTIPLIER = "CustomGearMultiplier";
    /** NBT compound key holding the rolled substat values ({@code name -> float}). */
    public static final String TAG_SUBSTATS = "CustomGearSubstats";

    private GearTierItemHelper() {
    }

    /**
     * Applies a tier to a stack in place: rolls each substat once and writes the tier id, rank,
     * multiplier, and rolled values to NBT. The item's name and lore are left untouched.
     *
     * @param stack  the stack to modify (must not be empty).
     * @param tier   the tier to apply.
     * @param random the source used to roll substat values.
     * @return the rolled substat values for reporting back to the caller.
     */
    public static Map<String, Float> applyTier(ItemStack stack, GearTier tier, RandomSource random) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_TIER, tier.id().toString());
        tag.putInt(TAG_RANK, tier.tier());
        tag.putFloat(TAG_MULTIPLIER, tier.statMultiplier());

        Map<String, Float> rolled = new LinkedHashMap<>();
        CompoundTag substatTag = new CompoundTag();
        tier.subStats().forEach((name, range) -> {
            float value = range.sample(random);
            rolled.put(name, value);
            substatTag.putFloat(name, value);
        });
        tag.put(TAG_SUBSTATS, substatTag);
        return rolled;
    }

    /**
     * @return the tier id stored on the stack, if any.
     */
    public static Optional<ResourceLocation> getTierId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_TIER, Tag.TAG_STRING)) {
            return Optional.empty();
        }
        return Optional.ofNullable(ResourceLocation.tryParse(tag.getString(TAG_TIER)));
    }

    /**
     * @return the applied {@link GearTier} for this stack, resolved against the live registry, if the
     * stack carries a tier id that still corresponds to a loaded tier.
     */
    public static Optional<GearTier> getTier(ItemStack stack) {
        return getTierId(stack).flatMap(GearTierManager::get);
    }

    /**
     * @return the rolled substat values stored on the stack ({@code name -> rolled float}), or an empty
     * map if none. Insertion order follows the stored NBT.
     */
    public static Map<String, Float> getRolledSubstats(ItemStack stack) {
        Map<String, Float> out = new LinkedHashMap<>();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_SUBSTATS, Tag.TAG_COMPOUND)) {
            return out;
        }
        CompoundTag substats = tag.getCompound(TAG_SUBSTATS);
        for (String key : substats.getAllKeys()) {
            out.put(key, substats.getFloat(key));
        }
        return out;
    }
}
