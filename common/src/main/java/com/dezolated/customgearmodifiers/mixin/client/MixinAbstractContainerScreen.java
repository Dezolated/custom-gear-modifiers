package com.dezolated.customgearmodifiers.mixin.client;

import com.dezolated.customgearmodifiers.client.TooltipContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Container screens render item tooltips through the {@code List}-based overload, not
 * {@code GuiGraphics#renderTooltip(Font, ItemStack, int, int)}, so we publish the hovered slot's
 * stack to {@link TooltipContext} for the duration of the tooltip render. {@link MixinTooltipRenderUtil}
 * then knows which rarity card (if any) to draw behind the tooltip.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen {

    @Shadow
    protected Slot hoveredSlot;

    @Inject(method = "renderTooltip", at = @At("HEAD"))
    private void customgearmodifiers$captureHoveredStack(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            TooltipContext.set(this.hoveredSlot.getItem());
        }
    }

    @Inject(method = "renderTooltip", at = @At("RETURN"))
    private void customgearmodifiers$clearHoveredStack(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        TooltipContext.clear();
    }
}
