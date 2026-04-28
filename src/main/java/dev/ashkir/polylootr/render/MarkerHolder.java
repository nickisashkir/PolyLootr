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
 * <h2>Per-player visibility (hide after open, refresh-aware)</h2>
 * When {@link PolyLootrConfig#markerHideAfterOpen} is on (default), the marker
 * disappears for any player who has personally opened the container — UNLESS
 * the container's loot has just refreshed, in which case the marker reappears
 * for everyone (including previous openers) so nobody misses fresh loot. The
 * "refresh window" lasts until any player opens the container post-refresh,
 * which clears Lootr's {@code isRefreshed} flag via {@code performRefresh()}.
 *
 * <p>{@link #startWatching(ServerGamePacketListenerImpl)} rejects players whose
 * marker would be hidden so they don't see/load it; {@link #onTick()} prunes
 * watchers who have flipped to opened since they started.
 *
 * <h2>Refresh burst + re-watch</h2>
 * On the false→true transition of {@link ILootrInventoryStore#isRefreshed()},
 * broadcasts a particle burst AND iterates online players in range, calling
 * {@code startWatching} for any whose marker should now be visible (because
 * the refresh-aware visibility check now lets them through).
 *
 * <h2>Per-player unopened sparkles</h2>
 * For each watching player who has not yet opened, a configurable particle
 * burst is sent via {@code ServerLevel.sendParticles(player, ...)}. Frequency
 * is gated by {@link PolyLootrConfig#unopenedParticleIntervalTicks}.
 */
public class MarkerHolder extends ElementHolder {
    private static final double REFRESH_REWATCH_RADIUS_SQ = 64 * 64;

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
            if (be != null && shouldHideForPlayer(be, handler.getPlayer())) {
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

        // Refresh detection (must run before visibility prune so the prune sees the new state)
        if (config.refreshBurstEnabled || config.markerHideAfterOpen) {
            ILootrInventoryStore store = resolveStore(be);
            if (store != null) {
                boolean refreshed = store.isRefreshed();
                if (refreshed && !lastRefreshState) {
                    if (config.refreshBurstEnabled) {
                        ParticleOptions burst = config.refreshBurstParticle();
                        world.sendParticles(burst, false, false, emitX, emitY, emitZ,
                                config.refreshBurstParticleCount, 0.5, 0.5, 0.5, 0.05);
                    }
                    if (config.markerHideAfterOpen) {
                        rewatchNearbyPlayers(world);
                    }
                }
                lastRefreshState = refreshed;
            }
        }

        // Hide marker for players whose visibility check now says hide
        if (config.markerHideAfterOpen) {
            List<ServerGamePacketListenerImpl> toRemove = null;
            for (var handler : getWatchingPlayers()) {
                if (shouldHideForPlayer(be, handler.getPlayer())) {
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

        // Unopened sparkles: per-player, throttled by interval
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

    /**
     * Returns true if the marker should be hidden from the given player right
     * now. Hidden iff the player has opened the container AND the container is
     * NOT currently in its refreshed-but-not-yet-consumed window.
     */
    private boolean shouldHideForPlayer(ILootrBlockEntity be, ServerPlayer player) {
        if (!be.hasServerOpened(player)) return false;
        ILootrInventoryStore store = resolveStore(be);
        if (store != null && store.isRefreshed()) return false;
        return true;
    }

    /**
     * On a refresh transition, walk online players within the rewatch radius and
     * try to start watching them. {@code startWatching} is idempotent (returns
     * false if already watching) and respects the visibility check, so this is
     * safe to call broadly.
     */
    private void rewatchNearbyPlayers(ServerLevel world) {
        for (ServerPlayer player : world.players()) {
            double dx = player.getX() - emitX;
            double dy = player.getY() - emitY;
            double dz = player.getZ() - emitZ;
            if (dx * dx + dy * dy + dz * dz > REFRESH_REWATCH_RADIUS_SQ) continue;
            startWatching(player);
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
