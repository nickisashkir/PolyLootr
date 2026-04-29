package dev.ashkir.polylootr.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON-backed configuration. Loaded once at mod init; not reloaded at runtime.
 *
 * <p>Lives at {@code <gameDir>/config/polylootr.json}. Missing fields fall back to
 * defaults; a fresh file is written if absent or unparseable.
 */
public final class PolyLootrConfig {
    private static final Logger LOG = LoggerFactory.getLogger("polylootr");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "polylootr.json";

    private static PolyLootrConfig INSTANCE;

    public boolean unopenedParticlesEnabled = true;
    public int unopenedParticleIntervalTicks = 40;
    public int unopenedParticleCount = 3;
    public String unopenedParticleId = "minecraft:enchant";

    public boolean breakEffectEnabled = true;
    public String breakEffectParticleId = "minecraft:dust_plume";
    public int breakEffectParticleCount = 7;

    public boolean refreshBurstEnabled = true;
    public String refreshBurstParticleId = "minecraft:happy_villager";
    public int refreshBurstParticleCount = 20;

    public boolean firstOpenSoundEnabled = true;
    public String firstOpenSoundId = "minecraft:entity.experience_orb.pickup";
    public float firstOpenSoundVolume = 0.6f;
    public float firstOpenSoundPitch = 1.4f;

    public boolean markerEnabled = true;
    public boolean markerHideAfterOpen = true;
    public boolean markerGlowingEnabled = true;
    /**
     * Glow outline color for the marker, as 0xRRGGBB (no alpha). {@code -1} means
     * "no override" — vanilla picks the entity's team color (white if no team).
     */
    public int markerGlowColor = 0x55FFFF;

    /**
     * When {@code true}, markers use Lootr's bundled "gold_planks" / "silver_planks"
     * block textures (shipped with PolyLootr's resource pack) instead of the
     * vanilla items in {@link #markerItems}. Players must accept the server
     * resource pack on join for the custom textures to appear.
     */
    public boolean useLootrTextures = false;

    /**
     * Help string serialized into the JSON to document {@link #markerItems}.
     * Gson preserves it round-trip so server admins reading the file see what
     * each option does without consulting external docs.
     */
    public String markerItemsHelp =
            "Per-container-type marker items. Map keys are container types (chest, " +
            "trapped_chest, barrel, shulker_box, suspicious_sand, suspicious_gravel, " +
            "decorated_pot). Values are vanilla item ids; invalid ids fall back to the " +
            "wrapper's built-in default for that type. The 'markerItemId' field below " +
            "globally overrides every entry here when non-empty.";

    public Map<String, String> markerItems = defaultMarkerItems();

    /**
     * Global override for the marker item. Empty string means "use the per-container-type
     * map below". Set to a vanilla item id (e.g. {@code "minecraft:gold_nugget"}) to use
     * the same item on every Lootr container regardless of type.
     */
    public String markerItemId = "";

    public String trophyDisplayItemId = "minecraft:gold_ingot";

    public boolean commandsEnabled = true;
    public int commandPermissionLevel = 2;

    public boolean menuTitleInfoEnabled = true;
    public String menuTitleRefreshSuffix = " §a(refresh in %s)";
    public String menuTitleDecaySuffix = " §c(decay in %s)";
    public String menuTitleOpenersSuffix = " §7[%d opener%s]";

    private static Map<String, String> defaultMarkerItems() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("chest", "minecraft:amethyst_shard");
        m.put("trapped_chest", "minecraft:redstone");
        m.put("barrel", "minecraft:wheat");
        m.put("shulker_box", "minecraft:ender_pearl");
        m.put("suspicious_sand", "minecraft:brush");
        m.put("suspicious_gravel", "minecraft:brush");
        m.put("decorated_pot", "minecraft:brush");
        return m;
    }

    public static PolyLootrConfig get() {
        return INSTANCE;
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        PolyLootrConfig loaded = null;
        if (Files.exists(path)) {
            try {
                loaded = GSON.fromJson(Files.readString(path), PolyLootrConfig.class);
            } catch (IOException | JsonSyntaxException e) {
                LOG.warn("Failed to read {}, using defaults: {}", FILE_NAME, e.getMessage());
            }
        }
        INSTANCE = loaded != null ? loaded : new PolyLootrConfig();
        INSTANCE.fillDefaults();
        write(path);
    }

    /**
     * Ensures all marker-item map keys are present after loading an older config
     * that may not have the field, or has it but is missing some keys (e.g. user
     * added a new container type). Existing user values are never overwritten.
     */
    private void fillDefaults() {
        if (markerItems == null) markerItems = new LinkedHashMap<>();
        for (var e : defaultMarkerItems().entrySet()) {
            markerItems.putIfAbsent(e.getKey(), e.getValue());
        }
        if (markerItemsHelp == null || markerItemsHelp.isEmpty()) {
            markerItemsHelp = new PolyLootrConfig().markerItemsHelp;
        }
    }

    private static void write(Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            LOG.warn("Failed to write {}: {}", FILE_NAME, e.getMessage());
        }
    }

    public ParticleOptions unopenedParticle() {
        return resolveSimpleParticle(unopenedParticleId, ParticleTypes.ENCHANT);
    }

    public ParticleOptions breakEffectParticle() {
        return resolveSimpleParticle(breakEffectParticleId, ParticleTypes.DUST_PLUME);
    }

    public ParticleOptions refreshBurstParticle() {
        return resolveSimpleParticle(refreshBurstParticleId, ParticleTypes.HAPPY_VILLAGER);
    }

    public SoundEvent firstOpenSound() {
        return resolveSound(firstOpenSoundId, SoundEvents.EXPERIENCE_ORB_PICKUP);
    }

    /**
     * Resolves the marker item for a given container type, applying overrides
     * in priority order:
     * <ol>
     *   <li>Global {@link #markerItemId} (if non-empty and resolvable)</li>
     *   <li>Per-type entry in {@link #markerItems} (if present and resolvable)</li>
     *   <li>The hardcoded fallback passed by the caller (the renderer's compile-time default)</li>
     * </ol>
     */
    public Item markerItemFor(String type, Item fallback) {
        Item global = resolveOptionalItem(markerItemId);
        if (global != null) return global;
        if (markerItems != null) {
            Item perType = resolveOptionalItem(markerItems.get(type));
            if (perType != null) return perType;
        }
        return fallback;
    }

    public Item trophyDisplayItem() {
        return resolveItem(trophyDisplayItemId, Items.GOLD_INGOT);
    }

    /**
     * Resolves an item id string to an Item, returning {@code null} if the id is
     * empty, unparseable, or maps to an unregistered item. Useful for "unset" semantics.
     */
    private static Item resolveOptionalItem(String idString) {
        if (idString == null || idString.isEmpty()) return null;
        Identifier id = Identifier.tryParse(idString);
        if (id == null) return null;
        Item item = BuiltInRegistries.ITEM.getValue(id);
        return item == Items.AIR ? null : item;
    }

    private static ParticleOptions resolveSimpleParticle(String idString, SimpleParticleType fallback) {
        Identifier id = Identifier.tryParse(idString);
        if (id == null) return fallback;
        var type = BuiltInRegistries.PARTICLE_TYPE.getValue(id);
        return type instanceof ParticleOptions opts ? opts : fallback;
    }

    private static Item resolveItem(String idString, Item fallback) {
        Identifier id = Identifier.tryParse(idString);
        if (id == null) return fallback;
        Item item = BuiltInRegistries.ITEM.getValue(id);
        return item == Items.AIR ? fallback : item;
    }

    private static SoundEvent resolveSound(String idString, SoundEvent fallback) {
        Identifier id = Identifier.tryParse(idString);
        if (id == null) return fallback;
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(id);
        return sound != null ? sound : fallback;
    }
}
