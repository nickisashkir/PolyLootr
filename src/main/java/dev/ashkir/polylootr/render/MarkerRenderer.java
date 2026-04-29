package dev.ashkir.polylootr.render;

import dev.ashkir.polylootr.PolyLootr;
import dev.ashkir.polylootr.config.PolyLootrConfig;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Marker overlay for Lootr containers (chests, barrels, shulkers, etc.).
 *
 * <p>Two visual modes, controlled by {@link PolyLootrConfig#useLootrTextures}:
 *
 * <ul>
 *   <li><b>Vanilla item mode</b> (default) — renders a vanilla item via
 *       {@link PolyLootrConfig#markerItemFor(String, Item)} (priority: global
 *       override &gt; per-type config &gt; compile-time default).</li>
 *   <li><b>Lootr texture mode</b> — renders a custom-textured cube using the
 *       {@code gold_planks} or {@code silver_planks} block textures bundled
 *       in PolyLootr's server-side resource pack. Players must accept the
 *       server pack on join for the textures to appear; without it, vanilla
 *       clients fall back to whatever item the {@code item_model} component
 *       points at on the underlying ItemStack.</li>
 * </ul>
 */
public final class MarkerRenderer implements BlockWithElementHolder {
    private static final float MARKER_SCALE = 0.35f;
    private static final double MARKER_Y_OFFSET = 0.85;
    private static final float LOOTR_MARKER_SCALE = 0.25f;

    private static final Identifier LOOTR_MARKER_GOLD = Identifier.fromNamespaceAndPath(PolyLootr.ID, "marker_gold_planks");
    private static final Identifier LOOTR_MARKER_SILVER = Identifier.fromNamespaceAndPath(PolyLootr.ID, "marker_silver_planks");

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
        if (!config.markerEnabled) return holder;

        ItemDisplayElement marker;
        float scale;
        if (config.useLootrTextures) {
            marker = createLootrTexturedMarker(type);
            scale = LOOTR_MARKER_SCALE;
        } else {
            Item item = config.markerItemFor(type, defaultMarkerItem);
            marker = new ItemDisplayElement(new ItemStack(item));
            scale = MARKER_SCALE;
        }

        marker.setScale(new Vector3f(scale, scale, scale));
        marker.setOffset(new Vec3(0, MARKER_Y_OFFSET, 0));
        marker.setBillboardMode(Display.BillboardConstraints.VERTICAL);
        if (config.markerGlowingEnabled) {
            marker.setGlowing(true);
            int color = config.markerGlowColor;
            if (color >= 0) {
                marker.setGlowColorOverride(color);
            }
        }
        holder.addElement(marker);
        return holder;
    }

    /**
     * Builds an ItemDisplayElement that renders one of PolyLootr's bundled
     * Lootr-style textures. Uses the {@code minecraft:item_model} component to
     * point the ItemStack at our custom item model definition shipped via
     * Polymer Resource Pack ({@code assets/polylootr/items/marker_*.json}).
     *
     * <p>Trapped chests get the silver_planks variant; everything else gets
     * gold_planks. Refine per-type if we add more bundled textures later.
     */
    private static ItemDisplayElement createLootrTexturedMarker(String type) {
        Identifier model = "trapped_chest".equals(type) ? LOOTR_MARKER_SILVER : LOOTR_MARKER_GOLD;
        ItemStack stack = new ItemStack(Items.STICK);
        stack.set(DataComponents.ITEM_MODEL, model);
        return new ItemDisplayElement(stack);
    }
}
