package dev.ashkir.polylootr.mixin;

import dev.ashkir.polylootr.config.PolyLootrConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import noobanidus.mods.lootr.fabric.event.HandleBreak;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Two responsibilities on Lootr's break handler:
 *
 * <ol>
 *   <li>Datapack guard: containers with no loot table (waystones, datapack
 *       custom blocks) bypass Lootr's break protection and break normally.</li>
 *   <li>Break-effect particles: spawn the configured particle cloud at the
 *       broken block's position. Lootr's native break particles run client-side
 *       only and aren't visible to vanilla clients.</li>
 * </ol>
 */
@Mixin(HandleBreak.class)
public class HandleBreakMixin {
    @Inject(method = "beforeBlockBreak", at = @At("HEAD"), cancellable = true, remap = false)
    private static void polylootr$allowDatapackContainerBreak(
            Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity,
            CallbackInfoReturnable<Boolean> cir) {
        if (blockEntity instanceof RandomizableContainer container && container.getLootTable() == null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "afterBlockBreak", at = @At("TAIL"), remap = false)
    private static void polylootr$breakEffect(
            Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity,
            CallbackInfo ci) {
        PolyLootrConfig config = PolyLootrConfig.get();
        if (!config.breakEffectEnabled) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (blockEntity instanceof RandomizableContainer container && container.getLootTable() == null) return;

        serverLevel.sendParticles(config.breakEffectParticle(), false, false,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                config.breakEffectParticleCount,
                0.25, 0.25, 0.25, 0.0);
    }
}
