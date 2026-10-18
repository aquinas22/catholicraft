package com.catholicraft.compat.jei;

import com.catholicraft.CatholiCraft;
import com.catholicraft.workbench.WorkbenchRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.level.ItemLike;

/** St. Joseph's Workbench recipes in JEI: a 3x3 grid, an arrow and the result, like the crafting table. */
public class WorkbenchCategory implements IRecipeCategory<WorkbenchRecipe> {
    public static final IRecipeType<WorkbenchRecipe> TYPE = IRecipeType.create(CatholiCraft.id("workbench"), WorkbenchRecipe.class);
    private final IDrawable icon;
    private final IDrawableStatic slot, arrow;
    private final ICraftingGridHelper grid;

    public WorkbenchCategory(IGuiHelper gui, ItemLike workbench) {
        this.icon = gui.createDrawableItemLike(workbench);
        this.slot = gui.getSlotDrawable();
        this.arrow = gui.getRecipeArrow();
        this.grid = gui.createCraftingGridHelper();
    }

    @Override
    public IRecipeType<WorkbenchRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.catholicraft.st_josephs_workbench");
    }

    @Override
    public int getWidth() {
        return 116;
    }

    @Override
    public int getHeight() {
        return 54;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public Identifier getIdentifier(WorkbenchRecipe recipe) {
        return null;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WorkbenchRecipe recipe, IFocusGroup focuses) {
        for (var display : recipe.display()) {
            if (display instanceof ShapedCraftingRecipeDisplay shaped) {
                grid.createAndSetIngredientsFromDisplays(builder, shaped.ingredients(), shaped.width(), shaped.height());
                grid.createAndSetOutputs(builder, shaped.result());
                return;
            }
            if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
                grid.createAndSetIngredientsFromDisplays(builder, shapeless.ingredients(), 0, 0);
                grid.createAndSetOutputs(builder, shapeless.result());
                return;
            }
        }
    }

    @Override
    public void draw(WorkbenchRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor g, double mx, double my) {
        arrow.draw(g, 61, 18);
    }
}
