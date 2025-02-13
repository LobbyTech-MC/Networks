package com.balugaq.netex.api.interfaces;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public interface CanTestRecipe {
    static boolean testRecipe(@Nonnull ItemStack[] input, @Nonnull ItemStack[] recipe) {
        return false;
    }
}
