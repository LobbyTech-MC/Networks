package com.balugaq.netex.utils;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.inventory.ItemFlag;

import com.balugaq.netex.api.enums.MinecraftVersion;

import io.github.sefiraat.networks.Networks;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NetworksVersionedItemFlag {
    public static final ItemFlag HIDE_ADDITIONAL_TOOLTIP;

    static {
        MinecraftVersion version = Networks.getInstance().getMCVersion();
        HIDE_ADDITIONAL_TOOLTIP = version.isAtLeast(MinecraftVersion.MC1_20_5) ? ItemFlag.HIDE_ADDITIONAL_TOOLTIP : getKey("HIDE_POTION_EFFECTS");

    }

    @Nullable
    private static ItemFlag getKey(@Nonnull String key) {
        try {
            Field field = ItemFlag.class.getDeclaredField(key);
            return (ItemFlag) field.get((Object) null);
        } catch (Exception ignored) {
            return null;
        }
    }
}
