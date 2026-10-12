package com.catholicraft.block;

import com.catholicraft.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class IncenseBurnerBlockEntity extends BlockEntity {
    public static final int TICKS_PER_INCENSE=1200;
    public static final int CAPACITY=6000;
    private int burnTicks;
    public IncenseBurnerBlockEntity(BlockPos pos,BlockState state) { super(ModContent.INCENSE_BURNER_ENTITY.get(),pos,state); }
    public int remainingTicks() { return burnTicks; }
    public boolean addIncense() {
        if (burnTicks>CAPACITY-TICKS_PER_INCENSE) return false;
        burnTicks+=TICKS_PER_INCENSE;
        setChanged();
        return true;
    }
    public static void tick(Level level,BlockPos pos,BlockState state,IncenseBurnerBlockEntity burner) {
        if (burner.burnTicks<=0) {
            if (state.getValue(IncenseBurnerBlock.LIT)) level.setBlock(pos,state.setValue(IncenseBurnerBlock.LIT,false),3);
            return;
        }
        burner.burnTicks--;
        // Mark every decrement dirty so a save/unload cannot restore already burned fuel.
        burner.setChanged();
        if (!state.getValue(IncenseBurnerBlock.LIT)) level.setBlock(pos,state.setValue(IncenseBurnerBlock.LIT,true),3);
        if (burner.burnTicks%20==0 && level instanceof ServerLevel server) server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,pos.getX()+.5,pos.getY()+.85,pos.getZ()+.5,2,.1,.1,.1,.02);
        if (burner.burnTicks==0) level.setBlock(pos,level.getBlockState(pos).setValue(IncenseBurnerBlock.LIT,false),3);
    }
    @Override protected void saveAdditional(ValueOutput out) { super.saveAdditional(out); out.putInt("BurnTicks",burnTicks); }
    @Override protected void loadAdditional(ValueInput in) { super.loadAdditional(in); burnTicks=Math.clamp(in.getIntOr("BurnTicks",0),0,CAPACITY); }
}
