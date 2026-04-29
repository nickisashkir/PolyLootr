package dev.ashkir.polylootr.render;

import dev.ashkir.polylootr.config.PolyLootrConfig;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Block-attached marker overlay for Lootr containers. Delegates the actual
 * visual element construction to {@link MarkerVisuals} so the entity-attached
 * path (chest minecarts) shares the same logic.
 */
public final class MarkerRenderer implements BlockWithElementHolder {
    private static final double BLOCK_MARKER_Y_OFFSET = 0.85;

    private final String type;
    private final Item defaultMarkerItem;

    public MarkerRenderer(String type, Item defaultMarkerItem) {
        this.type = type;
        this.defaultMarkerItem = defaultMarkerItem;
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        MarkerHolder holder = new MarkerHolder();
        PolyLootrConfig config = PolyLootrConfig.get();

        if (config.useChestOverlay && isChestType()) {
            holder.addElement(ChestOverlay.create(type, initialBlockState));
        }

        if (config.markerEnabled) {
            for (var element : MarkerVisuals.build(type, defaultMarkerItem, BLOCK_MARKER_Y_OFFSET)) {
                holder.addElement(element);
            }
        }
        return holder;
    }

    private boolean isChestType() {
        return "chest".equals(type) || "trapped_chest".equals(type);
    }
}
