package dev.ashkir.polylootr.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ashkir.polylootr.render.MenuTitleWrapper;
import net.minecraft.world.MenuProvider;
import noobanidus.mods.lootr.common.api.data.ILootrContainerInstance;
import noobanidus.mods.lootr.common.api.interfaces.lootr.ILootrAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Wraps the {@link MenuProvider} that Lootr passes to {@code player.openMenu}
 * with {@link MenuTitleWrapper}, which augments the menu title with live
 * refresh / decay / opener info.
 *
 * <p>Targets the second {@code openMenu} INVOKE inside
 * {@code ILootrAPI.handleInstanceOpen} ({@code ordinal = 1}). The first INVOKE
 * is the spectator-branch call which passes {@code null} and shouldn't be
 * wrapped.
 *
 * <p>Uses {@link Local @Local(argsOnly = true)} to capture the
 * {@link ILootrContainerInstance} method argument so the wrapper can query the
 * inventory store at title-render time.
 */
@Mixin(ILootrAPI.class)
public interface MenuTitleMixin {
    @ModifyArg(
            method = "handleInstanceOpen(Lnoobanidus/mods/lootr/common/api/data/ILootrContainerInstance;Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/common/api/interfaces/container/IMenuBuilder;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;",
                    ordinal = 1
            ),
            remap = false
    )
    private static MenuProvider polylootr$wrapMenuProvider(
            MenuProvider original,
            @Local(argsOnly = true) ILootrContainerInstance instance) {
        return new MenuTitleWrapper(original, instance);
    }
}
