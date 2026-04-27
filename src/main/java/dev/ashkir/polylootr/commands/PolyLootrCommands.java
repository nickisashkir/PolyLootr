package dev.ashkir.polylootr.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.ashkir.polylootr.config.PolyLootrConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.world.level.block.entity.BlockEntity;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.ILootrInventoryStore;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import noobanidus.mods.lootr.fabric.init.ModStats;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Registers the {@code /polylootr} command suite. Disabled wholesale via
 * {@link PolyLootrConfig#commandsEnabled}; permission level controlled via
 * {@link PolyLootrConfig#commandPermissionLevel}.
 *
 * <p>Subcommands:
 * <ul>
 *   <li>{@code /polylootr reload} — re-read {@code config/polylootr.json}
 *       without restarting the server.</li>
 *   <li>{@code /polylootr nearby [radius]} — list every Lootr container within
 *       {@code radius} blocks of the executor (default 16) with its current
 *       refresh / decay state and opener count.</li>
 *   <li>{@code /polylootr stats [player]} — report the {@code lootr:looted_stat}
 *       value for the executor (or named target).</li>
 * </ul>
 */
public final class PolyLootrCommands {
    private static final int DEFAULT_NEARBY_RADIUS = 16;
    private static final int MAX_NEARBY_RADIUS = 64;
    private static final int MAX_LIST_RESULTS = 32;

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            if (!PolyLootrConfig.get().commandsEnabled) return;
            PermissionCheck permission = permissionCheckFor(PolyLootrConfig.get().commandPermissionLevel);

            dispatcher.register(literal("polylootr")
                    .requires(source -> permission.check(source.permissions()))
                    .then(literal("reload")
                            .executes(PolyLootrCommands::reload))
                    .then(literal("nearby")
                            .executes(ctx -> nearby(ctx.getSource(), DEFAULT_NEARBY_RADIUS))
                            .then(argument("radius", IntegerArgumentType.integer(1, MAX_NEARBY_RADIUS))
                                    .executes(ctx -> nearby(ctx.getSource(),
                                            IntegerArgumentType.getInteger(ctx, "radius")))))
                    .then(literal("stats")
                            .executes(ctx -> stats(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                            .then(argument("player", EntityArgument.player())
                                    .executes(ctx -> stats(ctx.getSource(),
                                            EntityArgument.getPlayer(ctx, "player"))))));
        });
    }

    private static int reload(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        PolyLootrConfig.load();
        ctx.getSource().sendSuccess(() ->
                Component.literal("PolyLootr config reloaded.").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int nearby(CommandSourceStack source, int radius) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel world = player.level();
        BlockPos origin = player.blockPosition();

        List<NearbyResult> results = new ArrayList<>();
        int radiusSquared = radius * radius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (origin.distSqr(pos) > radiusSquared) continue;
                    BlockEntity be = world.getBlockEntity(pos);
                    if (!(be instanceof ILootrBlockEntity lootr)) continue;
                    results.add(new NearbyResult(pos, lootr));
                    if (results.size() >= MAX_LIST_RESULTS) break;
                }
                if (results.size() >= MAX_LIST_RESULTS) break;
            }
            if (results.size() >= MAX_LIST_RESULTS) break;
        }

        if (results.isEmpty()) {
            source.sendSuccess(() ->
                    Component.literal("No Lootr containers within " + radius + " blocks.")
                            .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }

        source.sendSuccess(() ->
                Component.literal("Lootr containers within " + radius + " blocks (" + results.size()
                        + (results.size() >= MAX_LIST_RESULTS ? "+" : "") + "):")
                        .withStyle(ChatFormatting.AQUA), false);

        for (NearbyResult r : results) {
            BlockPos p = r.pos();
            ILootrBlockEntity lootr = r.entity();
            ILootrInventoryStore store = LootrAPI.getData(lootr);
            int openers = lootr.getActualOpeners().size();

            StringBuilder line = new StringBuilder();
            line.append("  [").append(p.getX()).append(", ").append(p.getY()).append(", ").append(p.getZ()).append("]");
            line.append("  openers=").append(openers);
            if (store != null) {
                if (store.isRefreshed()) line.append("  §a(refreshed)§r");
                int refresh = store.remainingRefreshTime();
                if (refresh > 0) line.append("  refresh=").append(formatTicks(refresh));
                int decay = store.remainingDecayTime();
                if (decay > 0) line.append("  decay=").append(formatTicks(decay));
                if (store.isDecayed()) line.append("  §c(decayed)§r");
            }
            String formatted = line.toString();
            source.sendSuccess(() -> Component.literal(formatted), false);
        }

        return results.size();
    }

    private static int stats(CommandSourceStack source, ServerPlayer target) {
        int count = target.getStats().getValue(ModStats.LOOTED_STAT);
        source.sendSuccess(() ->
                Component.literal(target.getName().getString() + " has looted ")
                        .append(Component.literal(String.valueOf(count)).withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(" Lootr container" + (count == 1 ? "" : "s") + ".")), false);
        return count;
    }

    /** Formats a tick count as a human-readable duration like {@code 2m 35s}. */
    private static String formatTicks(int ticks) {
        int seconds = ticks / 20;
        if (seconds < 60) return seconds + "s";
        int minutes = seconds / 60;
        int remSeconds = seconds % 60;
        if (minutes < 60) return minutes + "m " + remSeconds + "s";
        int hours = minutes / 60;
        int remMinutes = minutes % 60;
        return hours + "h " + remMinutes + "m";
    }

    private record NearbyResult(BlockPos pos, ILootrBlockEntity entity) {
    }

    /**
     * Maps a numeric op level (0-4, the historical {@code hasPermission(int)} semantics)
     * to the corresponding 26.1 {@link PermissionCheck} constant.
     */
    private static PermissionCheck permissionCheckFor(int level) {
        return switch (level) {
            case 0 -> Commands.LEVEL_ALL;
            case 1 -> Commands.LEVEL_MODERATORS;
            case 3 -> Commands.LEVEL_ADMINS;
            case 4 -> Commands.LEVEL_OWNERS;
            default -> Commands.LEVEL_GAMEMASTERS;
        };
    }

    private PolyLootrCommands() {
    }
}
