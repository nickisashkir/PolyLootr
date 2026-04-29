package dev.ashkir.polylootr;

import dev.ashkir.polylootr.commands.PolyLootrCommands;
import dev.ashkir.polylootr.config.PolyLootrConfig;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import noobanidus.mods.lootr.fabric.init.ModParticles;
import noobanidus.mods.lootr.fabric.init.ModStats;

/**
 * Fabric entrypoint for PolyLootr.
 *
 * <p>Loads the JSON config, marks Lootr's custom registry entries as server-only
 * (so vanilla clients don't warn about unknown stat / particle ids on connect),
 * and registers the bundled resource pack.
 *
 * <p>Polymer overlay registration lives in
 * {@link dev.ashkir.polylootr.overlay.PolymerOverlayRegistrar} and runs from
 * {@link dev.ashkir.polylootr.mixin.LootrInitMixin} at the tail of Lootr's own
 * initializer.
 */
public class PolyLootr implements ModInitializer {
    public static final String ID = "polylootr";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    @Override
    public void onInitialize() {
        PolyLootrConfig.load();

        FabricLoader.getInstance().getModContainer(ID).ifPresent(modContainer ->
                ResourceLoader.registerBuiltinPack(
                        id(ID),
                        modContainer,
                        Component.literal("PolyLootr"),
                        PackActivationType.ALWAYS_ENABLED));

        markServerOnly(BuiltInRegistries.CUSTOM_STAT, ModStats.LOOTED_LOCATION);
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.PARTICLE_TYPE, ModParticles.UNOPENED_PARTCLE);
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.PARTICLE_TYPE, ModParticles.REFRESH_PARTICLE);

        PolyLootrCommands.register();

        // Bundle PolyLootr's assets into Polymer's auto-served resource pack so
        // vanilla clients can render the custom item models we register on
        // markers. polymer-autohost (also bundled) handles distribution.
        PolymerResourcePackUtils.addModAssets(ID);
    }

    /**
     * Marks a registry entry as server-only via {@link RegistrySyncUtils}. Wrapped in
     * a raw-typed helper because the two {@code setServerEntry} overloads (entry-by-value
     * vs entry-by-id) become ambiguous when the registry's value type is itself
     * {@link Identifier}, as it is for {@code CUSTOM_STAT}.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void markServerOnly(Registry registry, Object entry) {
        RegistrySyncUtils.setServerEntry(registry, entry);
    }
}
