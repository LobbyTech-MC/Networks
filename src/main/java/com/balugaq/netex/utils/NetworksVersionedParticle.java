package com.balugaq.netex.utils;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Particle;

import com.balugaq.netex.api.enums.MinecraftVersion;

import io.github.sefiraat.networks.Networks;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NetworksVersionedParticle {
    public static final @javax.annotation.Nullable Particle DUST;
    public static final @javax.annotation.Nullable Particle EXPLOSION;
    public static final @javax.annotation.Nullable Particle SMOKE;

    static {
        MinecraftVersion version = Networks.getInstance().getMCVersion();
        DUST = version.isAtLeast(MinecraftVersion.MC1_20_5) ? Particle.DUST : getKey("REDSTONE");
        EXPLOSION = version.isAtLeast(MinecraftVersion.MC1_20_5) ? Particle.EXPLOSION : getKey("EXPLOSION_LARGE");
        SMOKE = version.isAtLeast(MinecraftVersion.MC1_20_5) ? Particle.SMOKE : getKey("SMOKE_NORMAL");
    }

    @Nullable
    private static Particle getKey(@Nonnull String key) {
        try {
            Field field = Particle.class.getDeclaredField(key);
            return (Particle) field.get(null);
        } catch (Exception ignored) {
            return null;
        }
    }
}
