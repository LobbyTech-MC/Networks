package com.balugaq.netex.api.interfaces;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public interface HasRecipes {
    static Map<ItemStack[], ItemStack> getRecipes() {
        return new HashMap<>();
    }
}
