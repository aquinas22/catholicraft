package com.catholicraft.workbench;

import com.catholicraft.CatholiCraft;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * A recipe made only at St. Joseph's Workbench: an ordinary shaped or shapeless crafting recipe, kept out of the
 * crafting table so CatholiCraft items never collide with vanilla or other mods' recipes. JSON:
 * {@code {"type": "catholicraft:workbench", "recipe": {"type": "minecraft:crafting_shaped", ...}}}.
 */
public record WorkbenchRecipe(CraftingRecipe recipe) implements CraftingRecipe {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, CatholiCraft.ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, CatholiCraft.ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CraftingRecipe>> TYPE = TYPES.register("workbench",
            () -> RecipeType.simple(CatholiCraft.id("workbench")));

    static final Codec<CraftingRecipe> CRAFTING = Recipe.CODEC.comapFlatMap(r -> r instanceof CraftingRecipe c ? DataResult.success(c)
            : DataResult.error(() -> "A workbench recipe must wrap a crafting recipe"), c -> c);
    static final MapCodec<WorkbenchRecipe> CODEC = CRAFTING.fieldOf("recipe").xmap(WorkbenchRecipe::new, WorkbenchRecipe::recipe);
    static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchRecipe> STREAM_CODEC = Recipe.STREAM_CODEC.map(
            r -> new WorkbenchRecipe((CraftingRecipe) r), WorkbenchRecipe::recipe);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<WorkbenchRecipe>> SERIALIZER = SERIALIZERS.register("workbench",
            () -> new RecipeSerializer<>(CODEC, STREAM_CODEC));

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return recipe.matches(input, level);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return recipe.assemble(input);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return recipe.getRemainingItems(input);
    }

    @Override
    public boolean showNotification() {
        return recipe.showNotification();
    }

    @Override
    public String group() {
        return recipe.group();
    }

    @Override
    public RecipeSerializer<WorkbenchRecipe> getSerializer() {
        return SERIALIZER.get();
    }

    @Override
    public RecipeType<CraftingRecipe> getType() {
        return TYPE.get();
    }

    /** Not offered to the vanilla recipe book, which would try to place it in a crafting table. */
    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public CraftingBookCategory category() {
        return recipe.category();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return recipe.recipeBookCategory();
    }

    /** The wrapped recipe's grid and result, for JEI. */
    @Override
    public List<RecipeDisplay> display() {
        return recipe.display();
    }
}
