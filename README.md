# PolyLootr

A server-side Fabric mod that lets vanilla Minecraft clients connect to a Lootr server. Lootr's custom blocks, items, and entities are translated to their nearest vanilla equivalents via [Polymer](https://github.com/Patbox/polymer), so unmodded clients see normal chests / barrels / shulkers in the world while the server keeps Lootr's per-player loot mechanics intact.

## Features

- **Vanilla client compatibility.** All Lootr containers (chest, barrel, trapped chest, shulker, decorated pot, brushable blocks, trophy) appear as the closest vanilla counterpart on unmodded clients.
- **Floating amethyst shard marker.** Every Lootr container has a small amethyst shard floating above it as a persistent visual signal — this tells players "this is a Lootr container, expect per-player loot inside." It's a deliberate departure from native Lootr's chest texture (which vanilla clients can't see anyway) so server admins can explain the symbol to players: *amethyst above the chest = Lootr loot*.
- **Per-player "unlooted" particles.** On top of the marker, players see a configurable particle effect (default: enchant sparkles) above containers they haven't personally opened yet. Once a player opens the container, the sparkles stop for them but the amethyst marker stays visible (so they still know it's a Lootr container) and other players keep seeing both signals.
- **Break-effect particles.** Native Lootr's break particles run client-side and aren't visible to vanilla clients; PolyLootr emits them server-side via vanilla particle packets.
- **Trophy as a real trophy.** Lootr's trophy block renders as a blast furnace with a floating gold ingot above it, instead of a plain placeholder.
- **Datapack compatibility.** Datapacks that use vanilla barrels or chests as custom-block GUIs (waystones, custom shops, etc.) won't be swallowed by Lootr's container conversion.
- **Registry sync hygiene.** Lootr's custom stat and particle types are marked server-only so vanilla clients don't warn about unknown registry entries.
- **JSON config** at `config/polylootr.json` for toggling each effect, swapping particle types, and changing the marker / trophy display items.

## What players see

| State | Visual |
|---|---|
| Lootr container, you haven't opened it | Floating amethyst shard above + enchant sparkles |
| Lootr container, you've already opened it | Floating amethyst shard above (no sparkles) |
| Lootr trophy block | Blast furnace shape with a floating gold ingot above |
| Lootr container being broken | Brief dust-plume particle burst |
| Plain vanilla container | Nothing extra |

The amethyst shard is configurable — set `markerEnabled: false` in the config to disable it entirely, or change `markerItemId` to a different vanilla item if your players want a different signal (e.g. `minecraft:gold_nugget`, `minecraft:nether_star`).

## Requirements

- Minecraft 26.1.x (server)
- Fabric Loader 0.18.5+
- Fabric API
- [Lootr](https://modrinth.com/mod/lootr)
- [Polymer](https://modrinth.com/mod/polymer) (core + virtual-entity)

PolyLootr bundles polymer-core and polymer-virtual-entity via JIJ. If you want a single-jar install, just drop PolyLootr in alongside Lootr; if you already have Polymer separately, they coexist fine.

## Installation

1. Drop `PolyLootr-1.0.0+26.1.jar` into your server's `mods/` folder alongside Lootr and Fabric API.
2. Start the server. PolyLootr will create `config/polylootr.json` on first run.
3. Vanilla clients can now connect; they'll see Lootr containers as plain vanilla containers with per-player particles indicating unlooted state.

## Configuration

`config/polylootr.json`:

```json
{
  "unopenedParticlesEnabled": true,
  "unopenedParticleIntervalTicks": 40,
  "unopenedParticleCount": 3,
  "unopenedParticleId": "minecraft:enchant",
  "breakEffectEnabled": true,
  "breakEffectParticleId": "minecraft:dust_plume",
  "breakEffectParticleCount": 7,
  "markerEnabled": true,
  "markerItemId": "minecraft:amethyst_shard",
  "trophyDisplayItemId": "minecraft:gold_ingot"
}
```

Restart the server to apply config changes.

## License

MIT. PolyLootr is an independent implementation; it depends on the Lootr API at runtime but ships none of Lootr's source code.
