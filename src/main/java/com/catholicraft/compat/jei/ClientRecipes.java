package com.catholicraft.compat.jei;

import net.minecraft.world.item.crafting.RecipeMap;

/** Recipes the server synced to this client, for JEI to show. */
public final class ClientRecipes {
    public static RecipeMap map = RecipeMap.EMPTY;

    private ClientRecipes() {}
}
