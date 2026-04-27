package dev.ashkir.polylootr.render;

import dev.ashkir.polylootr.config.PolyLootrConfig;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Marker overlay for Lootr containers (chests, barrels, shulkers, etc.).
 *
 * <p>Renders a single small floating item display above the container, billboarded
 * so it always faces the player. Each container type has its own default item
 * (configured at {@link dev.ashkir.polylootr.overlay.ContainerMappings}); the user
 * can globally override via {@link PolyLootrConfig#markerItemId}.
 *
 * <p>Not a singleton — one instance per (block, default-marker-item) pair so each
 * container type carries its own thematic indicator.
 */
public final class MarkerRenderer implements BlockWithElementHolder {
    private static final float MARKER_SCALE = 0.35f;
    private static final double MARKER_Y_OFFSET = 0.85;

    private final Item defaultMarkerItem;

    public MarkerRenderer(Item defaultMarkerItem) {
        this.defaultMarkerItem = defaultMarkerItem;
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        MarkerHolder holder = new MarkerHolder();
        if (!PolyLootrConfig.get().markerEnabled) return holder;

        Item override = PolyLootrConfig.get().markerItemOverride();
        Item item = override != null ? override : defaultMarkerItem;

        ItemDisplayElement marker = new ItemDisplayElement(new ItemStack(item));
        marker.setScale(new Vector3f(MARKER_SCALE, MARKER_SCALE, MARKER_SCALE));
        marker.setOffset(new Vec3(0, MARKER_Y_OFFSET, 0));
        marker.setBillboardMode(Display.BillboardConstraints.VERTICAL);
        holder.addElement(marker);
        return holder;
    }
}
