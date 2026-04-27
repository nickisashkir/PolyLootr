# PolyLootr

A server-side Fabric mod that lets vanilla Minecraft clients connect to a Lootr server. Lootr's custom blocks, items, and entities are translated to their nearest vanilla equivalents via [Polymer](https://github.com/Patbox/polymer), so unmodded clients see normal chests / barrels / shulkers in the world while the server keeps Lootr's per-player loot mechanics intact.

## Features

- **Vanilla client compatibility.** All Lootr containers (chest, barrel, trapped chest, shulker, decorated pot, brushable blocks, trophy) appear as the closest vanilla counterpart on unmodded clients.
- **Per-type marker items.** Each container type floats a thematic vanilla item above it so players can tell what's inside at a glance:
  - chest = amethyst shard, trapped chest = redstone, barrel = wheat, shulker = ender pearl, suspicious sand/gravel + decorated pot = brush. Trophy block stays gold ingot.
  - Globally override with one item via `markerItemId` in the config, or disable entirely with `markerEnabled: false`.
- **Per-player "unlooted" particles.** On top of the marker, players see a configurable particle effect (default: enchant sparkles) above containers they haven't personally opened yet. Once a player opens the container, the sparkles stop for them but the marker stays visible.
- **Refresh-burst particles.** When a Lootr container's refresh timer hits and it has fresh loot to give, PolyLootr fires a one-shot particle burst (default: happy villager green) so nearby players notice without needing to interact first.
- **First-open sound.** A configurable vanilla sound (default: experience-orb pickup, mid-pitched) plays for a player the first time they open a Lootr container — reinforces the "fresh loot" feel without relying on chat messages.
- **Break-effect particles.** Native Lootr's break particles run client-side and aren't visible to vanilla clients; PolyLootr emits them server-side via vanilla particle packets.
- **Trophy as a real trophy.** Lootr's trophy block renders as a blast furnace with a floating gold ingot above it, instead of a plain placeholder.
- **Datapack compatibility.** Datapacks that use vanilla barrels or chests as custom-block GUIs (waystones, custom shops, etc.) won't be swallowed by Lootr's container conversion.
- **Registry sync hygiene.** Lootr's custom stat and particle types are marked server-only so vanilla clients don't warn about unknown registry entries.
- **JSON config** at `config/polylootr.json` for toggling each effect, swapping particle/sound types, and changing marker / trophy display items.

## What players see and hear

| Event / state | Effect |
|---|---|
| Lootr container, never opened by you | Floating type-themed item marker (amethyst / redstone / wheat / etc.) + enchant sparkles |
| You open a Lootr container for the first time | Brief sound effect (default: xp-pickup-style chime) |
| Lootr container, already opened by you | Marker stays; sparkles stop for you (still on for others) |
| Lootr container's loot refreshes | Burst of happy-villager particles broadcast to nearby players |
| Lootr trophy block | Blast furnace shape with a floating gold ingot above |
| Lootr container being broken | Brief dust-plume particle burst |
| Plain vanilla container | Nothing extra |

All visuals and the sound are toggleable / swappable via `config/polylootr.json`.

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

PolyLootr writes a default `config/polylootr.json` on first launch:

```json
{
  "unopenedParticlesEnabled": true,
  "unopenedParticleIntervalTicks": 40,
  "unopenedParticleCount": 3,
  "unopenedParticleId": "minecraft:enchant",
  "breakEffectEnabled": true,
  "breakEffectParticleId": "minecraft:dust_plume",
  "breakEffectParticleCount": 7,
  "refreshBurstEnabled": true,
  "refreshBurstParticleId": "minecraft:happy_villager",
  "refreshBurstParticleCount": 20,
  "firstOpenSoundEnabled": true,
  "firstOpenSoundId": "minecraft:entity.experience_orb.pickup",
  "firstOpenSoundVolume": 0.6,
  "firstOpenSoundPitch": 1.4,
  "markerEnabled": true,
  "markerItemId": "",
  "trophyDisplayItemId": "minecraft:gold_ingot"
}
```

Restart the server to apply config changes — PolyLootr loads the file once at mod init and does not hot-reload.

### Config options

| Key | Type | Default | Effect |
|---|---|---|---|
| `unopenedParticlesEnabled` | bool | `true` | Master toggle for the per-player sparkle particles on unopened containers. Set `false` to rely on the marker only. |
| `unopenedParticleIntervalTicks` | int | `40` | How often (in server ticks, 20 = 1 second) each container emits a sparkle. Lower = more frequent / more network traffic. |
| `unopenedParticleCount` | int | `3` | Particles per emission burst. Higher = denser visual. |
| `unopenedParticleId` | string | `minecraft:enchant` | Particle type id. Any vanilla simple particle works (e.g. `minecraft:end_rod`, `minecraft:happy_villager`, `minecraft:soul_fire_flame`). Invalid ids fall back to enchant. |
| `breakEffectEnabled` | bool | `true` | Whether to emit a particle burst when a Lootr container is broken. |
| `breakEffectParticleId` | string | `minecraft:dust_plume` | Particle type for the break burst. |
| `breakEffectParticleCount` | int | `7` | Particles in the break burst. |
| `refreshBurstEnabled` | bool | `true` | Whether to broadcast a particle burst when a container's refresh timer fires. |
| `refreshBurstParticleId` | string | `minecraft:happy_villager` | Particle type for the refresh burst. Try `minecraft:totem_of_undying` for a more dramatic effect. |
| `refreshBurstParticleCount` | int | `20` | Particles in the refresh burst. |
| `firstOpenSoundEnabled` | bool | `true` | Whether to play a sound to a player on their first open of any Lootr container. |
| `firstOpenSoundId` | string | `minecraft:entity.experience_orb.pickup` | Vanilla sound id. Try `minecraft:block.amethyst_block.chime`, `minecraft:entity.player.levelup`, or `minecraft:block.note_block.bell`. |
| `firstOpenSoundVolume` | float | `0.6` | Sound volume (0.0 – 1.0+). |
| `firstOpenSoundPitch` | float | `1.4` | Sound pitch (0.5 = octave down, 2.0 = octave up). |
| `markerEnabled` | bool | `true` | Whether to render the floating item marker above Lootr containers. |
| `markerItemId` | string | `""` (empty) | Empty = use the per-container-type defaults (chest = amethyst, barrel = wheat, etc.). Set to a vanilla item id (e.g. `minecraft:gold_nugget`) to use the same item on all containers. |
| `trophyDisplayItemId` | string | `minecraft:gold_ingot` | Item shown floating above trophy blocks. |

## Refilling already-looted chests

Lootr's automatic conversion only catches **vanilla chests that still have their `LootTable` NBT tag**. World-generated chests get this tag at chunk generation; the tag stays until a player opens the chest, at which point vanilla rolls the table once and strips the tag.

This means: **chests opened by anyone before Lootr was installed will NOT auto-convert when Lootr starts running.** They look like ordinary empty chests to Lootr, and players who arrive later get nothing.

The fix is one of Lootr's built-in commands. Run as op or from console:

### Single chest

```
/lootr custom-chest <x> <y> <z> <loot_table>
```

Example: `/lootr custom-chest 100 40 -200 minecraft:chests/simple_dungeon` converts the block at those coordinates into a fresh Lootr chest using the simple dungeon loot table. Every player gets their own roll going forward.

### Bulk (entire dimension)

```
/lootr custom-map <level> <loot_table>
```

This walks the dimension and converts every matching vanilla container. Use carefully — best for fresh server setup, not live worlds.

### Common loot tables

If you don't know which table the chest originally rolled, pick the closest match:

| Structure | Loot table |
|---|---|
| Simple dungeon (cobble + spawner) | `minecraft:chests/simple_dungeon` |
| Abandoned mineshaft | `minecraft:chests/abandoned_mineshaft` |
| Stronghold corridor | `minecraft:chests/stronghold_corridor` |
| Stronghold crossing | `minecraft:chests/stronghold_crossing` |
| Stronghold library | `minecraft:chests/stronghold_library` |
| Desert pyramid | `minecraft:chests/desert_pyramid` |
| Jungle temple | `minecraft:chests/jungle_temple` |
| Buried treasure | `minecraft:chests/buried_treasure` |
| Shipwreck (treasure) | `minecraft:chests/shipwreck_treasure` |
| Shipwreck (supply) | `minecraft:chests/shipwreck_supply` |
| Shipwreck (map) | `minecraft:chests/shipwreck_map` |
| End city treasure | `minecraft:chests/end_city_treasure` |
| Nether bridge | `minecraft:chests/nether_bridge` |
| Bastion treasure | `minecraft:chests/bastion_treasure` |
| Bastion bridge | `minecraft:chests/bastion_bridge` |
| Bastion hoglin stable | `minecraft:chests/bastion_hoglin_stable` |
| Bastion other | `minecraft:chests/bastion_other` |
| Pillager outpost | `minecraft:chests/pillager_outpost` |
| Igloo basement chest | `minecraft:chests/igloo_chest` |
| Trial chamber reward | `minecraft:chests/trial_chambers/reward` |
| Ancient city | `minecraft:chests/ancient_city` |
| Ancient city ice box | `minecraft:chests/ancient_city_ice_box` |
| Ruined portal | `minecraft:chests/ruined_portal` |
| Woodland mansion | `minecraft:chests/woodland_mansion` |
| Village (generic) | `minecraft:chests/village/village_weaponsmith` |

When in doubt, `minecraft:chests/simple_dungeon` is a safe generic default — decent random gear without being overpowered.

### Other useful commands

- `/lootr clear` — clears the opener list on a Lootr container. Every player can re-loot it from scratch.
- `/lootr refresh` — regenerates loot for a Lootr container without clearing the opener list.

## License

MIT. PolyLootr is an independent implementation; it depends on the Lootr API at runtime but ships none of Lootr's source code.
