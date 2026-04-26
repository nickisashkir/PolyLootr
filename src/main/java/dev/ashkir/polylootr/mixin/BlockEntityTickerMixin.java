package dev.ashkir.polylootr.mixin;

import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import noobanidus.mods.lootr.common.block.entity.BlockEntityTicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Datapack compatibility guard.
 *
 * <p>Lootr's conversion ticker treats every vanilla container as a candidate for
 * conversion. Datapacks like pk_waystones place vanilla barrels with explicit
 * items (no loot table) as custom-block GUIs; without this guard, Lootr's break
 * protection and conversion machinery break those datapack blocks.
 *
 * <p>Containers with no loot table aren't world-generated loot containers, so
 * skipping them here is safe. The check uses {@link RandomizableContainer#getLootTable()},
 * a simple field getter. Iterating {@code getItem(int)} would recurse into Lootr's own
 * {@code unpackLootTable} mixin and stack-overflow.
 */
@Mixin(BlockEntityTicker.class)
public class BlockEntityTickerMixin {
    @Inject(method = "isValidEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private static void polylootr$skipNoLootTable(BlockEntity blockEntity, CallbackInfoReturnable<Boolean> cir) {
        if (isDatapackContainer(blockEntity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isValidEntityFull", at = @At("HEAD"), cancellable = true, remap = false)
    private static void polylootr$skipNoLootTableFull(BlockEntity blockEntity, CallbackInfoReturnable<Boolean> cir) {
        if (isDatapackContainer(blockEntity)) {
            cir.setReturnValue(false);
        }
    }

    private static boolean isDatapackContainer(BlockEntity blockEntity) {
        return blockEntity instanceof RandomizableContainer container && container.getLootTable() == null;
    }
}
