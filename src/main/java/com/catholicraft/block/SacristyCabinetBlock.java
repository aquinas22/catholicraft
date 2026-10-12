package com.catholicraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A 27-slot wooden cabinet using the tested container/door implementation. */
public final class SacristyCabinetBlock extends TabernacleBlock {
    private static final MapCodec<SacristyCabinetBlock> CODEC = simpleCodec(SacristyCabinetBlock::new);
    public SacristyCabinetBlock(Properties properties) { super(properties); }
    @Override public MapCodec<SacristyCabinetBlock> codec() { return CODEC; }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return box(1,0,1,15,16,15); }
}
