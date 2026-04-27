package dev.ashkir.polylootr.render;

import dev.ashkir.polylootr.config.PolyLootrConfig;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.ILootrInventoryStore;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Shared element holder driving particle effects for one Lootr container.
 *
 * <p>Caches the {@link ILootrBlockEntity} reference, particle emission origin,
 * and (lazily) the {@link ILootrInventoryStore} on first lookup so subsequent
 * ticks skip lookups.
 *
 * <h2>Per-player unopened sparkles</h2>
 * For each watching player who has not yet opened the container, a configurable
 * particle burst is sent using {@code ServerLevel.sendParticles(player, ...)}.
 * Frequency is controlled by {@link PolyLootrConfig#unopenedParticleIntervalTicks}.
 *
 * <h2>Refresh burst</h2>
 * When the container's inventory store transitions from "not refreshed" to
 * "refreshed" (Lootr's refresh timer hits zero), a one-shot broadcast burst is
 * emitted so all nearby players see "fresh loot here." The transition is
 * detected by polling {@link ILootrInventoryStore#isRefreshed()} every tick;
 * the previous state is cached so the burst fires once per refresh cycle.
 */
public class MarkerHolder extends ElementHolder {
    private int tickCounter = 0;
    private @Nullable ILootrBlockEntity cachedBlockEntity;
    private @Nullable ILootrInventoryStore cachedStore;
    private double emitX;
    private double emitY;
    private double emitZ;
    private boolean cacheReady = false;
    private boolean lastRefreshState = false;

    @Override
    protected void onTick() {
        PolyLootrConfig config = PolyLootrConfig.get();

        var attachment = getAttachment();
        if (attachment == null) return;
        ServerLevel world = attachment.getWorld();

        ILootrBlockEntity be = resolveBlockEntity();
        if (be == null) return;

        // Refresh burst: poll every tick (cheap field read), broadcast on transition.
        if (config.refreshBurstEnabled) {
            ILootrInventoryStore store = resolveStore(be);
            if (store != null) {
                boolean refreshed = store.isRefreshed();
                if (refreshed && !lastRefreshState) {
                    ParticleOptions burst = config.refreshBurstParticle();
                    world.sendParticles(burst, false, false, emitX, emitY, emitZ,
                            config.refreshBurstParticleCount, 0.5, 0.5, 0.5, 0.05);
                }
                lastRefreshState = refreshed;
            }
        }

        // Unopened sparkles: per-player, throttled by interval.
        if (!config.unopenedParticlesEnabled) return;
        tickCounter++;
        if (tickCounter < config.unopenedParticleIntervalTicks) return;
        tickCounter = 0;
        if (getWatchingPlayers().isEmpty()) return;

        ParticleOptions particle = config.unopenedParticle();
        for (var handler : getWatchingPlayers()) {
            ServerPlayer player = handler.getPlayer();
            if (be.hasServerOpened(player)) continue;
            world.sendParticles(player, particle, false, false,
                    emitX, emitY, emitZ,
                    config.unopenedParticleCount, 0.15, 0.1, 0.15, 0.02);
        }
    }

    private @Nullable ILootrBlockEntity resolveBlockEntity() {
        if (cachedBlockEntity != null) return cachedBlockEntity;

        var attachment = getAttachment();
        if (attachment == null) return null;

        ServerLevel world = attachment.getWorld();
        BlockPos pos = BlockPos.containing(attachment.getPos());
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof ILootrBlockEntity lootrBE)) return null;

        cachedBlockEntity = lootrBE;
        if (!cacheReady) {
            var center = lootrBE.getParticleCenter();
            emitX = center.x;
            emitY = center.y + lootrBE.getParticleYOffset() + 0.5;
            emitZ = center.z;
            cacheReady = true;
        }
        return cachedBlockEntity;
    }

    private @Nullable ILootrInventoryStore resolveStore(ILootrBlockEntity be) {
        if (cachedStore != null) return cachedStore;
        cachedStore = LootrAPI.getData(be);
        return cachedStore;
    }
}
