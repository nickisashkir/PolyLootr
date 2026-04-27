package dev.ashkir.polylootr.render;

import dev.ashkir.polylootr.config.PolyLootrConfig;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.entity.BlockEntity;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.ILootrInventoryStore;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared element holder driving particle effects and per-player visibility for
 * one Lootr container.
 *
 * <p>Caches the {@link ILootrBlockEntity}, particle emission origin, and
 * (lazily) the {@link ILootrInventoryStore} so subsequent ticks skip lookups.
 *
 * <h2>Per-player visibility (hide after open)</h2>
 * When {@link PolyLootrConfig#markerHideAfterOpen} is on (default), the marker
 * disappears for any player who has personally opened the container:
 * <ul>
 *   <li>{@link #startWatching(ServerGamePacketListenerImpl)} is overridden so
 *       a player who already opened never starts seeing the marker when they
 *       enter view distance.</li>
 *   <li>Each tick prunes existing watchers who have flipped to
 *       {@code hasServerOpened == true} since they started watching.</li>
 * </ul>
 *
 * <h2>Per-player unopened sparkles</h2>
 * For each watching player who has not yet opened, a configurable particle
 * burst is sent via {@code ServerLevel.sendParticles(player, ...)}. Frequency
 * is gated by {@link PolyLootrConfig#unopenedParticleIntervalTicks}.
 *
 * <h2>Refresh burst</h2>
 * When {@link ILootrInventoryStore#isRefreshed()} flips from {@code false} to
 * {@code true} (refresh timer fired), broadcasts a one-shot particle burst.
 * Detected by polling each tick and tracking {@link #lastRefreshState}.
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
    public boolean startWatching(ServerGamePacketListenerImpl handler) {
        if (PolyLootrConfig.get().markerHideAfterOpen) {
            ILootrBlockEntity be = resolveBlockEntity();
            if (be != null && be.hasServerOpened(handler.getPlayer())) {
                return false;
            }
        }
        return super.startWatching(handler);
    }

    @Override
    protected void onTick() {
        PolyLootrConfig config = PolyLootrConfig.get();

        var attachment = getAttachment();
        if (attachment == null) return;
        ServerLevel world = attachment.getWorld();

        ILootrBlockEntity be = resolveBlockEntity();
        if (be == null) return;

        // Hide marker for players who've opened the chest since they started watching.
        if (config.markerHideAfterOpen) {
            List<ServerGamePacketListenerImpl> toRemove = null;
            for (var handler : getWatchingPlayers()) {
                if (be.hasServerOpened(handler.getPlayer())) {
                    if (toRemove == null) toRemove = new ArrayList<>();
                    toRemove.add(handler);
                }
            }
            if (toRemove != null) {
                for (var handler : toRemove) {
                    stopWatching(handler);
                }
            }
        }

        // Refresh burst: poll every tick, broadcast on transition.
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
