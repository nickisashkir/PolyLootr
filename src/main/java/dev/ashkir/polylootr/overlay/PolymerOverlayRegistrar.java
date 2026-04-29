package dev.ashkir.polylootr.overlay;

import dev.ashkir.polylootr.config.PolyLootrConfig;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import noobanidus.mods.lootr.fabric.init.ModBlocks;

/**
 * Walks {@link ContainerMappings#ALL} and registers every Polymer overlay
 * (block, item, block entity, virtual-entity renderer) the wrapper provides,
 * then delegates to {@link EntityMappings#registerAll()} for entities.
 *
 * <p>For chest and trapped-chest mappings, if Lootr's polymer-block reservations
 * succeeded ({@link LootrPolymerBlocks#CHEST_STATE} / {@code TRAPPED_CHEST_STATE}
 * non-null) AND {@link PolyLootrConfig#useCustomChestBlock} is on, we route the
 * client-side overlay to the reserved Polymer Blocks state instead of vanilla
 * {@code Blocks.CHEST}. Vanilla clients render the chest-shape Lootr-textured
 * block model; Lootr-side mechanics are unaffected.
 */
public final class PolymerOverlayRegistrar {
    public static void registerAll() {
        for (ContainerMapping mapping : ContainerMappings.ALL) {
            BlockState polymerBlockState = polymerBlockStateFor(mapping);
            if (polymerBlockState != null) {
                PolymerBlockUtils.registerOverlay(
                        mapping.lootrBlock(),
                        (state, ctx) -> polymerBlockState
                );
            } else {
                PolymerBlockUtils.registerOverlay(
                        mapping.lootrBlock(),
                        (state, ctx) -> mapping.vanillaBlock().withPropertiesOf(state)
                );
            }

            PolymerItemUtils.registerOverlay(
                    mapping.lootrItem(),
                    (stack, ctx) -> mapping.vanillaItem()
            );
            if (mapping.lootrBlockEntity() != null && mapping.vanillaBlockEntity() != null) {
                BlockEntityType<?> vanilla = mapping.vanillaBlockEntity();
                PolymerBlockUtils.registerBlockEntity(
                        mapping.lootrBlockEntity(),
                        (be, ctx) -> vanilla
                );
            }
            if (mapping.renderer() != null) {
                BlockWithElementHolder.registerOverlay(mapping.lootrBlock(), mapping.renderer());
            }
        }
        EntityMappings.registerAll();
    }

    /**
     * Returns the polymer block state to use for the client-side overlay, or
     * null to fall back to the standard vanilla block mapping.
     */
    private static BlockState polymerBlockStateFor(ContainerMapping mapping) {
        if (!PolyLootrConfig.get().useCustomChestBlock) return null;
        if (mapping.lootrBlock() == ModBlocks.CHEST) {
            return LootrPolymerBlocks.CHEST_STATE;
        }
        if (mapping.lootrBlock() == ModBlocks.TRAPPED_CHEST) {
            return LootrPolymerBlocks.TRAPPED_CHEST_STATE;
        }
        return null;
    }

    private PolymerOverlayRegistrar() {
    }
}
