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
 * <p>For chest and trapped-chest mappings, the registered lambda checks
 * {@link PolyLootrConfig#useCustomChestBlock} <b>at every block-translation
 * call</b> (not just once at registration time). This means
 * {@code /polylootr reload} flips chest rendering live: clients get the
 * polymer-block visual or the vanilla CHEST visual on the next chunk send
 * after the config change, without a server restart.
 */
public final class PolymerOverlayRegistrar {
    public static void registerAll() {
        // Polymer-block state reservation must happen before chunks ship to
        // clients. Idempotent — safe to call from both here and PolyLootr.onInitialize.
        LootrPolymerBlocks.register();

        for (ContainerMapping mapping : ContainerMappings.ALL) {
            registerBlockOverlay(mapping);

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
     * Registers a block overlay whose target is computed dynamically per-call.
     * For chest types we have a polymer-block alternative; the lambda checks
     * config and either reserved-state or falls back to vanilla CHEST. For
     * everything else, the lambda always returns the vanilla mapping.
     */
    private static void registerBlockOverlay(ContainerMapping mapping) {
        boolean isChestType = mapping.lootrBlock() == ModBlocks.CHEST
                || mapping.lootrBlock() == ModBlocks.TRAPPED_CHEST;

        if (!isChestType) {
            PolymerBlockUtils.registerOverlay(
                    mapping.lootrBlock(),
                    (state, ctx) -> mapping.vanillaBlock().withPropertiesOf(state)
            );
            return;
        }

        PolymerBlockUtils.registerOverlay(
                mapping.lootrBlock(),
                (state, ctx) -> {
                    if (PolyLootrConfig.get().useCustomChestBlock) {
                        BlockState polymerState = polymerBlockStateFor(mapping);
                        if (polymerState != null) return polymerState;
                    }
                    return mapping.vanillaBlock().withPropertiesOf(state);
                }
        );
    }

    private static BlockState polymerBlockStateFor(ContainerMapping mapping) {
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
