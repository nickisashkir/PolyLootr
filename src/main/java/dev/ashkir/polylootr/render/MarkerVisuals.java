package dev.ashkir.polylootr.render;

import dev.ashkir.polylootr.PolyLootr;
import dev.ashkir.polylootr.config.PolyLootrConfig;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the ItemDisplayElements that make up a Lootr-container marker —
 * shared between block-attached holders ({@link MarkerRenderer}) and
 * entity-attached holders (chest minecarts, item frames).
 *
 * <p>Returns a list because two visual modes can stack: the Lootr-textured
 * cube (when {@link PolyLootrConfig#useLootrTextures} is on) and the vanilla
 * item marker (when {@link PolyLootrConfig#showVanillaItemMarker} is on, or
 * when Lootr textures are off entirely).
 */
public final class MarkerVisuals {
    private static final float VANILLA_SCALE = 0.35f;
    private static final float LOOTR_SCALE = 0.25f;

    private static final Identifier LOOTR_MARKER_GOLD =
            Identifier.fromNamespaceAndPath(PolyLootr.ID, "marker_gold_planks");
    private static final Identifier LOOTR_MARKER_SILVER =
            Identifier.fromNamespaceAndPath(PolyLootr.ID, "marker_silver_planks");

    private MarkerVisuals() {
    }

    /**
     * Build the marker elements for a container of the given type.
     *
     * @param type             container type key (e.g. "chest", "chest_minecart")
     * @param defaultMarkerItem fallback for vanilla-item marker when config has no override
     * @param yOffset          vertical offset to apply to all elements (above the container)
     */
    public static List<ItemDisplayElement> build(String type, Item defaultMarkerItem, double yOffset) {
        PolyLootrConfig config = PolyLootrConfig.get();
        List<ItemDisplayElement> elements = new ArrayList<>(2);

        boolean lootrShown = config.useLootrTextures;
        boolean vanillaShown = !lootrShown || config.showVanillaItemMarker;

        if (lootrShown) {
            ItemDisplayElement cube = createLootrTexturedMarker(type);
            apply(cube, LOOTR_SCALE, yOffset, config);
            elements.add(cube);
        }

        if (vanillaShown) {
            Item item = config.markerItemFor(type, defaultMarkerItem);
            ItemDisplayElement vanilla = new ItemDisplayElement(new ItemStack(item));
            // Stack the vanilla item slightly above the cube so they don't overlap
            double y = lootrShown ? yOffset + 0.5 : yOffset;
            apply(vanilla, VANILLA_SCALE, y, config);
            elements.add(vanilla);
        }

        return elements;
    }

    private static void apply(ItemDisplayElement element, float scale, double yOffset, PolyLootrConfig config) {
        element.setScale(new Vector3f(scale, scale, scale));
        element.setOffset(new Vec3(0, yOffset, 0));
        element.setBillboardMode(Display.BillboardConstraints.VERTICAL);
        if (config.markerGlowingEnabled) {
            element.setGlowing(true);
            int color = config.markerGlowColor;
            if (color >= 0) {
                element.setGlowColorOverride(color);
            }
        }
    }

    private static ItemDisplayElement createLootrTexturedMarker(String type) {
        Identifier model = "trapped_chest".equals(type) ? LOOTR_MARKER_SILVER : LOOTR_MARKER_GOLD;
        ItemStack stack = new ItemStack(Items.STICK);
        stack.set(DataComponents.ITEM_MODEL, model);
        return new ItemDisplayElement(stack);
    }
}
