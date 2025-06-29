package com.balugaq.netex.api.helpers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.inventory.ItemStack;

import com.balugaq.netex.api.interfaces.CanTestRecipe;
import com.balugaq.netex.api.interfaces.RecipesHolder;
import io.github.sefiraat.networks.utils.StackUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.backpacks.SlimefunBackpack;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class SupportedAncientAltarRecipes implements RecipesHolder, CanTestRecipe {

    private static final Map<ItemStack[], ItemStack> RECIPES = new HashMap<>();

    static {
        for (SlimefunItem item : Slimefun.getRegistry().getEnabledSlimefunItems()) {
            RecipeType recipeType = item.getRecipeType();
            if ((recipeType == RecipeType.ANCIENT_ALTAR) && allowedRecipe(item)) {
                ItemStack[] itemStacks = new ItemStack[9];
                int i = 0;
                for (ItemStack itemStack : item.getRecipe()) {
                    if (itemStack == null) {
                        itemStacks[i] = null;
                    } else {
                        itemStacks[i] = new ItemStack(itemStack.clone());
                    }
                    if (++i >= 9) {
                        break;
                    }
                }
                SupportedAncientAltarRecipes.addRecipe(itemStacks, item.getRecipeOutput());
            }
        }
    }

    public static @Nonnull Map<ItemStack[], ItemStack> getRecipes() {
        return RECIPES;
    }

    public static void addRecipe(@Nonnull ItemStack[] input, @Nonnull ItemStack output) {
        RECIPES.put(input, output);
    }

    public static boolean testRecipe(@Nonnull ItemStack[] input, @Nonnull ItemStack[] recipe) {
        for (int test = 0; test < recipe.length; test++) {
            if (!StackUtils.itemsMatch(input[test], recipe[test])) {
                return false;
            }
        }
        return true;
    }

    public static boolean allowedRecipe(@Nonnull SlimefunItem item) {
        return !(item instanceof SlimefunBackpack);
    }

}
