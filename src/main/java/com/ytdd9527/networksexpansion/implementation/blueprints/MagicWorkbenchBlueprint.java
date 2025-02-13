package com.ytdd9527.networksexpansion.implementation.blueprints;

import org.bukkit.inventory.ItemStack;

import com.ytdd9527.networksexpansion.core.items.unusable.AbstractBlueprint;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;

public class MagicWorkbenchBlueprint extends AbstractBlueprint {

    public MagicWorkbenchBlueprint(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }
}
