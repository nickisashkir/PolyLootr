package dev.ashkir.polylootr.overlay;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.minecraft.world.entity.EntityType;
import noobanidus.mods.lootr.common.entity.LootrChestMinecartEntity;
import noobanidus.mods.lootr.common.entity.LootrItemFrame;
import noobanidus.mods.lootr.fabric.init.ModEntities;

/**
 * Polymer overlay registration for Lootr entity types.
 *
 * <p>Kept separate from {@link ContainerMappings} because entities don't share the
 * (block, item, block-entity, renderer) shape — they're standalone EntityTypes
 * each translated to a vanilla EntityType.
 */
public final class EntityMappings {
    @SuppressWarnings("unchecked")
    public static void registerAll() {
        PolymerEntityUtils.registerOverlay(
                (EntityType<LootrChestMinecartEntity>) (EntityType<?>) ModEntities.MINECART_WITH_CHEST,
                entity -> ctx -> EntityType.CHEST_MINECART
        );
        PolymerEntityUtils.registerOverlay(
                (EntityType<LootrItemFrame>) (EntityType<?>) ModEntities.ITEM_FRAME,
                entity -> ctx -> EntityType.ITEM_FRAME
        );
    }

    private EntityMappings() {
    }
}
