package com.dezolated.customgearmodifiers.client;

import net.minecraft.world.item.ItemStack;

/**
 * Holds the {@link ItemStack} whose tooltip is currently being rendered.
 *
 * <p>Vanilla's {@code TooltipRenderUtil#renderTooltipBackground} is not given the item it belongs to,
 * so {@code MixinGuiGraphics} stashes the stack here around the item-tooltip render call and the
 * background mixin reads it back. GUI rendering is single-threaded, so a plain static field is safe.
 */
public final class TooltipContext {

    private static ItemStack current = ItemStack.EMPTY;

    private TooltipContext() {
    }

    public static void set(ItemStack stack) {
        current = stack == null ? ItemStack.EMPTY : stack;
    }

    public static ItemStack get() {
        return current;
    }

    public static void clear() {
        current = ItemStack.EMPTY;
    }
}
