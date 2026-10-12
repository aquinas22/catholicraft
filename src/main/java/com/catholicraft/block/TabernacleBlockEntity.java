package com.catholicraft.block;

import com.catholicraft.registry.ModContent;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class TabernacleBlockEntity extends BaseContainerBlockEntity {
    private final int size;
    private NonNullList<ItemStack> items;
    private final ContainerOpenersCounter openers = new ContainerOpenersCounter() {
        @Override protected void onOpen(Level l, BlockPos p, BlockState s) { door(l, p, s, true); }
        @Override protected void onClose(Level l, BlockPos p, BlockState s) { door(l, p, s, false); }
        @Override protected void openerCountChanged(Level l, BlockPos p, BlockState s, int oldCount, int count) {}
        @Override public boolean isOwnContainer(Player p) { return p.containerMenu instanceof ChestMenu m && m.getContainer() == TabernacleBlockEntity.this; }
    };

    public TabernacleBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.TABERNACLE_ENTITY.get(), pos, state);
        size = state.is(ModContent.SACRISTY_CABINET.get()) ? 27 : 9;
        items = NonNullList.withSize(size, ItemStack.EMPTY);
    }
    @Override public int getContainerSize() { return size; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> value) { items = value; }
    @Override protected Component getDefaultName() { return getBlockState().getBlock().getName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new ChestMenu(size == 27 ? MenuType.GENERIC_9x3 : MenuType.GENERIC_9x1, id, inventory, this, size / 9); }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(size, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
    }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); ContainerHelper.saveAllItems(output, items); }
    @Override public void startOpen(ContainerUser user) {
        if (!isRemoved() && !user.getLivingEntity().isSpectator()) openers.incrementOpeners(user.getLivingEntity(), level, worldPosition, getBlockState(), user.getContainerInteractionRange());
    }
    @Override public void stopOpen(ContainerUser user) {
        if (!isRemoved() && !user.getLivingEntity().isSpectator()) openers.decrementOpeners(user.getLivingEntity(), level, worldPosition, getBlockState());
    }
    @Override public List<ContainerUser> getEntitiesWithContainerOpen() { return openers.getEntitiesWithContainerOpen(level, worldPosition); }
    public void recheckOpen() { if (!isRemoved()) openers.recheckOpeners(level, worldPosition, getBlockState()); }
    private void door(Level level, BlockPos pos, BlockState state, boolean open) {
        level.setBlock(pos, state.setValue(TabernacleBlock.OPEN, open), 3);
        level.playSound(null, pos, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, .5F, 1.2F);
    }
}
