package com.catholicraft.workbench;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * The workbench's result slot. Vanilla's result slot looks up leftovers among crafting-table recipes and, finding
 * none, would hand every ingredient back; this one asks the workbench recipe, so taking a result consumes exactly
 * one of each ingredient (keeping containers such as buckets, as vanilla does).
 */
public class WorkbenchResultSlot extends ResultSlot {
    private final CraftingContainer grid;
    private final Player player;

    public WorkbenchResultSlot(Player player, CraftingContainer grid, Container result, int slot, int x, int y) {
        super(player, grid, result, slot, x, y);
        this.grid = grid;
        this.player = player;
    }

    /** Leftovers from the workbench recipe matching the grid, or the default (container items) when none does. */
    static NonNullList<ItemStack> remaining(CraftingInput input, Player player) {
        if (player.level() instanceof net.minecraft.server.level.ServerLevel level) {
            return level.recipeAccess().getRecipeFor(WorkbenchRecipe.TYPE.get(), input, level)
                    .map(holder -> holder.value().getRemainingItems(input)).orElseGet(() -> CraftingRecipe.defaultCraftingReminder(input));
        }
        return CraftingRecipe.defaultCraftingReminder(input);
    }

    @Override
    public void onTake(Player taker, ItemStack carried) {
        checkTakeAchievements(carried);
        CraftingInput.Positioned positioned = grid.asPositionedCraftInput();
        CraftingInput input = positioned.input();
        int left = positioned.left(), top = positioned.top();
        CommonHooks.setCraftingPlayer(taker);
        NonNullList<ItemStack> leftovers = remaining(input, taker);
        CommonHooks.setCraftingPlayer(null);
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                int index = x + left + (y + top) * grid.getWidth();
                ItemStack inGrid = grid.getItem(index);
                ItemStack leftover = leftovers.get(x + y * input.width());
                if (!inGrid.isEmpty()) {
                    grid.removeItem(index, 1);
                    inGrid = grid.getItem(index);
                }
                if (leftover.isEmpty()) continue;
                if (inGrid.isEmpty()) {
                    grid.setItem(index, leftover);
                } else if (ItemStack.isSameItemSameComponents(inGrid, leftover)) {
                    leftover.grow(inGrid.getCount());
                    grid.setItem(index, leftover);
                } else if (!player.getInventory().add(leftover)) {
                    player.drop(leftover, false);
                }
            }
        }
    }
}
