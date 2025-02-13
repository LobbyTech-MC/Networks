package com.balugaq.netex.api.interfaces;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

public interface HasRecipes {
    static Map<ItemStack[], ItemStack> getRecipes() {
        return new HashMap<>();
    }
}
