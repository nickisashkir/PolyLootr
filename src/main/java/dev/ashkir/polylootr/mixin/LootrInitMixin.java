package dev.ashkir.polylootr.mixin;

import dev.ashkir.polylootr.overlay.PolymerOverlayRegistrar;
import noobanidus.mods.lootr.fabric.Lootr;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Single injection point for all Polymer overlay registration.
 *
 * <p>Hooking the tail of Lootr's own initializer guarantees every static-field
 * assignment Lootr does inside its register methods has run before we read any
 * {@code ModBlocks.X} or {@code ModEntities.X} field.
 */
@Mixin(Lootr.class)
public class LootrInitMixin {
    @Inject(method = "onInitialize", at = @At("TAIL"), remap = false)
    private void polylootr$registerOverlays(CallbackInfo ci) {
        PolymerOverlayRegistrar.registerAll();
    }
}
