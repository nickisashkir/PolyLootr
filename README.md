# PolyLootr

A server-side Fabric mod that lets vanilla Minecraft clients connect to a Lootr server. Lootr's custom blocks, items, and entities are translated to their nearest vanilla equivalents via [Polymer](https://github.com/Patbox/polymer), so unmodded clients see normal chests / barrels / shulkers in the world while the server keeps Lootr's per-player loot mechanics intact.

## Features

- **Vanilla client compatibility.** All Lootr containers (chest, barrel, trapped chest, shulker, decorated pot, brushable blocks, trophy) appear as the closest vanilla counterpart on unmodded clients.
- **Per-type marker items, fully configurable.** Each container type floats a thematic vanilla item above it so players can tell what's inside at a glance. Each type's item is editable in the config:
  - chest = amethyst shard, trapped chest = redstone, barrel = wheat, shulker = ender pearl, suspicious sand/gravel + decorated pot = brush. Trophy block stays gold ingot.
  - Edit any value under `markerItems` in the config to swap a single type's item. Set the global `markerItemId` to one item to override every type at once. Set `markerEnabled: false` to hide markers entirely.
- **Marker hides for players who already looted (v1.2+).** Once a player opens a Lootr container, the floating marker disappears for *them* — easy way to see at a glance which chests you've already cleared. Other players keep seeing it until they too open it. Toggle with `markerHideAfterOpen` in the config.
- **Refresh-aware re-show (v1.4+).** When a Lootr container's loot refreshes, the marker reappears for previously-opened players too — so nobody misses fresh loot just because they already looted that container. Once anyone opens it post-refresh and consumes the cycle, it goes back to normal hide-after-open behavior.
- **Glowing marker (v1.4+).** The floating marker glows with a configurable color (default cyan) so unopened containers are visible through walls at distance. Set `markerGlowingEnabled: false` to disable, or `markerGlowColor` to change the hue.
- **Menu title shows live state (v1.3+).** When a player opens a Lootr container, the menu title is augmented with refresh / decay countdowns and opener count (e.g. *"Loot Chest §a(refresh in 4m 30s) §7[2 openers]"*). Format strings configurable; disable with `menuTitleInfoEnabled: false`.
- **`/polylootr` admin commands (v1.3+).** Reload config without restart, list Lootr containers near you with their state, and check looted-stat counts:
  - `/polylootr reload`
  - `/polylootr nearby [radius]`
  - `/polylootr stats [player]`
- **Per-player "unlooted" particles.** On top of the marker, players see a configurable particle effect (default: enchant sparkles) above containers they haven't personally opened yet. Once a player opens the container, the sparkles stop for them.
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
| Lootr container, you've already opened it | Marker disappears for you; other players still see it until they open it too |
| Lootr container's loot refreshes | Burst of happy-villager particles broadcast to nearby players |
| Lootr trophy block | Blast furnace shape with a floating gold ingot above |
| Lootr container being broken | Brief dust-plume particle burst |
| Plain vanilla container | Nothing extra |

Every visual and the sound is toggleable / swappable via `config/polylootr.json`. The "marker hides after you open" behavior toggles via `markerHideAfterOpen`.

## Requirements

- Minecraft 26.1.x (server)
- Fabric Loader 0.18.5+
- Fabric API
- [Lootr](https://modrinth.com/mod/lootr)
- [Polymer](https://modrinth.com/mod/polymer) (core + virtual-entity)

PolyLootr bundles polymer-core and polymer-virtual-entity via JIJ. If you want a single-jar install, just drop PolyLootr in alongside Lootr; if you already have Polymer separately, they coexist fine.

## Installation

1. Drop `PolyLootr-1.4.0+26.1.jar` into your server's `mods/` folder alongside Lootr and Fabric API.
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
  "markerHideAfterOpen": true,
  "markerGlowingEnabled": true,
  "markerGlowColor": 5635839,
  "markerItemsHelp": "Per-container-type marker items. Map keys are container types (chest, trapped_chest, barrel, shulker_box, suspicious_sand, suspicious_gravel, decorated_pot). Values are vanilla item ids; invalid ids fall back to the wrapper's built-in default for that type. The 'markerItemId' field below globally overrides every entry here when non-empty.",
  "markerItems": {
    "chest": "minecraft:amethyst_shard",
    "trapped_chest": "minecraft:redstone",
    "barrel": "minecraft:wheat",
    "shulker_box": "minecraft:ender_pearl",
    "suspicious_sand": "minecraft:brush",
    "suspicious_gravel": "minecraft:brush",
    "decorated_pot": "minecraft:brush"
  },
  "markerItemId": "",
  "trophyDisplayItemId": "minecraft:gold_ingot",
  "commandsEnabled": true,
  "commandPermissionLevel": 2,
  "menuTitleInfoEnabled": true,
  "menuTitleRefreshSuffix": " §a(refresh in %s)",
  "menuTitleDecaySuffix": " §c(decay in %s)",
  "menuTitleOpenersSuffix": " §7[%d opener%s]"
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
| `markerHideAfterOpen` | bool | `true` | When true, the marker disappears for any player who has personally opened the container. Other players keep seeing it. When the container's loot refreshes, the marker reappears for previous openers too until someone consumes the refresh. |
| `markerGlowingEnabled` | bool | `true` | Renders the marker with a glowing outline visible through walls. Disable for a flatter look. |
| `markerGlowColor` | int | `5635839` (`0x55FFFF` cyan) | RGB color of the glow outline. Set to `-1` to use the entity's team color (white if no team). Provide as decimal in JSON; common values: `16777215` (white), `16776960` (yellow), `16711935` (magenta), `65535` (cyan). |
| `markerItemsHelp` | string | (auto) | Inline help string the wrapper writes back into the JSON. Edit it freely; PolyLootr only restores it if you blank it. |
| `markerItems.<type>` | string | per type | Per-container-type marker item. Edit a single value to change one type's marker. Container types: `chest`, `trapped_chest`, `barrel`, `shulker_box`, `suspicious_sand`, `suspicious_gravel`, `decorated_pot`. Invalid ids fall back to the wrapper's built-in default. |
| `markerItemId` | string | `""` (empty) | Global override. Empty = use `markerItems` per-type values. Set to a vanilla item id (e.g. `minecraft:gold_nugget`) to force the same item on every Lootr container regardless of type. |
| `trophyDisplayItemId` | string | `minecraft:gold_ingot` | Item shown floating above trophy blocks. |
| `commandsEnabled` | bool | `true` | Whether to register the `/polylootr` command tree. |
| `commandPermissionLevel` | int | `2` | Op level required (0 = ALL, 1 = MODERATORS, 2 = GAMEMASTERS, 3 = ADMINS, 4 = OWNERS). 26.1 redefined permissions; this is mapped to the equivalent `PermissionCheck`. |
| `menuTitleInfoEnabled` | bool | `true` | Whether to append live refresh / decay / opener info to the menu title when a player opens a Lootr container. |
| `menuTitleRefreshSuffix` | string | ` §a(refresh in %s)` | Format string appended when refresh timer is active. `%s` is the formatted duration (e.g. `4m 30s`). Set to empty to suppress. |
| `menuTitleDecaySuffix` | string | ` §c(decay in %s)` | Same, for the decay timer. |
| `menuTitleOpenersSuffix` | string | ` §7[%d opener%s]` | Format string showing how many players have opened. `%d` is the count, `%s` is `""` or `"s"` for pluralization. |

## Commands

All `/polylootr` subcommands require op level >= `commandPermissionLevel` (default 2 / GAMEMASTERS).

| Command | Effect |
|---|---|
| `/polylootr help` | Lists all subcommands. Bare `/polylootr` also runs help. |
| `/polylootr reload` | Re-reads `config/polylootr.json` without restarting. Marker visuals updated on next chunk reload; menu title and command behaviors take effect immediately. |
| `/polylootr nearby [radius]` | Lists every Lootr container within `radius` blocks of you (default 16, max 64). Shows position, opener count, refresh / decay state. |
| `/polylootr stats [player]` | Shows the `lootr:looted_stat` counter for you (or the named player). |
| `/polylootr forget <player> <pos>` | Clears the named player's "opened" state at the Lootr container at `<pos>` so they can re-roll fresh loot. Also wipes their per-player inventory snapshot for that container. Useful for testing or restoring lost access. |

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
