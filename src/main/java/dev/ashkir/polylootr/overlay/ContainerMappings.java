package dev.ashkir.polylootr.overlay;

import dev.ashkir.polylootr.render.MarkerRenderer;
import dev.ashkir.polylootr.render.TrophyRenderer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import noobanidus.mods.lootr.fabric.init.ModBlockEntities;
import noobanidus.mods.lootr.fabric.init.ModBlocks;
import noobanidus.mods.lootr.fabric.init.ModItems;

import java.util.List;

/**
 * Single source of truth for every Lootr container the wrapper translates.
 *
 * <p>Each {@link MarkerRenderer} carries a type key (matched against
 * {@link dev.ashkir.polylootr.config.PolyLootrConfig#markerItems}) and a
 * compile-time default item used when neither the global override nor the
 * per-type config entry resolves.
 */
public final class ContainerMappings {
    public static final List<ContainerMapping> ALL = List.of(
            new ContainerMapping(
                    ModBlocks.CHEST, Blocks.CHEST,
                    ModItems.CHEST, Items.CHEST,
                    ModBlockEntities.CHEST, BlockEntityType.CHEST,
                    new MarkerRenderer("chest", Items.AMETHYST_SHARD)
            ),
            new ContainerMapping(
                    ModBlocks.TRAPPED_CHEST, Blocks.TRAPPED_CHEST,
                    ModItems.TRAPPED_CHEST, Items.TRAPPED_CHEST,
                    ModBlockEntities.TRAPPED_CHEST, BlockEntityType.TRAPPED_CHEST,
                    new MarkerRenderer("trapped_chest", Items.REDSTONE)
            ),
            new ContainerMapping(
                    ModBlocks.BARREL, Blocks.BARREL,
                    ModItems.BARREL, Items.BARREL,
                    ModBlockEntities.BARREL, BlockEntityType.BARREL,
                    new MarkerRenderer("barrel", Items.WHEAT)
            ),
            new ContainerMapping(
                    ModBlocks.SHULKER_BOX, Blocks.SHULKER_BOX,
                    ModItems.SHULKER_BOX, Items.SHULKER_BOX,
                    ModBlockEntities.SHULKER_BOX, BlockEntityType.SHULKER_BOX,
                    new MarkerRenderer("shulker_box", Items.ENDER_PEARL)
            ),
            new ContainerMapping(
                    ModBlocks.SUSPICIOUS_SAND, Blocks.SUSPICIOUS_SAND,
                    ModItems.SUSPICIOUS_SAND, Items.SUSPICIOUS_SAND,
                    ModBlockEntities.BRUSHABLE_BLOCK, BlockEntityType.BRUSHABLE_BLOCK,
                    new MarkerRenderer("suspicious_sand", Items.BRUSH)
            ),
            new ContainerMapping(
                    ModBlocks.SUSPICIOUS_GRAVEL, Blocks.SUSPICIOUS_GRAVEL,
                    ModItems.SUSPICIOUS_GRAVEL, Items.SUSPICIOUS_GRAVEL,
                    null, null,
                    new MarkerRenderer("suspicious_gravel", Items.BRUSH)
            ),
            new ContainerMapping(
                    ModBlocks.DECORATED_POT, Blocks.DECORATED_POT,
                    ModItems.DECORATED_POT, Items.DECORATED_POT,
                    ModBlockEntities.DECORATED_POT, BlockEntityType.DECORATED_POT,
                    new MarkerRenderer("decorated_pot", Items.BRUSH)
            ),
            // Trophy is HorizontalDirectional; BLAST_FURNACE matches that property
            // layout so withPropertiesOf actually copies facing.
            new ContainerMapping(
                    ModBlocks.TROPHY, Blocks.BLAST_FURNACE,
                    ModItems.TROPHY, Items.PLAYER_HEAD,
                    null, null,
                    TrophyRenderer.INSTANCE
            )
    );

    private ContainerMappings() {
    }
}
