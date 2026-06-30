package com.dezolated.customgearmodifiers.command;

import com.dezolated.customgearmodifiers.geartier.GearTier;
import com.dezolated.customgearmodifiers.geartier.GearTierItemHelper;
import com.dezolated.customgearmodifiers.geartier.GearTierManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Map;

/**
 * Developer/admin command tree for inspecting and applying data-driven gear tiers.
 *
 * <p>The Brigadier wiring lives here in the common module so both loaders share one implementation;
 * each loader only forwards its dispatcher (Fabric via {@code CommandRegistrationCallback}, Forge via
 * {@code RegisterCommandsEvent}).
 *
 * <pre>
 *   /cgmtiers list             - list every loaded tier
 *   /cgmtiers info  &lt;tier&gt;     - print one tier's properties
 *   /cgmtiers apply &lt;tier&gt;     - stamp the tier onto the held item (rolls substats)
 * </pre>
 */
public final class GearTierCommands {

    private static final SimpleCommandExceptionType ERROR_EMPTY_HAND =
            new SimpleCommandExceptionType(Component.literal("You must be holding an item to apply a tier."));
    private static final SimpleCommandExceptionType ERROR_UNKNOWN_TIER =
            new SimpleCommandExceptionType(Component.literal("No loaded gear tier with that id."));

    /** Suggests tier ids from the live registry as the player types. */
    private static final SuggestionProvider<CommandSourceStack> TIER_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(GearTierManager.ids(), builder);

    private GearTierCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cgmtiers")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list")
                        .executes(GearTierCommands::listTiers))
                .then(Commands.literal("info")
                        .then(Commands.argument("tier", ResourceLocationArgument.id())
                                .suggests(TIER_SUGGESTIONS)
                                .executes(GearTierCommands::tierInfo)))
                .then(Commands.literal("apply")
                        .then(Commands.argument("tier", ResourceLocationArgument.id())
                                .suggests(TIER_SUGGESTIONS)
                                .executes(GearTierCommands::applyTier))));
    }

    private static int listTiers(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (GearTierManager.size() == 0) {
            source.sendSuccess(() -> Component.literal("No gear tiers are loaded. Add JSON under data/<namespace>/gear_tiers/ and run /reload.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Loaded gear tiers (" + GearTierManager.size() + "):")
                .withStyle(ChatFormatting.GOLD), false);
        for (GearTier tier : GearTierManager.all()) {
            source.sendSuccess(() -> Component.literal(" - ")
                    .append(coloredName(tier))
                    .append(Component.literal(" (" + tier.id() + ")").withStyle(ChatFormatting.DARK_GRAY)), false);
        }
        return GearTierManager.size();
    }

    private static int tierInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        GearTier tier = requireTier(context);

        source.sendSuccess(() -> Component.literal("Tier ").withStyle(ChatFormatting.GOLD)
                .append(coloredName(tier))
                .append(Component.literal(" (" + tier.id() + ")").withStyle(ChatFormatting.DARK_GRAY)), false);
        source.sendSuccess(() -> line("tier", String.valueOf(tier.tier())), false);
        source.sendSuccess(() -> line("color", String.format(Locale.ROOT, "%s (#%06X)", tier.hexColor(), tier.colorValue())), false);
        source.sendSuccess(() -> line("stat_multiplier", String.format(Locale.ROOT, "x%.2f", tier.statMultiplier())), false);
        source.sendSuccess(() -> line("visualize", String.valueOf(tier.visualize())), false);
        if (tier.subStats().isEmpty()) {
            source.sendSuccess(() -> line("substats", "none"), false);
        } else {
            source.sendSuccess(() -> line("substats", String.valueOf(tier.subStats().size())), false);
            tier.subStats().forEach((name, range) -> source.sendSuccess(() -> Component.literal("    " + range.displayName(name) + " (" + name + "): ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format(Locale.ROOT, "[%.3f, %.3f]", range.min(), range.max()))
                            .withStyle(ChatFormatting.WHITE)), false));
        }
        return 1;
    }

    private static int applyTier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        GearTier tier = requireTier(context);

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            throw ERROR_EMPTY_HAND.create();
        }

        Map<String, Float> rolled = GearTierItemHelper.applyTier(stack, tier, player.getRandom());

        source.sendSuccess(() -> Component.literal("Applied tier ")
                .withStyle(ChatFormatting.GREEN)
                .append(coloredName(tier))
                .append(Component.literal(" to " + stack.getCount() + "x " + stack.getItem()).withStyle(ChatFormatting.GREEN)), false);
        if (rolled.isEmpty()) {
            source.sendSuccess(() -> line("rolled", "no substats defined"), false);
        } else {
            rolled.forEach((name, value) -> source.sendSuccess(() -> Component.literal("    " + name + " = ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format(Locale.ROOT, "%.3f", value)).withStyle(ChatFormatting.WHITE)), false));
        }
        if (!tier.visualize()) {
            source.sendSuccess(() -> Component.literal("    (visualize=false: data written, no built-in styling applied)")
                    .withStyle(ChatFormatting.DARK_GRAY), false);
        }
        return 1;
    }

    private static GearTier requireTier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(context, "tier");
        return GearTierManager.get(id).orElseThrow(ERROR_UNKNOWN_TIER::create);
    }

    private static Component coloredName(GearTier tier) {
        return Component.literal(tier.displayName())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(tier.colorValue())));
    }

    private static Component line(String key, String value) {
        return Component.literal("  " + key + ": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
    }
}
