package dev.ashkir.polylootr.render;

import dev.ashkir.polylootr.config.PolyLootrConfig;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Shared element holder driving per-player particle emission for Lootr containers.
 *
 * <p>One holder per Lootr block instance. Caches the {@link ILootrBlockEntity}
 * reference and the particle emission origin on first lookup so subsequent ticks
 * skip the {@code getBlockEntity} call. Emission is gated by the configured
 * tick interval and skipped entirely when no players are watching the holder
 * or the {@code unopenedParticlesEnabled} config flag is off.
 *
 * <p>For each watching player who has not yet opened the container, a small
 * burst of the configured particle is sent using {@code ServerLevel.sendParticles(player, ...)}
 * so the visual signal is per-player.
 */
public class MarkerHolder extends ElementHolder {
    private int tickCounter = 0;
    private @Nullable ILootrBlockEntity cachedBlockEntity;
    private double emitX;
    private double emitY;
    private double emitZ;
    private boolean cacheReady = false;

    @Override
    protected void onTick() {
        PolyLootrConfig config = PolyLootrConfig.get();
        if (!config.unopenedParticlesEnabled) return;

        tickCounter++;
        if (tickCounter < config.unopenedParticleIntervalTicks) return;
        tickCounter = 0;

        if (getWatchingPlayers().isEmpty()) return;

        var attachment = getAttachment();
        if (attachment == null) return;
        ServerLevel world = attachment.getWorld();

        ILootrBlockEntity be = resolveBlockEntity();
        if (be == null) return;

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
}
