# Glassmorphic Rarity Cards — Design

**Date:** 2026-06-29
**Status:** Approved
**Mod:** Custom Gear Modifiers (`customgearmodifiers`), MC 1.20.1, MultiLoader (common/fabric/forge)

## Goal

Make tier-tagged items feel premium: render a stylized "glassmorphic" card on both the
**hover tooltip** and the **inventory slot**, using the item's rarity color. Translucent
rarity-tinted background, rarity-colored border, top sheen, and a subtle animated sheen sweep +
breathing border glow that scales with tier rarity.

Decisions (from brainstorming):
- Surfaces: **tooltip + slot**.
- Fidelity: **stylized glass, no real backdrop blur**.
- Motion: **subtle animated sheen** (sweep + pulsing border), intensity scaling with `stat_multiplier`.

## Approach: shared common client mixins

All logic in the **common** module via client mixins (the template already applies common client
mixins on both loaders). One implementation, identical on Fabric and Forge. Drawing is isolated in a
single renderer class; mixins are thin and delegate to it.

Rejected: Forge `RenderTooltipEvent.Color` (can't do gradient/sheen/custom border, no slot support);
custom `ClientTooltipComponent` (can't own the full background/border, no slot support).

## Components

1. **`client.RarityCardRenderer`** — pure drawing. `drawTooltipCard(GuiGraphics, x, y, w, h, GearTier)`
   and `drawSlotCard(GuiGraphics, x, y, GearTier)`. Owns animation phase from `Util.getMillis()`.
   Color math derived from `GearTier.colorValue()` and `statMultiplier`.
2. **`client.TooltipContext`** — static holder for the ItemStack currently being tooltip-rendered
   (the background method isn't passed the stack). Single-threaded render use.
3. **`mixin.client.MixinGuiGraphics`** — `renderTooltip(Font, ItemStack, int, int)`: set
   `TooltipContext` at HEAD, clear at RETURN.
4. **`mixin.client.MixinTooltipRenderUtil`** — `renderTooltipBackground(GuiGraphics,int,int,int,int,int)`
   @HEAD cancellable: if the held stack has a visualized tier, draw the tooltip card and cancel vanilla.
5. **`mixin.client.MixinAbstractContainerScreen`** — `renderSlot(GuiGraphics, Slot)` @HEAD: if
   `slot.getItem()` has a visualized tier, draw the slot card behind the icon.

**Gating:** every surface only renders when `GearTierItemHelper.getTier(stack)` is present AND
`tier.visualize()` is true. The existing JSON `visualize` flag now also controls these visuals.

## Visual spec

- **Background:** vertical `fillGradient` of rarity color, ~`0x33` alpha top → ~`0x14` bottom, over a
  faint dark base for legibility.
- **Border:** 1px rarity color at ~`0xCC` alpha on all edges; tooltip gets framed inner+outer edge,
  slot gets a single crisp border.
- **Sheen:** soft white top-edge highlight; plus a diagonal translucent band that sweeps across on a
  ~3–4s loop. Border alpha breathes on a sine wave. Sweep speed & glow intensity scale with
  `statMultiplier`.
- **Rendering:** `GuiGraphics.fill` / `fillGradient` with ARGB alpha (gui render type blends).

## Multiloader wiring & safety

- Add the three mixins to `common/src/main/resources/customgearmodifiers.mixins.json` under `"client"`
  (client mixins are not applied on dedicated servers).
- No new dependencies; pure vanilla `GuiGraphics`.

## Testing

Dev client: `/cgmtiers apply <tier>` then hover (tooltip card) and view in inventory (slot card).
Verify `visualize:false` renders no card but still tags data; a plain item is untouched; a dedicated
server still launches (client classes not loaded).

## Known caveats

Mixing into `renderTooltipBackground` / `renderSlot` can conflict with other mods that retexture
tooltips or slots. Standard approach; conflicts possible.

(No git repo in workspace, so this doc is saved but not committed.)
