package com.dezolated.customgearmodifiers.geartier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * An inclusive {@code [min, max]} float range describing how a single custom substat may roll for a
 * {@link GearTier}. This is intentionally generic: the substat's meaning (crit chance, ammo
 * efficiency, lifesteal, ...) is decided entirely by the consuming feature, not by this type.
 *
 * <p>Defined in datapack JSON as:
 * <pre>{@code { "label": "Reload Speed", "min": 0.15, "max": 0.35 }}</pre>
 * The {@code label} is optional; when omitted, the substat's map key is prettified for display
 * (e.g. {@code "reload_speed"} -> {@code "Reload Speed"}).
 *
 * @param label a human-readable display name, or empty to fall back to the prettified key.
 * @param min   inclusive minimum roll.
 * @param max   inclusive maximum roll.
 */
public record SubStatRange(String label, float min, float max) {

    public static final Codec<SubStatRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("label", "").forGetter(SubStatRange::label),
            Codec.FLOAT.fieldOf("min").forGetter(SubStatRange::min),
            Codec.FLOAT.fieldOf("max").forGetter(SubStatRange::max)
    ).apply(instance, SubStatRange::new));

    /**
     * @return a uniformly distributed roll within {@code [min, max]} using the supplied source.
     */
    public float sample(RandomSource random) {
        if (max <= min) {
            return min;
        }
        return min + random.nextFloat() * (max - min);
    }

    /**
     * @return the midpoint of the range, useful for tooltips/previews that want a representative value.
     */
    public float average() {
        return (min + max) * 0.5F;
    }

    /**
     * @return how good {@code value} is within this range, as a fraction {@code 0..1} (0 = rolled the
     * minimum, 1 = rolled the maximum). Returns {@code 1} for a degenerate range where {@code max <= min}.
     */
    public float quality(float value) {
        if (max <= min) {
            return 1.0F;
        }
        return Mth.clamp((value - min) / (max - min), 0.0F, 1.0F);
    }

    /**
     * @param key the substat's map key, used as the fallback name.
     * @return the configured {@link #label()} if present, otherwise the prettified key.
     */
    public String displayName(String key) {
        return label == null || label.isBlank() ? prettify(key) : label;
    }

    /** Turns a snake_case key into Title Case words, e.g. {@code "ammo_efficiency"} -> {@code "Ammo Efficiency"}. */
    private static String prettify(String key) {
        StringBuilder out = new StringBuilder();
        for (String part : key.split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.length() == 0 ? key : out.toString();
    }
}
