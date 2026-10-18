package com.catholicraft.workbench;

import com.catholicraft.registry.ModContent;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;

/**
 * St. Joseph's Workbench: the crafting table's 3x3 grid and screen, matching only CatholiCraft workbench recipes.
 * The result slot is replaced so leftovers come from the workbench recipe (see {@link WorkbenchResultSlot}).
 */
public class WorkbenchMenu extends CraftingMenu {
    private final ContainerLevelAccess access;
    private final Player player;

    public WorkbenchMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(id, inventory, access);
        this.access = access;
        this.player = inventory.player;
        WorkbenchResultSlot result = new WorkbenchResultSlot(player, craftSlots, resultSlots, 0, slots.get(0).x, slots.get(0).y);
        result.index = 0;
        slots.set(0, result);
    }

    @Override
    public void slotsChanged(Container container) {
        access.execute((level, pos) -> {
            if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
                var input = craftSlots.asCraftInput();
                ItemStack result = ItemStack.EMPTY;
                var found = server.recipeAccess().getRecipeFor(WorkbenchRecipe.TYPE.get(), input, server);
                if (found.isPresent() && resultSlots.setRecipeUsed(sp, found.get())) {
                    ItemStack made = found.get().value().assemble(input);
                    if (made.isItemEnabled(server.enabledFeatures())) result = made;
                }
                resultSlots.setItem(0, result);
                setRemoteSlot(0, result);
                sp.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 0, result));
            }
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModContent.ST_JOSEPHS_WORKBENCH.get());
    }
}
