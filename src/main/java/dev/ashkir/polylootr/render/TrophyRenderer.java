package dev.ashkir.polylootr.render;

import dev.ashkir.polylootr.config.PolyLootrConfig;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Renders Lootr's trophy block as a floating item display sitting on the
 * blast-furnace stand-in (the trophy block is mapped to BLAST_FURNACE so its
 * HorizontalDirectional facing copies cleanly to clients).
 *
 * <p>Item type is configurable via {@link PolyLootrConfig#trophyDisplayItemId};
 * default is gold ingot.
 */
public final class TrophyRenderer implements BlockWithElementHolder {
    public static final TrophyRenderer INSTANCE = new TrophyRenderer();

    private static final float TROPHY_SCALE = 1.2f;
    private static final double TROPHY_Y_OFFSET = 0.15;

    private TrophyRenderer() {
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        ItemDisplayElement display = new ItemDisplayElement(new ItemStack(PolyLootrConfig.get().trophyDisplayItem()));
        display.setItemDisplayContext(ItemDisplayContext.GROUND);
        display.setScale(new Vector3f(TROPHY_SCALE, TROPHY_SCALE, TROPHY_SCALE));
        display.setOffset(new Vec3(0, TROPHY_Y_OFFSET, 0));
        display.setBillboardMode(Display.BillboardConstraints.VERTICAL);

        MarkerHolder holder = new MarkerHolder();
        holder.addElement(display);
        return holder;
    }
}
