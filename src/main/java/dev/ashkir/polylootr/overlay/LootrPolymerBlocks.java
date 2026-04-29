package dev.ashkir.polylootr.overlay;

import dev.ashkir.polylootr.PolyLootr;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers custom-textured Lootr-style block states via Polymer Blocks.
 *
 * <p>Polymer Blocks reserves vanilla block states (typically from the
 * {@code note_block} state pool for {@link BlockModelType#FULL_BLOCK}) and
 * pairs each with a custom block model shipped in our resource pack. Vanilla
 * clients render the reserved state with our model — giving us per-block
 * custom appearance without affecting unrelated blocks (vanilla chests stay
 * vanilla).
 *
 * <p>Lootr chests get a chest-shape model textured with Lootr's signature
 * {@code gold_planks} (silver for trapped variant). The chest is mechanically
 * still a Lootr chest server-side; only the client-facing block state is
 * swapped for ours.
 */
public final class LootrPolymerBlocks {
    private static final Logger LOG = LoggerFactory.getLogger("polylootr/blocks");

    public static @Nullable BlockState CHEST_STATE;
    public static @Nullable BlockState TRAPPED_CHEST_STATE;

    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;
        PolymerResourcePackUtils.addModAssets(PolyLootr.ID);
        PolymerResourcePackUtils.markAsRequired();

        int beforeFullBlock = PolymerBlockResourceUtils.getBlocksLeft(BlockModelType.FULL_BLOCK);
        LOG.info("polymer-blocks FULL_BLOCK pool size before chest reservations: {}", beforeFullBlock);

        CHEST_STATE = requestBlock("block/lootr_chest");
        TRAPPED_CHEST_STATE = requestBlock("block/lootr_trapped_chest");

        LOG.info("CHEST_STATE = {}", CHEST_STATE);
        LOG.info("TRAPPED_CHEST_STATE = {}", TRAPPED_CHEST_STATE);
        LOG.info("polymer-blocks FULL_BLOCK pool size after: {}",
                PolymerBlockResourceUtils.getBlocksLeft(BlockModelType.FULL_BLOCK));
    }

    private static @Nullable BlockState requestBlock(String modelPath) {
        try {
            Identifier modelId = Identifier.fromNamespaceAndPath(PolyLootr.ID, modelPath);
            BlockState result = PolymerBlockResourceUtils.requestBlock(
                    BlockModelType.FULL_BLOCK,
                    PolymerBlockModel.of(modelId)
            );
            LOG.info("Reserved polymer block state for {}: {}", modelId, result);
            return result;
        } catch (Throwable t) {
            LOG.warn("polymer-blocks requestBlock failed for {}: {}", modelPath, t.toString());
            return null;
        }
    }

    private LootrPolymerBlocks() {
    }
}
