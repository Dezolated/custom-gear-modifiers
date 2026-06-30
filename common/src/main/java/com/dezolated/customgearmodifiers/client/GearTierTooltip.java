package com.dezolated.customgearmodifiers.client;

import com.dezolated.customgearmodifiers.geartier.GearTier;
import com.dezolated.customgearmodifiers.geartier.GearTierItemHelper;
import com.dezolated.customgearmodifiers.geartier.SubStatRange;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Builds the tier display lines injected into an item's tooltip at render time (so they can react to
 * the Alt key). Shared by both loaders; each loader registers its own tooltip hook and calls
 * {@link #appendTooltipLines(ItemStack, List)}.
 *
 * <p>Layout (only when the item carries a visualized tier):
 * <pre>
 *   &lt;Rarity Name&gt;          (tier color)
 *   Stat Multiplier: x2.50  (grey)
 *   &lt;Substat&gt;: 0.12        (grey -&gt; tier color by roll quality)
 *   ...
 * </pre>
 * Holding <b>Alt</b> expands each substat line with its range and the achieved percentage; otherwise a
 * faint "Hold Alt for roll details" hint is shown.
 */
public final class GearTierTooltip {

    /** Substat line color for the worst possible roll (grey); best roll lerps to the tier color. */
    private static final int QUALITY_LOW_COLOR = 0x9E9E9E;

    private GearTierTooltip() {
    }

    public static void appendTooltipLines(ItemStack stack, List<Component> lines) {
        Optional<GearTier> maybeTier = GearTierItemHelper.getTier(stack);
        if (maybeTier.isEmpty() || !maybeTier.get().visualize()) {
            return;
        }
        GearTier tier = maybeTier.get();
        Map<String, Float> rolled = GearTierItemHelper.getRolledSubstats(stack);
        boolean alt = Screen.hasAltDown();

        List<Component> ours = new ArrayList<>();
        ours.add(plain(tier.displayName(), tier.colorValue()));
        ours.add(plain(String.format(Locale.ROOT, "Stat Multiplier: x%.2f", tier.statMultiplier()), ChatFormatting.GRAY));

        tier.subStats().forEach((name, range) -> ours.add(substatLine(tier, name, range, rolled, alt)));

        if (!alt && !tier.subStats().isEmpty()) {
            ours.add(Component.literal("Hold Alt for roll details")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(true)));
        }

        // Insert directly beneath the item name (line 0), pushing any existing lore down.
        lines.addAll(Math.min(1, lines.size()), ours);
    }

    private static MutableComponent substatLine(GearTier tier, String name, SubStatRange range,
                                                Map<String, Float> rolled, boolean alt) {
        float value = rolled.getOrDefault(name, range.average());
        float quality = range.quality(value);
        int color = lerpColor(QUALITY_LOW_COLOR, tier.colorValue(), quality);

        MutableComponent line = Component.literal(String.format(Locale.ROOT, "%s: %.2f", range.displayName(name), value))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)).withItalic(false));

        if (alt) {
            line.append(Component.literal(
                            String.format(Locale.ROOT, "  [%.2f - %.2f]  (%d%%)", range.min(), range.max(), Math.round(quality * 100)))
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false)));
        }
        return line;
    }

    private static MutableComponent plain(String text, int rgb) {
        return Component.literal(text).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withItalic(false));
    }

    private static MutableComponent plain(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(Style.EMPTY.withColor(color).withItalic(false));
    }

    /** Linearly interpolates between two packed RGB colors; {@code t} is clamped to {@code 0..1}. */
    private static int lerpColor(int from, int to, float t) {
        t = Math.max(0.0F, Math.min(1.0F, t));
        int fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF, tg = (to >> 8) & 0xFF, tb = to & 0xFF;
        int r = (int) (fr + (tr - fr) * t);
        int g = (int) (fg + (tg - fg) * t);
        int b = (int) (fb + (tb - fb) * t);
        return (r << 16) | (g << 8) | b;
    }
}
