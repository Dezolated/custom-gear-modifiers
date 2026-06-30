# Custom Gear Modifiers

A **data-driven gear rarity & modifier framework** for Minecraft **1.20.1** (Fabric & Forge).

> This is a **library / dependency mod for developers and modpack authors** — not a standalone content
> mod. It adds no items, blocks, or recipes on its own. It lets you define unlimited rarity tiers in
> JSON, stamp them onto any item, query them through a small API, and renders premium tier visuals
> (tooltip card, colored name, loot beam). What a tier's stats actually *do* is decided by the mod or
> pack that consumes the data.

---

## Features

- **Infinite, datapack-defined tiers** — no hardcoded rarities. Define as many as you want under your
  own namespace.
- **Arbitrary substats** — each tier rolls a set of named min/max ranges (e.g. `reload_speed`,
  `crit_chance`); the names are entirely up to you.
- **Global runtime registry** + an item NBT contract so any mod can read a stack's tier and rolls.
- **Premium visuals**: a glassmorphic tooltip card in the rarity color, a colored rarity name with a
  roll-quality gradient (hold **Alt** for ranges + percentages), and an ARPG-style loot beam on
  dropped items. Each is individually toggleable per tier.

## For modpack / datapack authors

Drop tier files into any datapack at:

```
data/<your_namespace>/gear_tiers/<tier_name>.json
```

The file path becomes the tier id (`<your_namespace>:<tier_name>`). Example:

```json
{
    "tier": 5,
    "display_name": "Legendary",
    "color": "#FFAA00",
    "stat_multiplier": 2.5,
    "visualize": true,
    "loot_beam": true,
    "substats": {
        "crit_chance":     { "label": "Crit Chance",     "min": 0.15, "max": 0.35 },
        "ammo_efficiency": { "label": "Ammo Efficiency", "min": 0.20, "max": 0.50 }
    }
}
```

| Field | Type | Required | Default | Meaning |
|-------|------|----------|---------|---------|
| `tier` | int | no | `0` | Integer rank for ordering / gating. Larger = rarer by convention. |
| `display_name` | string | **yes** | — | Name shown on items. |
| `color` | hex string | **yes** | — | Rarity color (`#RRGGBB`, `RRGGBB`, or `0xRRGGBB`). |
| `stat_multiplier` | float | no | `1.0` | Global multiplier stored on the item for consumers to apply. |
| `visualize` | bool | no | `true` | Render the built-in tooltip card + name. Set `false` to apply data only and do your own styling. |
| `loot_beam` | bool | no | `true` | Emit a rarity-colored beam on dropped items of this tier. |
| `substats` | object | no | `{}` | Map of substat key → `{ label?, min, max }`. `label` is optional; without it the key is prettified (`reload_speed` → `Reload Speed`). |

Changes apply on world load and on `/reload`.

### Admin / testing command (op level 2)

```
/cgmtiers list                 # list every loaded tier
/cgmtiers info <tier>          # print a tier's properties
/cgmtiers apply <tier>         # stamp the tier onto the held item (rolls substats)
```

## For mod developers

The data lives in the **common** module; the following are the intended entry points.

**Look up tiers** — `GearTierManager`:
```java
Optional<GearTier> tier = GearTierManager.get(new ResourceLocation("mypack", "mythic"));
Collection<GearTier> all = GearTierManager.all();
```

**Read / write tiers on an item** — `GearTierItemHelper`:
```java
GearTierItemHelper.applyTier(stack, tier, randomSource);     // rolls + writes NBT
Optional<GearTier> t  = GearTierItemHelper.getTier(stack);   // resolves against the registry
Map<String, Float> rolls = GearTierItemHelper.getRolledSubstats(stack);
```

**Stable NBT contract** (read directly, no compile dependency required):

| Key | Type | Contents |
|-----|------|----------|
| `CustomGearTier` | string | tier id, `"namespace:path"` |
| `CustomGearTierRank` | int | the tier's `tier` rank |
| `CustomGearMultiplier` | float | the tier's `stat_multiplier` at apply time |
| `CustomGearSubstats` | compound | rolled substats, `name → float` |

> **Applying effects is the consumer's responsibility.** This mod stores the tier, multiplier, and
> rolled substats and renders the visuals; it does not change weapon/entity stats itself. Read the
> values above and feed them into your own attribute system or another mod's API.

## Loader support

- Minecraft **1.20.1**, **Fabric** and **Forge** from one codebase (MultiLoader template).
- **Fabric requires Fabric API** (used for resource reload, commands, and tooltips).
- All in-world/UI rendering is client-side; dedicated servers load no client classes.

## Building

```
./gradlew build
```

Output jars: `fabric/build/libs/` and `forge/build/libs/`.

## License

CC0-1.0 (see `LICENSE`).
