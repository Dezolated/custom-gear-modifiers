package com.dezolated.customgearmodifiers.mixin.client;

import com.dezolated.customgearmodifiers.client.RarityCardRenderer;
import com.dezolated.customgearmodifiers.client.TooltipContext;
import com.dezolated.customgearmodifiers.geartier.GearTier;
import com.dezolated.customgearmodifiers.geartier.GearTierItemHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Replaces the vanilla tooltip background with a glassmorphic rarity card when the hovered item
 * carries a tier that opts into visualization. Falls through to vanilla for everything else.
 */
@Mixin(TooltipRenderUtil.class)
public class MixinTooltipRenderUtil {

    @Inject(method = "renderTooltipBackground", at = @At("HEAD"), cancellable = true)
    private static void customgearmodifiers$glassCard(GuiGraphics graphics, int x, int y, int width, int height, int z, CallbackInfo ci) {
        ItemStack stack = TooltipContext.get();
        if (stack.isEmpty()) {
            return;
        }
        Optional<GearTier> tier = GearTierItemHelper.getTier(stack);
        if (tier.isPresent() && tier.get().visualize()) {
            RarityCardRenderer.drawTooltipCard(graphics, x, y, width, height, z, tier.get());
            ci.cancel();
        }
    }
}
