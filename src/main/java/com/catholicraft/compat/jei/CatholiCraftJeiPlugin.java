package com.catholicraft.compat.jei;

import com.catholicraft.CatholiCraft;
import com.catholicraft.registry.ModContent;
import com.catholicraft.workbench.WorkbenchRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;

/** Loaded by JEI only when JEI is installed. */
@JeiPlugin
public class CatholiCraftJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return CatholiCraft.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new WorkbenchCategory(registration.getJeiHelpers().getGuiHelper(), ModContent.ST_JOSEPHS_WORKBENCH.get()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(WorkbenchCategory.TYPE, ClientRecipes.map.byType(WorkbenchRecipe.TYPE.get()).stream()
                .map(h -> h.value()).filter(r -> r instanceof WorkbenchRecipe).map(r -> (WorkbenchRecipe) r).toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(WorkbenchCategory.TYPE, ModContent.ST_JOSEPHS_WORKBENCH.get());
    }
}
