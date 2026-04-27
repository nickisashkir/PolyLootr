package dev.ashkir.polylootr.render;

import dev.ashkir.polylootr.config.PolyLootrConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.ILootrContainerInstance;
import noobanidus.mods.lootr.common.api.data.ILootrInventoryStore;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps the {@link MenuProvider} Lootr passes to {@code player.openMenu} so the
 * menu title shows live refresh / decay / opener info at open time.
 *
 * <p>Created from {@code MenuTitleMixin}'s {@code @ModifyArg} on the
 * {@code openMenu} call inside {@code ILootrAPI.handleInstanceOpen}. The wrapper
 * delegates {@code createMenu} unchanged and only augments {@link #getDisplayName()}.
 *
 * <p>Augmentation is gated by {@link PolyLootrConfig#menuTitleInfoEnabled} and
 * uses configurable suffix templates from the same config so server admins can
 * tune the format (color codes, label text) without recompiling.
 */
public final class MenuTitleWrapper implements MenuProvider {
    private final MenuProvider delegate;
    private final ILootrContainerInstance instance;

    public MenuTitleWrapper(MenuProvider delegate, ILootrContainerInstance instance) {
        this.delegate = delegate;
        this.instance = instance;
    }

    @Override
    public Component getDisplayName() {
        Component base = delegate.getDisplayName();
        if (!PolyLootrConfig.get().menuTitleInfoEnabled) return base;
        return augment(base, instance);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return delegate.createMenu(containerId, inventory, player);
    }

    private static Component augment(Component base, ILootrContainerInstance instance) {
        PolyLootrConfig config = PolyLootrConfig.get();
        ILootrInventoryStore store = LootrAPI.getData(instance);

        MutableComponent result = Component.empty().append(base);

        int openers = instance.getActualOpeners().size();
        if (openers > 0 && !config.menuTitleOpenersSuffix.isEmpty()) {
            result.append(format(config.menuTitleOpenersSuffix, openers, openers == 1 ? "" : "s"));
        }

        if (store == null) return result;

        int refresh = store.remainingRefreshTime();
        if (refresh > 0 && !config.menuTitleRefreshSuffix.isEmpty()) {
            result.append(format(config.menuTitleRefreshSuffix, formatTicks(refresh)));
        }

        int decay = store.remainingDecayTime();
        if (decay > 0 && !config.menuTitleDecaySuffix.isEmpty()) {
            result.append(format(config.menuTitleDecaySuffix, formatTicks(decay)));
        }

        return result;
    }

    private static Component format(String template, Object... args) {
        return Component.literal(safeFormat(template, args));
    }

    /**
     * Calls {@code String.format} but never throws on a malformed user-supplied
     * template — returns the raw template instead so a typo in the config doesn't
     * crash the menu open.
     */
    private static String safeFormat(String template, @Nullable Object... args) {
        try {
            return String.format(template, args);
        } catch (Exception e) {
            return template;
        }
    }

    private static String formatTicks(int ticks) {
        int seconds = ticks / 20;
        if (seconds < 60) return seconds + "s";
        int minutes = seconds / 60;
        int remSeconds = seconds % 60;
        if (minutes < 60) return minutes + "m" + (remSeconds > 0 ? " " + remSeconds + "s" : "");
        int hours = minutes / 60;
        int remMinutes = minutes % 60;
        return hours + "h" + (remMinutes > 0 ? " " + remMinutes + "m" : "");
    }
}
