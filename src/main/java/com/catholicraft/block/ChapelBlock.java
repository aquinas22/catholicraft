package com.catholicraft.block;

import com.catholicraft.registry.ModContent;
import com.catholicraft.item.AltarClothItem;
import com.catholicraft.item.AspergillumItem;
import com.catholicraft.item.StationNames;
import com.catholicraft.world.PewSeating;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChapelBlock extends Block {
    public enum Kind { ALTAR, CRUCIFIX, FONT, CANDLE, LAMP, BELL, KNEELER, PEW, CHALICE, CIBORIUM, MONSTRANCE, WREATH, VOTIVE, STATIONS, STATUE, TABLE, AMBO }
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty WATER = IntegerProperty.create("water", 0, 3);
    public static final IntegerProperty CLOTH = IntegerProperty.create("cloth",0,5);
    public static final IntegerProperty CANDLES = IntegerProperty.create("candles",0,4);
    public static final IntegerProperty STATION = IntegerProperty.create("station",1,14);
    private final Kind kind;
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    public ChapelBlock(Properties p, Kind kind) {
        super(p);
        this.kind = kind;
        var defaults=stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false).setValue(POWERED, false).setValue(WATER, 0);
        if (defaults.hasProperty(CLOTH)) defaults=defaults.setValue(CLOTH,0);
        if (defaults.hasProperty(CANDLES)) defaults=defaults.setValue(CANDLES,0);
        if (defaults.hasProperty(STATION)) defaults=defaults.setValue(STATION,1);
        registerDefaultState(defaults);
        VoxelShape base = switch (kind) {
            case ALTAR -> Shapes.or(box(0, 12, 0, 16, 16, 16), box(2, 0, 2, 5, 12, 14), box(11, 0, 2, 14, 12, 14));
            case CRUCIFIX -> Shapes.or(box(6, 0, 6, 10, 16, 10), box(1, 10, 6, 15, 13, 10));
            case FONT -> Shapes.or(box(3, 0, 3, 13, 2, 13), box(6, 2, 6, 10, 10, 10), box(2, 10, 2, 14, 14, 14));
            case CANDLE -> Shapes.or(box(4, 0, 4, 12, 2, 12), box(7, 2, 7, 9, 5, 9), box(6, 5, 6, 10, 14, 10));
            case LAMP -> Shapes.or(box(4, 0, 4, 12, 2, 12), box(5, 2, 5, 11, 12, 11), box(4, 12, 4, 12, 14, 12));
            case BELL -> Shapes.or(box(0, 0, 5, 3, 16, 11), box(13, 0, 5, 16, 16, 11), box(0, 14, 5, 16, 16, 11), box(4, 4, 4, 12, 13, 12));
            case KNEELER -> Shapes.or(box(0, 2, 3, 16, 5, 8), box(0, 12, 12, 16, 15, 16), box(1, 0, 12, 3, 12, 15), box(13, 0, 12, 15, 12, 15));
            case PEW -> Shapes.or(box(0, 5, 2, 16, 8, 14), box(0, 8, 12, 16, 16, 15), box(1, 0, 3, 3, 5, 13), box(13, 0, 3, 15, 5, 13));
            case CHALICE -> Shapes.or(box(4, 0, 4, 12, 2, 12), box(7, 2, 7, 9, 7, 9), box(4, 7, 4, 12, 11, 12));
            case CIBORIUM -> Shapes.or(box(4, 0, 4, 12, 2, 12), box(7, 2, 7, 9, 7, 9), box(3, 7, 3, 13, 12, 13), box(7, 12, 7, 9, 16, 9));
            case MONSTRANCE -> Shapes.or(box(4, 0, 4, 12, 2, 12), box(7, 2, 7, 9, 6, 9), box(2, 6, 7, 14, 14, 9), box(7, 14, 7, 9, 16, 9));
            case WREATH -> Shapes.or(box(2,0,2,14,3,14),box(4,3,4,12,10,12));
            case VOTIVE -> Shapes.or(box(1,0,2,3,12,14),box(13,0,2,15,12,14),box(0,8,1,16,10,15),box(1,10,2,15,14,14));
            case STATIONS -> box(1,0,6,15,16,10);
            case STATUE -> Shapes.or(box(3,0,3,13,2,13),box(5,2,5,11,11,11),box(6,11,6,10,16,10));
            case TABLE -> Shapes.or(box(0,11,0,16,14,16),box(1,0,1,3,11,3),box(13,0,1,15,11,3),box(1,0,13,3,11,15),box(13,0,13,15,11,15));
            case AMBO -> Shapes.or(box(2,0,2,14,2,14),box(4,2,6,12,12,14),box(1,12,3,15,15,15));
        };
        for (Direction direction : Direction.Plane.HORIZONTAL) shapes.put(direction, rotateShape(base, direction));
    }

    @Override protected MapCodec<? extends ChapelBlock> codec() { return simpleCodec(p -> new ChapelBlock(p, kind)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING,LIT,POWERED,WATER);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c) { return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite()); }
    @Override protected BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING, r.rotate(s.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState s, Mirror m) { return rotate(s, m.getRotation(s.getValue(FACING))); }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return shapes.get(s.getValue(FACING)); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState s, Level l, BlockPos p, Player player, InteractionHand hand, BlockHitResult hit) {
        if (kind==Kind.ALTAR && stack.getItem() instanceof AltarClothItem cloth) {
            if (s.getValue(CLOTH)==cloth.color()) return InteractionResult.SUCCESS;
            if (!l.isClientSide()) {
                ItemStack previous=new ItemStack(ModContent.altarCloth(s.getValue(CLOTH)));
                stack.consume(1,player);
                l.setBlock(p,s.setValue(CLOTH,cloth.color()),3);
                if (!player.hasInfiniteMaterials() && !player.getInventory().add(previous)) player.drop(previous,false);
                l.playSound(null,p,SoundEvents.WOOL_PLACE,SoundSource.BLOCKS,.8F,1F);
            }
            return InteractionResult.SUCCESS;
        }
        if (kind==Kind.FONT && stack.getItem() instanceof AspergillumItem aspergillum) return aspergillum.refill(stack,s,l,p,player);
        if (kind==Kind.WREATH && stack.is(Items.FLINT_AND_STEEL)) {
            if (s.getValue(CANDLES)<4 && !l.isClientSide()) {
                l.setBlock(p,s.setValue(CANDLES,s.getValue(CANDLES)+1),3);
                stack.hurtAndBreak(1,player,hand);
                l.playSound(null,p,SoundEvents.FLINTANDSTEEL_USE,SoundSource.BLOCKS,.8F,1F);
            }
            return InteractionResult.SUCCESS;
        }
        if (kind == Kind.FONT && stack.is(Items.WATER_BUCKET)) {
            if (!l.isClientSide()) {
                l.setBlock(p, s.setValue(WATER, 3), 3);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
                l.playSound(null, p, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, .8F, 1F);
            }
            return InteractionResult.SUCCESS;
        }
        if (kind == Kind.FONT && stack.is(Items.GLASS_BOTTLE)) {
            if (s.getValue(WATER) == 0) {
                if (!l.isClientSide()) player.sendOverlayMessage(Component.translatable("message.catholicraft.empty_font"));
                return InteractionResult.FAIL;
            }
            if (!l.isClientSide()) {
                l.setBlock(p, s.setValue(WATER, s.getValue(WATER) - 1), 3);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(ModContent.HOLY_WATER.get())));
                l.playSound(null, p, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, .8F, 1F);
            }
            return InteractionResult.SUCCESS;
        }
        if ((kind == Kind.CANDLE || kind == Kind.LAMP || kind==Kind.VOTIVE) && stack.is(Items.FLINT_AND_STEEL)) {
            if (!s.getValue(LIT) && !l.isClientSide()) {
                l.setBlock(p, s.setValue(LIT, true), 3);
                stack.hurtAndBreak(1, player, hand);
                l.playSound(null, p, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, .8F, 1F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState s, Level l, BlockPos p, Player player, BlockHitResult hit) {
        if (kind==Kind.PEW && l instanceof ServerLevel server) return PewSeating.sit(server,p,s,player);
        if (kind==Kind.PEW) return InteractionResult.SUCCESS;
        if (kind==Kind.STATIONS) {
            if (!l.isClientSide()) {
                int station=1+Math.floorMod(s.getValue(STATION)-1+(player.isShiftKeyDown() ? -1 : 1),14);
                l.setBlock(p,s.setValue(STATION,station),3);
                player.sendOverlayMessage(Component.translatable("message.catholicraft.station",station,StationNames.TITLES[station-1]));
            }
            return InteractionResult.SUCCESS;
        }
        if (kind==Kind.WREATH) {
            if (!l.isClientSide() && s.getValue(CANDLES)>0) {
                l.setBlock(p,s.setValue(CANDLES,s.getValue(CANDLES)-1),3);
                l.playSound(null,p,SoundEvents.CANDLE_EXTINGUISH,SoundSource.BLOCKS,.8F,1F);
            }
            return InteractionResult.SUCCESS;
        }
        if (kind == Kind.CANDLE || kind == Kind.LAMP || kind==Kind.VOTIVE) {
            if (s.getValue(LIT) && !l.isClientSide()) {
                l.setBlock(p, s.setValue(LIT, false), 3);
                l.playSound(null, p, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, .8F, 1F);
            }
            return InteractionResult.SUCCESS;
        }
        if (kind == Kind.BELL) { ring(l, p); return InteractionResult.SUCCESS; }
        if (kind == Kind.FONT) {
            if (!l.isClientSide()) player.sendOverlayMessage(Component.translatable("message.catholicraft.font_level", s.getValue(WATER)));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void neighborChanged(BlockState s, Level l, BlockPos p, Block block, Orientation orientation, boolean moved) {
        if (kind != Kind.BELL || l.isClientSide()) return;
        boolean power = l.hasNeighborSignal(p);
        if (power != s.getValue(POWERED)) {
            l.setBlock(p, s.setValue(POWERED, power), 3);
            if (power) ring(l, p);
        }
    }

    @Override
    public void animateTick(BlockState s, Level l, BlockPos p, RandomSource r) {
        if (kind==Kind.WREATH) for(int i=0;i<s.getValue(CANDLES);i++) {
            double x=p.getX()+(i%2==0 ? .3 : .7), z=p.getZ()+(i<2 ? .3 : .7);
            l.addParticle(ParticleTypes.FLAME,x,p.getY()+.72,z,0,0,0);
        }
        if (kind==Kind.VOTIVE && s.getValue(LIT)) for(int i=0;i<3;i++) l.addParticle(ParticleTypes.FLAME,p.getX()+.25+i*.25,p.getY()+.88,p.getZ()+.5,0,0,0);
        if ((kind == Kind.CANDLE || kind == Kind.LAMP) && s.getValue(LIT)) {
            l.addParticle(ParticleTypes.FLAME, p.getX() + .5, p.getY() + .92, p.getZ() + .5, 0, 0, 0);
            if (r.nextInt(3) == 0) l.addParticle(ParticleTypes.SMOKE, p.getX() + .5, p.getY() + .95, p.getZ() + .5, 0, .01, 0);
        }
    }

    private static void ring(Level l, BlockPos p) {
        if (l instanceof ServerLevel server) {
            l.playSound(null, p, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2F, .75F);
            server.sendParticles(ParticleTypes.NOTE, p.getX() + .5, p.getY() + 1.2, p.getZ() + .5, 1, 0, 0, 0, 0);
        }
    }

    private static VoxelShape rotateShape(VoxelShape base, Direction direction) {
        int turns = switch (direction) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
        VoxelShape result = base;
        for (int i = 0; i < turns; i++) {
            VoxelShape[] next = {Shapes.empty()};
            result.forAllBoxes((x1, y1, z1, x2, y2, z2) -> next[0] = Shapes.or(next[0], Shapes.box(1-z2, y1, x1, 1-z1, y2, x2)));
            result = next[0];
        }
        return result;
    }

    /** Specialized state definitions avoid multiplying unrelated blocks' possible states. */
    public static final class Altar extends ChapelBlock {
        public Altar(Properties p) { super(p,Kind.ALTAR); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { super.createBlockStateDefinition(b); b.add(CLOTH); }
        @Override protected MapCodec<Altar> codec() { return simpleCodec(Altar::new); }
    }
    public static final class Wreath extends ChapelBlock {
        public Wreath(Properties p) { super(p,Kind.WREATH); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { super.createBlockStateDefinition(b); b.add(CANDLES); }
        @Override protected MapCodec<Wreath> codec() { return simpleCodec(Wreath::new); }
    }
    public static final class Stations extends ChapelBlock {
        public Stations(Properties p) { super(p,Kind.STATIONS); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { super.createBlockStateDefinition(b); b.add(STATION); }
        @Override protected MapCodec<Stations> codec() { return simpleCodec(Stations::new); }
    }
}
