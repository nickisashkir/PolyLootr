package dev.ashkir.polylootr.render;

import dev.ashkir.polylootr.PolyLootr;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Builds a chest-shape ItemDisplayElement that overlays at block size on top
 * of a vanilla chest. Pairs with {@link PolymerOverlayRegistrar}'s vanilla
 * chest mapping to give clients Lootr's chest texture without relying on
 * polymer-blocks.
 *
 * <p>The model is parented from {@code polylootr:block/lootr_chest} (or the
 * trapped variant) — same geometry as the polymer-block route, just delivered
 * as an ItemDisplayElement so it doesn't depend on the block atlas pipeline
 * that polymer-blocks uses.
 *
 * <p>The element's left-rotation is set from the chest's {@link BlockStateProperties#HORIZONTAL_FACING}
 * so the lock face matches the block's facing. Default model orientation has
 * the lock on the -Z (NORTH) face, so facing-to-yaw mapping is:
 * NORTH→0°, WEST→90°, SOUTH→180°, EAST→270°.
 */
public final class ChestOverlay {
    private static final Identifier CHEST_OVERLAY_MODEL =
            Identifier.fromNamespaceAndPath(PolyLootr.ID, "lootr_chest_overlay");
    private static final Identifier TRAPPED_CHEST_OVERLAY_MODEL =
            Identifier.fromNamespaceAndPath(PolyLootr.ID, "lootr_trapped_chest_overlay");

    /**
     * Scale the overlay above 1.0 so its faces clear the vanilla chest entity
     * rendered behind it. Vanilla chests are 14/16 wide; our overlay model is
     * 16/16 (full block), so at scale 1.0 the outer faces sit at the block
     * boundary while vanilla's inner geometry sits just inside, producing
     * z-fighting flicker on the body sides. Pushing scale to 1.05 puts our
     * inner geometry clearly outside vanilla's, eliminating the overlap.
     */
    private static final float SCALE = 1.05f;

    private ChestOverlay() {
    }

    public static ItemDisplayElement create(String type, BlockState state) {
        Identifier model = "trapped_chest".equals(type)
                ? TRAPPED_CHEST_OVERLAY_MODEL
                : CHEST_OVERLAY_MODEL;

        ItemStack stack = new ItemStack(Items.STICK);
        stack.set(DataComponents.ITEM_MODEL, model);

        ItemDisplayElement element = new ItemDisplayElement(stack);
        element.setItemDisplayContext(ItemDisplayContext.FIXED);
        element.setScale(new Vector3f(SCALE, SCALE, SCALE));
        element.setOffset(new Vec3(0, 0, 0));
        element.setLeftRotation(rotationFor(state));
        return element;
    }

    private static Quaternionf rotationFor(BlockState state) {
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
        float yawDeg = switch (facing) {
            case WEST -> 90f;
            case SOUTH -> 180f;
            case EAST -> 270f;
            default -> 0f;
        };
        return new Quaternionf().rotationY((float) Math.toRadians(yawDeg));
    }
}
