package com.ytdd9527.networksexpansion.implementation.machines.encoders;

import java.util.Map;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import com.balugaq.netex.api.helpers.SupportedArmorForgeRecipes;
import com.ytdd9527.networksexpansion.core.items.machines.AbstractEncoder;
import com.ytdd9527.networksexpansion.implementation.blueprints.ArmorForgeBlueprint;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;

public class ArmorForgeEncoder extends AbstractEncoder {

    public ArmorForgeEncoder(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    public void blueprintSetter(ItemStack itemStack, ItemStack[] inputs, ItemStack crafted) {
        ArmorForgeBlueprint.setBlueprint(itemStack, inputs, crafted);
    }

    public boolean isValidBlueprint(ItemStack blueprint) {
        return SlimefunItem.getByItem(blueprint) instanceof ArmorForgeBlueprint;
    }

    public Set<Map.Entry<ItemStack[], ItemStack>> getRecipeEntries() {
        return SupportedArmorForgeRecipes.getRecipes().entrySet();
    }

    public boolean getRecipeTester(ItemStack[] inputs, ItemStack[] recipe) {
        return SupportedArmorForgeRecipes.testRecipe(inputs, recipe);
    }
}
