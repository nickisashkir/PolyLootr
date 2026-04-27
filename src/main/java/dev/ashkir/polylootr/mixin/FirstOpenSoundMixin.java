package dev.ashkir.polylootr.mixin;

import dev.ashkir.polylootr.config.PolyLootrConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import noobanidus.mods.lootr.common.api.data.ILootrContainerInstance;
import noobanidus.mods.lootr.common.api.interfaces.container.IMenuBuilder;
import noobanidus.mods.lootr.common.api.interfaces.lootr.ILootrAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Plays a configurable sound to a player the first time they open a Lootr
 * container.
 *
 * <p>Hooks the {@code ServerPlayer#awardStat(Stat)} call inside
 * {@link ILootrAPI#handleInstanceOpen}, which Lootr only invokes when the
 * per-player "has opened" check returns false. Injecting at HEAD of that call
 * site is exactly "first open per player" without re-implementing the check
 * ourselves.
 */
@Mixin(ILootrAPI.class)
public interface FirstOpenSoundMixin {
    @Inject(
            method = "handleInstanceOpen(Lnoobanidus/mods/lootr/common/api/data/ILootrContainerInstance;Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/common/api/interfaces/container/IMenuBuilder;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;awardStat(Lnet/minecraft/stats/Stat;)V"
            ),
            remap = false
    )
    private static void polylootr$firstOpenSound(
            ILootrContainerInstance instance, ServerPlayer player, IMenuBuilder menuBuilder,
            CallbackInfo ci) {
        PolyLootrConfig config = PolyLootrConfig.get();
        if (!config.firstOpenSoundEnabled) return;

        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                net.minecraft.core.Holder.direct(config.firstOpenSound()),
                SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(),
                config.firstOpenSoundVolume,
                config.firstOpenSoundPitch,
                player.getRandom().nextLong()
        ));
    }
}
