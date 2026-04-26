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
 * <p>Adding a new entry here is sufficient to register all four overlays for it
 * (block, item, block entity, virtual-entity marker). Order doesn't matter.
 *
 * <p>Trophy uses {@link TrophyRenderer} (a floating display item); every other
 * container uses {@link MarkerRenderer} (a small floating loot indicator).
 */
public final class ContainerMappings {
    public static final List<ContainerMapping> ALL = List.of(
            new ContainerMapping(
                    ModBlocks.CHEST, Blocks.CHEST,
                    ModItems.CHEST, Items.CHEST,
                    ModBlockEntities.CHEST, BlockEntityType.CHEST,
                    MarkerRenderer.INSTANCE
            ),
            new ContainerMapping(
                    ModBlocks.TRAPPED_CHEST, Blocks.TRAPPED_CHEST,
                    ModItems.TRAPPED_CHEST, Items.TRAPPED_CHEST,
                    ModBlockEntities.TRAPPED_CHEST, BlockEntityType.TRAPPED_CHEST,
                    MarkerRenderer.INSTANCE
            ),
            new ContainerMapping(
                    ModBlocks.BARREL, Blocks.BARREL,
                    ModItems.BARREL, Items.BARREL,
                    ModBlockEntities.BARREL, BlockEntityType.BARREL,
                    MarkerRenderer.INSTANCE
            ),
            new ContainerMapping(
                    ModBlocks.SHULKER_BOX, Blocks.SHULKER_BOX,
                    ModItems.SHULKER_BOX, Items.SHULKER_BOX,
                    ModBlockEntities.SHULKER_BOX, BlockEntityType.SHULKER_BOX,
                    MarkerRenderer.INSTANCE
            ),
            new ContainerMapping(
                    ModBlocks.SUSPICIOUS_SAND, Blocks.SUSPICIOUS_SAND,
                    ModItems.SUSPICIOUS_SAND, Items.SUSPICIOUS_SAND,
                    ModBlockEntities.BRUSHABLE_BLOCK, BlockEntityType.BRUSHABLE_BLOCK,
                    MarkerRenderer.INSTANCE
            ),
            new ContainerMapping(
                    ModBlocks.SUSPICIOUS_GRAVEL, Blocks.SUSPICIOUS_GRAVEL,
                    ModItems.SUSPICIOUS_GRAVEL, Items.SUSPICIOUS_GRAVEL,
                    null, null,
                    MarkerRenderer.INSTANCE
            ),
            new ContainerMapping(
                    ModBlocks.DECORATED_POT, Blocks.DECORATED_POT,
                    ModItems.DECORATED_POT, Items.DECORATED_POT,
                    ModBlockEntities.DECORATED_POT, BlockEntityType.DECORATED_POT,
                    MarkerRenderer.INSTANCE
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
