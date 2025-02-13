package com.balugaq.netex.utils;

import javax.annotation.Nonnull;

import org.bukkit.Location;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LocationUtil {
    @Nonnull
    public static String humanize(@Nonnull Location location) {
        return String.format("%s (%.2f, %.2f, %.2f)", location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    @Nonnull
    public static String humanizeBlock(@Nonnull Location location) {
        return String.format("%s (%d, %d, %d)", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Nonnull
    public static String humanizeFull(@Nonnull Location location) {
        return String.format("%s (%.2f, %.2f, %.2f, %.2f, %.2f)", location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }
}
