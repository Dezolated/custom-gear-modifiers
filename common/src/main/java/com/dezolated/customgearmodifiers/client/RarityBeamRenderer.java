package com.dezolated.customgearmodifiers.client;

import com.dezolated.customgearmodifiers.geartier.GearTier;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Draws a subtle ARPG-style loot beam in a tier's color above a dropped item.
 *
 * <p>Rendered as a cross of two vertical quads (visible from any angle without billboarding math),
 * fading from a soft base to transparent at the top. Drawn in immediate mode so we can set
 * {@code depthMask(false)} — the beam still depth-<i>tests</i> (terrain in front hides it) but never
 * writes depth, so it can't occlude the dropped item it surrounds. Opacity gently pulses over time.
 *
 * <p>The pose origin inside {@code ItemEntityRenderer.render} sits at the item's position, so the
 * beam is drawn in local space rising from {@code y = 0}.
 */
public final class RarityBeamRenderer {

    private static final float HALF_WIDTH = 0.13F;
    private static final float BASE_Y = 0.0F;
    private static final float TOP_Y = 1.3F;
    private static final float BASE_ALPHA = 0.45F;
    private static final long PULSE_PERIOD_MS = 2600L;

    private RarityBeamRenderer() {
    }

    public static void render(PoseStack pose, GearTier tier) {
        int rgb = tier.colorValue();
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;

        float pulse = 0.65F + 0.35F * Mth.sin((Util.getMillis() % PULSE_PERIOD_MS) / (float) PULSE_PERIOD_MS * (float) (Math.PI * 2.0));
        float alpha = BASE_ALPHA * pulse;

        Matrix4f matrix = pose.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);   // key: test depth but never write it, so the item stays visible
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        // Two perpendicular vertical planes; cull is disabled so both faces show.
        quad(builder, matrix, -HALF_WIDTH, 0.0F, HALF_WIDTH, 0.0F, r, g, b, alpha);
        quad(builder, matrix, 0.0F, -HALF_WIDTH, 0.0F, HALF_WIDTH, r, g, b, alpha);
        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /** Emits one vertical quad: bottom edge (ax,az)->(bx,bz) at full alpha, fading to 0 at the top. */
    private static void quad(BufferBuilder builder, Matrix4f m, float ax, float az, float bx, float bz,
                             float r, float g, float b, float a) {
        builder.vertex(m, ax, BASE_Y, az).color(r, g, b, a).endVertex();
        builder.vertex(m, bx, BASE_Y, bz).color(r, g, b, a).endVertex();
        builder.vertex(m, bx, TOP_Y, bz).color(r, g, b, 0.0F).endVertex();
        builder.vertex(m, ax, TOP_Y, az).color(r, g, b, 0.0F).endVertex();
    }
}
