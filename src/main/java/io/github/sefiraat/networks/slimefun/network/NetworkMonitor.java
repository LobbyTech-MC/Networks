package io.github.sefiraat.networks.slimefun.network;

import org.bukkit.inventory.ItemStack;

import io.github.sefiraat.networks.network.NodeType;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;

public class NetworkMonitor extends NetworkDirectional {

    public NetworkMonitor(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe, NodeType.STORAGE_MONITOR);
    }

}
