package dev.ashkir.polylootr.overlay;

import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

/**
 * One Lootr container's complete mapping to its vanilla counterpart.
 *
 * <p>Bundles the four parallel registries the wrapper has to translate (block,
 * item, block entity, optional virtual-entity renderer) so callers have a single
 * source of truth per container type.
 */
public record ContainerMapping(
        Block lootrBlock,
        Block vanillaBlock,
        Item lootrItem,
        Item vanillaItem,
        @Nullable BlockEntityType<?> lootrBlockEntity,
        @Nullable BlockEntityType<?> vanillaBlockEntity,
        @Nullable BlockWithElementHolder renderer
) {
}
