# Custom Gear Modifiers

**A data-driven gear rarity & modifier framework for Minecraft 1.20.1 — Fabric & Forge.**

> ⚙️ **This is a library / dependency mod for modpack makers and mod developers.** On its own it adds no items, blocks, or recipes. It lets you define unlimited rarity tiers in JSON, stamp them onto any item, query them through a small API, and it renders premium tier visuals. What a tier's stats actually *do* is up to the mod or pack that uses the data.

---

## ✨ What it does

- **Infinite, datapack-defined tiers** — no hardcoded rarities. Define as many as you want, under your own namespace.
- **Arbitrary substats** — each tier rolls a set of named min/max ranges (e.g. `reload_speed`, `crit_chance`, `armor_penetration`). The names are entirely yours.
- **Tier ranks & multipliers** — an integer rank and a global stat multiplier per tier, for clear distinctions and easy scaling.
- **Premium visuals** (all toggleable per tier):
  - A **glassmorphic tooltip card** tinted with the rarity color.
  - A colored rarity name with a **roll-quality gradient** — better rolls glow brighter. Hold **Alt** for exact ranges and percentages.
  - An **ARPG-style loot beam** above dropped items.
- **Global registry + stable item NBT contract** so any other mod can read a stack's tier and rolls.

## 🎒 For modpack / datapack authors

Drop tier files into any datapack at:

`data/<your_namespace>/gear_tiers/<tier_name>.json`

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

Changes apply on world load and `/reload`. An op-only `/cgmtiers` command (`list` / `info` / `apply`) lets you inspect tiers and stamp the held item for testing.

## 🛠️ For developers

- Look up tiers via `GearTierManager`.
- Read/write tiers on items via `GearTierItemHelper` (`applyTier`, `getTier`, `getRolledSubstats`).
- Or read the stable NBT directly — `CustomGearTier`, `CustomGearTierRank`, `CustomGearMultiplier`, `CustomGearSubstats`.

> **Applying effects is the consumer's job.** This mod stores the tier, multiplier, and rolled substats, and renders the visuals — it does not change weapon/entity stats itself. Read the values and feed them into your own attribute system or another mod's API.

Full JSON schema and API docs are in the [README](https://github.com/Dezolated/custom-gear-modifiers#readme).

## 📦 Requirements

- Minecraft **1.20.1**
- **Fabric:** requires **Fabric API**
- **Forge:** 47+

## 🔗 Links

- **Source & issues:** https://github.com/Dezolated/custom-gear-modifiers
- **License:** MIT
