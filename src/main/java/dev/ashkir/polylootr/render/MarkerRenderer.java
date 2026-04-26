package dev.ashkir.polylootr.render;

import dev.ashkir.polylootr.config.PolyLootrConfig;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Default marker overlay for Lootr containers (chests, barrels, shulkers, etc.).
 *
 * <p>Renders a single small floating item display above the container — by default
 * an amethyst shard — billboarded so it always faces the player. The element is
 * intentionally minimal: its purpose is to provide a static "this is a Lootr
 * container" signal to complement the particle emission driven by
 * {@link MarkerHolder}.
 *
 * <p>The displayed item and whether the marker shows at all are configurable via
 * {@link PolyLootrConfig}.
 *
 * <p>Singleton — registered once per Lootr block, reused across all instances.
 */
public final class MarkerRenderer implements BlockWithElementHolder {
    public static final MarkerRenderer INSTANCE = new MarkerRenderer();

    private static final float MARKER_SCALE = 0.35f;
    private static final double MARKER_Y_OFFSET = 0.85;

    private MarkerRenderer() {
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        MarkerHolder holder = new MarkerHolder();
        if (!PolyLootrConfig.get().markerEnabled) return holder;

        ItemDisplayElement marker = new ItemDisplayElement(new ItemStack(PolyLootrConfig.get().markerItem()));
        marker.setScale(new Vector3f(MARKER_SCALE, MARKER_SCALE, MARKER_SCALE));
        marker.setOffset(new Vec3(0, MARKER_Y_OFFSET, 0));
        marker.setBillboardMode(Display.BillboardConstraints.VERTICAL);
        holder.addElement(marker);
        return holder;
    }
}
