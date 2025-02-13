package com.ytdd9527.networksexpansion.implementation.machines.networks.advanced;

import org.bukkit.inventory.ItemStack;

import io.github.sefiraat.networks.network.NodeType;
import io.github.sefiraat.networks.slimefun.network.NetworkDirectional;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;

public class NetworkOutputOnlyMonitor extends NetworkDirectional {

    public NetworkOutputOnlyMonitor(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe, NodeType.OUTPUT_ONLY_MONITOR);
    }

}
