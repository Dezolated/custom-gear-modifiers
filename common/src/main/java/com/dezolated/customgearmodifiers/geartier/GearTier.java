package com.dezolated.customgearmodifiers.geartier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

/**
 * An immutable, fully data-driven gear rarity tier.
 *
 * <p>There are <b>no hardcoded tiers</b> in this mod. Every tier is defined by a modpack/server JSON
 * file placed at {@code data/<namespace>/gear_tiers/<name>.json} in any loaded datapack (including
 * this mod's own bundled data). The file's location determines the tier {@link #id()}; the remaining
 * properties come from the JSON body and are parsed via {@link #CODEC}.
 *
 * <p>Example JSON:
 * <pre>{@code
 * {
 *   "tier": 5,
 *   "display_name": "Legendary",
 *   "color": "#FFAA00",
 *   "stat_multiplier": 2.5,
 *   "visualize": true,
 *   "substats": {
 *     "crit_chance":      { "min": 0.15, "max": 0.35 },
 *     "ammo_efficiency":  { "min": 0.20, "max": 0.50 }
 *   }
 * }
 * }</pre>
 *
 * @param id             the unique tier identifier, derived from the JSON file's namespace + path.
 * @param tier           an integer rank for clear tier distinctions (ordering, gating, other mods).
 *                       Defaults to {@code 0}; larger means rarer by convention.
 * @param displayName    human-readable name (literal string or a translation key, consumer's choice).
 * @param hexColor       the raw hex color string as authored, e.g. {@code "#FFAA00"} or {@code "FFAA00"}.
 * @param statMultiplier a global multiplier applied to this tier's gear stats; defaults to {@code 1.0}.
 * @param visualize      whether the mod should render the built-in rarity lore/styling on tagged items.
 *                       Defaults to {@code true}; set {@code false} so the tier data is still applied
 *                       (id, multiplier, rolled substats) while a modder provides their own styling.
 * @param lootBeam       whether a dropped item of this tier emits a rarity-colored loot beam on the
 *                       ground. Defaults to {@code true}; set {@code false} per tier to suppress it.
 * @param subStats       arbitrary, named substat roll ranges that other features can query at runtime.
 */
public record GearTier(
        ResourceLocation id,
        int tier,
        String displayName,
        String hexColor,
        float statMultiplier,
        boolean visualize,
        boolean lootBeam,
        Map<String, SubStatRange> subStats
) {

    public static final float DEFAULT_STAT_MULTIPLIER = 1.0F;

    /** Tiers render their built-in lore/styling on items unless a JSON explicitly opts out. */
    public static final boolean DEFAULT_VISUALIZE = true;

    /** Dropped tier items emit a loot beam unless a JSON explicitly opts out. */
    public static final boolean DEFAULT_LOOT_BEAM = true;

    /** Fallback color (white) used when {@link #hexColor()} cannot be parsed. */
    public static final int DEFAULT_COLOR = 0xFFFFFF;

    /**
     * Codec for the JSON body of a tier. The {@code id} is supplied separately from the file location
     * (see {@link #withId(ResourceLocation)}) and is therefore decoded as {@code null} here.
     */
    public static final Codec<GearTier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("tier", 0).forGetter(GearTier::tier),
            Codec.STRING.fieldOf("display_name").forGetter(GearTier::displayName),
            Codec.STRING.fieldOf("color").forGetter(GearTier::hexColor),
            Codec.FLOAT.optionalFieldOf("stat_multiplier", DEFAULT_STAT_MULTIPLIER).forGetter(GearTier::statMultiplier),
            Codec.BOOL.optionalFieldOf("visualize", DEFAULT_VISUALIZE).forGetter(GearTier::visualize),
            Codec.BOOL.optionalFieldOf("loot_beam", DEFAULT_LOOT_BEAM).forGetter(GearTier::lootBeam),
            Codec.unboundedMap(Codec.STRING, SubStatRange.CODEC).optionalFieldOf("substats", Map.of()).forGetter(GearTier::subStats)
    ).apply(instance, (tier, displayName, hexColor, statMultiplier, visualize, lootBeam, subStats) ->
            new GearTier(null, tier, displayName, hexColor, statMultiplier, visualize, lootBeam, Map.copyOf(subStats))));

    /**
     * @return a copy of this tier stamped with the given identifier. Used by the reload listener to
     * attach the id derived from the JSON file's resource location after decoding the body.
     */
    public GearTier withId(ResourceLocation id) {
        return new GearTier(id, tier, displayName, hexColor, statMultiplier, visualize, lootBeam, subStats);
    }

    /**
     * Parses {@link #hexColor()} into a packed 0xRRGGBB int. Accepts an optional leading {@code #} or
     * {@code 0x}. Falls back to {@link #DEFAULT_COLOR} if the string is not valid hex.
     *
     * @return the packed RGB color value.
     */
    public int colorValue() {
        return HexColor.parse(hexColor, DEFAULT_COLOR);
    }

    /**
     * Looks up a named substat roll range.
     *
     * @param name the substat key as authored in JSON (e.g. {@code "crit_chance"}).
     * @return the range, or empty if this tier does not define that substat.
     */
    public Optional<SubStatRange> subStat(String name) {
        return Optional.ofNullable(subStats.get(name));
    }

    /**
     * @return {@code true} if this tier defines a substat with the given name.
     */
    public boolean hasSubStat(String name) {
        return subStats.containsKey(name);
    }
}
