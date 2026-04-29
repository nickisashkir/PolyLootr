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
import noobanidus.mods.lootr.common.api.data.ILootrContainerInstance;
import noobanidus.mods.lootr.common.api.data.ILootrInventoryStore;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Element holder for one Lootr container's marker, particles, and visibility
 * logic. Works for both block-attached holders (default constructor) and
 * entity-attached holders (constructor taking the {@link ILootrContainerInstance}
 * directly — used for chest minecarts and item frames).
 *
 * <p>Position-related state ({@code emitX/Y/Z}) is queried fresh each tick from
 * {@link ILootrContainerInstance#getParticleCenter()} rather than cached, so
 * markers attached to moving entities still emit particles at the entity's
 * current position.
 *
 * <h2>Per-player visibility (hide-after-open, refresh-aware)</h2>
 * Marker hidden iff player has opened the container AND the container is not
 * in its post-refresh window. Both {@link #startWatching} and onTick prune
 * apply the check.
 *
 * <h2>Refresh burst + re-watch</h2>
 * On {@link ILootrInventoryStore#isRefreshed()} false→true transition, broadcasts
 * a particle burst and calls startWatching for online players within 64 blocks.
 */
public class MarkerHolder extends ElementHolder {
    private static final double REFRESH_REWATCH_RADIUS_SQ = 64 * 64;

    private int tickCounter = 0;
    private @Nullable ILootrContainerInstance cachedInstance;
    private @Nullable ILootrInventoryStore cachedStore;
    private boolean lastRefreshState = false;

    /** Default constructor — block-source mode; resolves via world.getBlockEntity. */
    public MarkerHolder() {
    }

    /** Entity-source constructor — caches the instance directly (cast from the entity). */
    public MarkerHolder(ILootrContainerInstance entityInstance) {
        this.cachedInstance = entityInstance;
    }

    @Override
    public boolean startWatching(ServerGamePacketListenerImpl handler) {
        if (PolyLootrConfig.get().markerHideAfterOpen) {
            ILootrContainerInstance instance = resolveInstance();
            if (instance != null && shouldHideForPlayer(instance, handler.getPlayer())) {
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

        ILootrContainerInstance instance = resolveInstance();
        if (instance == null) return;

        var center = instance.getParticleCenter();
        double emitX = center.x;
        double emitY = center.y + instance.getParticleYOffset() + 0.5;
        double emitZ = center.z;

        // Refresh transition — burst + force re-watch nearby players whose marker should now show
        if (config.refreshBurstEnabled || config.markerHideAfterOpen) {
            ILootrInventoryStore store = resolveStore(instance);
            if (store != null) {
                boolean refreshed = store.isRefreshed();
                if (refreshed && !lastRefreshState) {
                    if (config.refreshBurstEnabled) {
                        ParticleOptions burst = config.refreshBurstParticle();
                        world.sendParticles(burst, false, false, emitX, emitY, emitZ,
                                config.refreshBurstParticleCount, 0.5, 0.5, 0.5, 0.05);
                    }
                    if (config.markerHideAfterOpen) {
                        rewatchNearbyPlayers(world, emitX, emitY, emitZ);
                    }
                }
                lastRefreshState = refreshed;
            }
        }

        // Hide marker for watching players whose visibility now says hide
        if (config.markerHideAfterOpen) {
            List<ServerGamePacketListenerImpl> toRemove = null;
            for (var handler : getWatchingPlayers()) {
                if (shouldHideForPlayer(instance, handler.getPlayer())) {
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

        if (!config.unopenedParticlesEnabled) return;
        tickCounter++;
        if (tickCounter < config.unopenedParticleIntervalTicks) return;
        tickCounter = 0;
        if (getWatchingPlayers().isEmpty()) return;

        ParticleOptions particle = config.unopenedParticle();
        for (var handler : getWatchingPlayers()) {
            ServerPlayer player = handler.getPlayer();
            if (instance.hasServerOpened(player)) continue;
            world.sendParticles(player, particle, false, false,
                    emitX, emitY, emitZ,
                    config.unopenedParticleCount, 0.15, 0.1, 0.15, 0.02);
        }
    }

    private boolean shouldHideForPlayer(ILootrContainerInstance instance, ServerPlayer player) {
        if (!instance.hasServerOpened(player)) return false;
        ILootrInventoryStore store = resolveStore(instance);
        if (store != null && store.isRefreshed()) return false;
        return true;
    }

    private void rewatchNearbyPlayers(ServerLevel world, double x, double y, double z) {
        for (ServerPlayer player : world.players()) {
            double dx = player.getX() - x;
            double dy = player.getY() - y;
            double dz = player.getZ() - z;
            if (dx * dx + dy * dy + dz * dz > REFRESH_REWATCH_RADIUS_SQ) continue;
            startWatching(player);
        }
    }

    /**
     * Resolves the container instance — for entity sources, the instance is
     * already cached at construction. For block sources, queries the block
     * entity at the holder's attachment position once and caches that.
     */
    private @Nullable ILootrContainerInstance resolveInstance() {
        if (cachedInstance != null) return cachedInstance;

        var attachment = getAttachment();
        if (attachment == null) return null;

        ServerLevel world = attachment.getWorld();
        BlockPos pos = BlockPos.containing(attachment.getPos());
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof ILootrContainerInstance instance) {
            cachedInstance = instance;
            return instance;
        }
        return null;
    }

    private @Nullable ILootrInventoryStore resolveStore(ILootrContainerInstance instance) {
        if (cachedStore != null) return cachedStore;
        cachedStore = LootrAPI.getData(instance);
        return cachedStore;
    }
}
