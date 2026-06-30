package com.dezolated.customgearmodifiers.mixin.client;

import com.dezolated.customgearmodifiers.client.RarityBeamRenderer;
import com.dezolated.customgearmodifiers.geartier.GearTier;
import com.dezolated.customgearmodifiers.geartier.GearTierItemHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds a rarity-colored loot beam above dropped items whose tier opts into it. Injected at HEAD so the
 * beam is drawn at the item's stable position, before the renderer applies item bob/spin transforms.
 */
@Mixin(ItemEntityRenderer.class)
public class MixinItemEntityRenderer {

    @Inject(method = "render", at = @At("HEAD"))
    private void customgearmodifiers$lootBeam(ItemEntity entity, float entityYaw, float partialTicks,
                                              PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            return;
        }
        GearTierItemHelper.getTier(stack)
                .filter(GearTier::lootBeam)
                .ifPresent(tier -> RarityBeamRenderer.render(poseStack, tier));
    }
}
