package dev.ashkir.polylootr.overlay;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Walks {@link ContainerMappings#ALL} and registers every Polymer overlay
 * (block, item, block entity, virtual-entity renderer) the wrapper provides,
 * then delegates to {@link EntityMappings#registerAll()} for entities.
 *
 * <p>Called from {@code LootrInitMixin} at the tail of Lootr's own initializer
 * so all of Lootr's static fields and registry entries are guaranteed populated.
 */
public final class PolymerOverlayRegistrar {
    public static void registerAll() {
        for (ContainerMapping mapping : ContainerMappings.ALL) {
            PolymerBlockUtils.registerOverlay(
                    mapping.lootrBlock(),
                    (state, ctx) -> mapping.vanillaBlock().withPropertiesOf(state)
            );
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

    private PolymerOverlayRegistrar() {
    }
}
