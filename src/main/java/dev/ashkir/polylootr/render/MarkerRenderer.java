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
 * <p>Each instance represents one Lootr container type. At render time it asks
 * {@link PolyLootrConfig#markerItemFor(String, Item)} for the item to display,
 * which applies overrides in this order: global override &gt; per-type config &gt;
 * the {@code defaultMarkerItem} the renderer was constructed with.
 */
public final class MarkerRenderer implements BlockWithElementHolder {
    private static final float MARKER_SCALE = 0.35f;
    private static final double MARKER_Y_OFFSET = 0.85;

    private final String type;
    private final Item defaultMarkerItem;

    /**
     * @param type the container-type key used to look up
     *             {@link PolyLootrConfig#markerItems} (e.g. {@code "chest"},
     *             {@code "barrel"})
     * @param defaultMarkerItem fallback used when the user has neither set a
     *                          global override nor a per-type entry
     */
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
        if (!PolyLootrConfig.get().markerEnabled) return holder;

        Item item = PolyLootrConfig.get().markerItemFor(type, defaultMarkerItem);
        ItemDisplayElement marker = new ItemDisplayElement(new ItemStack(item));
        marker.setScale(new Vector3f(MARKER_SCALE, MARKER_SCALE, MARKER_SCALE));
        marker.setOffset(new Vec3(0, MARKER_Y_OFFSET, 0));
        marker.setBillboardMode(Display.BillboardConstraints.VERTICAL);
        if (PolyLootrConfig.get().markerGlowingEnabled) {
            marker.setGlowing(true);
            int color = PolyLootrConfig.get().markerGlowColor;
            if (color >= 0) {
                marker.setGlowColorOverride(color);
            }
        }
        holder.addElement(marker);
        return holder;
    }
}
