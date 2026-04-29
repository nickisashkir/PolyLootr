package dev.ashkir.polylootr.render;

import dev.ashkir.polylootr.config.PolyLootrConfig;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.item.Items;
import noobanidus.mods.lootr.common.api.data.ILootrContainerInstance;
import noobanidus.mods.lootr.common.entity.LootrChestMinecartEntity;
import noobanidus.mods.lootr.common.entity.LootrItemFrame;

/**
 * Wires Lootr container entities (chest minecart, item frame) up with
 * {@link MarkerHolder}s via Polymer's {@link EntityAttachment#ofTicking}.
 *
 * <p>Block-based Lootr containers get markers via
 * {@link dev.ashkir.polylootr.overlay.PolymerOverlayRegistrar} →
 * {@link MarkerRenderer}. Entities can't use that path (it's
 * {@code BlockWithElementHolder}-specific), so we hook
 * {@link ServerEntityEvents#ENTITY_LOAD} and attach the marker once per spawn /
 * chunk-load. Polymer handles the entity's position tracking from there —
 * the marker follows the minecart as it moves.
 */
public final class EntityMarkerAttacher {
    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!PolyLootrConfig.get().markerEnabled) return;

            if (entity instanceof LootrChestMinecartEntity minecart) {
                attach((ILootrContainerInstance) minecart, "chest_minecart", minecart);
            } else if (entity instanceof LootrItemFrame frame) {
                attach((ILootrContainerInstance) frame, "item_frame", frame);
            }
        });
    }

    private static void attach(ILootrContainerInstance instance, String type, net.minecraft.world.entity.Entity entity) {
        MarkerHolder holder = new MarkerHolder(instance);
        // For entities, the marker sits slightly above the entity's hitbox center.
        // Minecart hitboxes are short, so a smaller offset than the block path.
        for (var element : MarkerVisuals.build(type, defaultItemFor(type), 0.6)) {
            holder.addElement(element);
        }
        EntityAttachment.ofTicking(holder, entity);
    }

    private static net.minecraft.world.item.Item defaultItemFor(String type) {
        return switch (type) {
            case "chest_minecart" -> Items.AMETHYST_SHARD;
            case "item_frame" -> Items.ITEM_FRAME;
            default -> Items.AMETHYST_SHARD;
        };
    }

    private EntityMarkerAttacher() {
    }
}
