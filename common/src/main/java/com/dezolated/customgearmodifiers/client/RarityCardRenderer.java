package com.dezolated.customgearmodifiers.client;

import com.dezolated.customgearmodifiers.geartier.GearTier;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Draws the stylized "glassmorphic" rarity card used behind item tooltips.
 *
 * <p>Pure rendering, no state beyond the wall clock ({@link Util#getMillis()}) used to animate. The
 * look: a dark backdrop for legibility, a rarity-tinted vertical gradient, a static top sheen, and a
 * rarity-colored border whose glow gently breathes. The glow strength scales with the tier's
 * {@code stat_multiplier}, so rarer gear feels livelier. All colors are ARGB; the GUI render type
 * blends translucent fills correctly.
 *
 * <p>Every draw call is issued at the {@code z} depth vanilla passes for the tooltip background
 * (typically 400) so the card composites above slot item icons rather than behind them.
 */
public final class RarityCardRenderer {

    /** Multiplier value that maps to full animation intensity (top of the default tier ladder). */
    private static final float INTENSITY_RANGE = 5.5F;

    /**
     * Opacity of the dark backdrop behind a card (0-255). High enough that item icons and text stay
     * readable through the glass; vanilla tooltips use ~0xF0. Lower this for a more see-through look.
     */
    private static final int BACKDROP_ALPHA = 0xEC;

    /** How far the card extends past the tooltip content box, in pixels (vanilla uses 3). */
    private static final int CARD_PADDING = 5;

    private RarityCardRenderer() {
    }

    /**
     * Draws the card behind a tooltip. Coordinates match the content box vanilla passes to
     * {@code renderTooltipBackground}; we expand by the same 3px padding vanilla uses for its frame.
     *
     * @param z the render depth vanilla uses for the tooltip background, so the card layers correctly.
     */
    public static void drawTooltipCard(GuiGraphics graphics, int x, int y, int width, int height, int z, GearTier tier) {
        int p = CARD_PADDING;
        drawCard(graphics, x - p, y - p, x + width + p, y + height + p, z, tier, true);
    }

    private static void drawCard(GuiGraphics graphics, int x0, int y0, int x1, int y1, int z, GearTier tier, boolean framed) {
        if (x1 <= x0 || y1 <= y0) {
            return;
        }
        int rgb = tier.colorValue();
        int tint = mute(rgb, 0.55F, 0.35F);     // darkened, desaturated rarity wash
        int borderColor = mute(rgb, 0.80F, 0.15F); // keeps the hue but pulls back the neon
        long time = Util.getMillis();
        float intensity = Mth.clamp((tier.statMultiplier() - 1.0F) / INTENSITY_RANGE, 0.0F, 1.0F);

        // Near-black backdrop, then a muted rarity wash that fades out toward the bottom.
        fill(graphics, x0, y0, x1, y1, z, argb(BACKDROP_ALPHA, 0x07070A));
        graphics.fillGradient(x0, y0, x1, y1, z, argb(0x28, tint), argb(0x08, tint));

        // Bottom vignette for depth and grit.
        graphics.fillGradient(x0, y0, x1, y1, z, argb(0x00, 0x000000), argb(0x3C, 0x000000));

        // Faint, cool top sheen (much dimmer and grayer than a bright glassy highlight).
        int sheenHeight = Math.max(2, (y1 - y0) / 4);
        graphics.fillGradient(x0, y0, x1, y0 + sheenHeight, z, argb(0x12, 0xB8BEC8), argb(0x00, 0xB8BEC8));

        // Subtle, slow breathing border in the muted color.
        float breathe = 0.5F + 0.5F * Mth.sin((time % 5000L) / 5000.0F * (float) (Math.PI * 2.0));
        int borderAlpha = Mth.clamp((int) (0x80 + breathe * 0x30 * (0.3F + 0.7F * intensity)), 0x70, 0xE0);
        drawBorder(graphics, x0, y0, x1, y1, z, argb(borderAlpha, borderColor));
        if (framed) {
            // Dark inset line for a recessed, gritty edge (instead of a bright glassy one).
            drawBorder(graphics, x0 + 1, y0 + 1, x1 - 1, y1 - 1, z, argb(0x55, 0x000000));
        }
    }

    private static void drawBorder(GuiGraphics graphics, int x0, int y0, int x1, int y1, int z, int color) {
        fill(graphics, x0, y0, x1, y0 + 1, z, color);     // top
        fill(graphics, x0, y1 - 1, x1, y1, z, color);     // bottom
        fill(graphics, x0, y0, x0 + 1, y1, z, color);     // left
        fill(graphics, x1 - 1, y0, x1, y1, z, color);     // right
    }

    private static void fill(GuiGraphics graphics, int x0, int y0, int x1, int y1, int z, int color) {
        graphics.fill(x0, y0, x1, y1, z, color);
    }

    private static int argb(int alpha, int rgb) {
        return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF);
    }

    /**
     * Mutes a color: pulls it toward its own gray luminance by {@code desat}, then scales brightness by
     * {@code darken}. Lower values = grittier, less vibrant.
     */
    private static int mute(int rgb, float darken, float desat) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        float lum = 0.299F * r + 0.587F * g + 0.114F * b;
        return (clampByte((r + (lum - r) * desat) * darken) << 16)
                | (clampByte((g + (lum - g) * desat) * darken) << 8)
                | clampByte((b + (lum - b) * desat) * darken);
    }

    private static int clampByte(float value) {
        return (int) Mth.clamp(value, 0.0F, 255.0F);
    }
}
