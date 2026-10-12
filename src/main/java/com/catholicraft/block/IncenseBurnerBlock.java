package com.catholicraft.block;

import com.catholicraft.registry.ModContent;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class IncenseBurnerBlock extends BaseEntityBlock {
    public static final MapCodec<IncenseBurnerBlock> CODEC=simpleCodec(IncenseBurnerBlock::new);
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE=Shapes.or(box(3,0,3,13,3,13),box(5,3,5,11,7,11),box(2,7,2,14,12,14));
    public IncenseBurnerBlock(Properties p) { super(p); registerDefaultState(stateDefinition.any().setValue(LIT,false).setValue(FACING,Direction.NORTH)); }
    @Override protected MapCodec<IncenseBurnerBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { b.add(LIT,FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c) { return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite()); }
    @Override protected BlockState rotate(BlockState s,Rotation r) { return s.setValue(FACING,r.rotate(s.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState s,Mirror m) { return rotate(s,m.getRotation(s.getValue(FACING))); }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c) { return SHAPE; }
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s) { return new IncenseBurnerBlockEntity(p,s); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t) {
        return l.isClientSide() ? null : createTickerHelper(t,ModContent.INCENSE_BURNER_ENTITY.get(),IncenseBurnerBlockEntity::tick);
    }
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit) {
        if (!stack.is(ModContent.INCENSE.get())) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!l.isClientSide() && l.getBlockEntity(p) instanceof IncenseBurnerBlockEntity burner) {
            if (!burner.addIncense()) {
                player.sendOverlayMessage(Component.translatable("message.catholicraft.burner_full"));
                return InteractionResult.FAIL;
            }
            stack.consume(1,player);
            l.setBlock(p,s.setValue(LIT,true),3);
            l.playSound(null,p,SoundEvents.FIRECHARGE_USE,SoundSource.BLOCKS,.4F,1.1F);
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit) {
        if (!l.isClientSide() && l.getBlockEntity(p) instanceof IncenseBurnerBlockEntity burner) player.sendOverlayMessage(Component.translatable("message.catholicraft.burner_time",(burner.remainingTicks()+19)/20));
        return InteractionResult.SUCCESS;
    }
}
