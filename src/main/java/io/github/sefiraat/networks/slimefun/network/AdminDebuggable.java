package io.github.sefiraat.networks.slimefun.network;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;

import io.github.sefiraat.networks.Networks;

public interface AdminDebuggable {
    Set<Player> VIEWERS = new HashSet<>();
    String DEBUG_KEY = "network_debugging";

    default boolean isDebug(@Nonnull Location location) {
        String debug = StorageCacheUtils.getData(location, DEBUG_KEY);
        return Boolean.parseBoolean(debug);
    }

    default void setDebug(@Nonnull Location location, boolean value) {
        StorageCacheUtils.setData(location, DEBUG_KEY, String.valueOf(value));
    }

    default void toggle(@Nonnull Location location, @Nonnull Player player) {
        final boolean isDebug = isDebug(location);
        final boolean nextState = !isDebug;
        setDebug(location, nextState);
        player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.debug.toggle-debug"), nextState));
        if (nextState) {
            player.sendMessage(Networks.getLocalizationService().getString("messages.debug.enabled-debug"));
        }
    }

    default void sendDebugMessage(@Nonnull Location location, @Nonnull String string) {
        if (isDebug(location)) {
            final String locationString = "W[" + location.getWorld().getName() + "] " +
                    "X[" + location.getBlockX() + "] " +
                    "Y[" + location.getBlockY() + "] " +
                    "Z[" + location.getBlockZ() + "] ";
            Networks.getInstance().getLogger().log(Level.INFO, String.format(Networks.getLocalizationService().getString("messages.debug.info"), locationString, string));
            for (Player player : VIEWERS) {
                if (player.isOnline()) {
                    player.sendMessage(String.format(Networks.getLocalizationService().getString("messages.debug.viewer-info"), locationString, string));
                } else {
                    removeViewer(player);
                }
            }
        }
    }

    default void addViewer(@Nonnull Player player) {
        VIEWERS.add(player);
    }

    default void removeViewer(@Nonnull Player player) {
        VIEWERS.remove(player);
    }

    default boolean hasViewer(@Nonnull Player player) {
        return VIEWERS.contains(player);
    }
}
