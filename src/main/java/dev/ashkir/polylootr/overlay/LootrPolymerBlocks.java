package dev.ashkir.polylootr.overlay;

import dev.ashkir.polylootr.PolyLootr;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

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
    public static @Nullable BlockState CHEST_STATE;
    public static @Nullable BlockState TRAPPED_CHEST_STATE;

    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;
        CHEST_STATE = requestBlock("block/lootr_chest");
        TRAPPED_CHEST_STATE = requestBlock("block/lootr_trapped_chest");
    }

    private static @Nullable BlockState requestBlock(String modelPath) {
        try {
            Identifier modelId = Identifier.fromNamespaceAndPath(PolyLootr.ID, modelPath);
            return PolymerBlockResourceUtils.requestBlock(
                    BlockModelType.FULL_BLOCK,
                    PolymerBlockModel.of(modelId)
            );
        } catch (Throwable t) {
            // Polymer Blocks state pool exhausted, or registration failed —
            // we fall back to vanilla block mapping in the registrar.
            return null;
        }
    }

    private LootrPolymerBlocks() {
    }
}
