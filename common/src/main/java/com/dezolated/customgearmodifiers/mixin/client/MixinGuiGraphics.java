package com.dezolated.customgearmodifiers.mixin.client;

import com.dezolated.customgearmodifiers.client.TooltipContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the item being tooltip-rendered so {@link MixinTooltipRenderUtil} knows which rarity card
 * (if any) to draw — vanilla's background method isn't told the stack.
 */
@Mixin(GuiGraphics.class)
public class MixinGuiGraphics {

    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
    private void customgearmodifiers$captureTooltipStack(Font font, ItemStack stack, int x, int y, CallbackInfo ci) {
        TooltipContext.set(stack);
    }

    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("RETURN"))
    private void customgearmodifiers$clearTooltipStack(Font font, ItemStack stack, int x, int y, CallbackInfo ci) {
        TooltipContext.clear();
    }
}
