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
    /**
     * Global override for the marker item. Empty string means "use the per-container-type
     * default" (chest = amethyst shard, barrel = wheat, shulker = ender pearl, etc.).
     * Set to a vanilla item id (e.g. {@code "minecraft:gold_nugget"}) to use the same
     * item on every Lootr container.
     */
    public String markerItemId = "";

    public String trophyDisplayItemId = "minecraft:gold_ingot";

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
        write(path);
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
     * Returns the global marker-item override if {@link #markerItemId} is set, otherwise
     * {@code null} so callers know to use the per-container-type default.
     */
    public Item markerItemOverride() {
        if (markerItemId == null || markerItemId.isEmpty()) return null;
        Identifier id = Identifier.tryParse(markerItemId);
        if (id == null) return null;
        Item item = BuiltInRegistries.ITEM.getValue(id);
        return item == Items.AIR ? null : item;
    }

    public Item trophyDisplayItem() {
        return resolveItem(trophyDisplayItemId, Items.GOLD_INGOT);
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
