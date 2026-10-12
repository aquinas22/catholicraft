package com.catholicraft.world;

import com.catholicraft.block.ChapelBlock;
import com.catholicraft.registry.ModContent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class ChapelCommands {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("catholicraft")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("kit").executes(context -> {
                var player = context.getSource().getPlayerOrException();
                for (var holder : ModContent.ITEMS.getEntries()) {
                    ItemStack stack = new ItemStack(holder.get(), holder == ModContent.INCENSE ? 16 : 1);
                    if (!player.getInventory().add(stack)) player.drop(stack, false);
                }
                context.getSource().sendSuccess(() -> Component.translatable("message.catholicraft.kit"), false);
                return 1;
            }))
            .then(Commands.literal("chapel").executes(context -> {
                var player = context.getSource().getPlayerOrException();
                ServerLevel level = context.getSource().getLevel();
                BlockPos origin = player.blockPosition().offset(3, 0, -5);
                var obstruction = firstObstruction(level, origin);
                if (obstruction.isPresent()) {
                    BlockPos pos = obstruction.get();
                    context.getSource().sendFailure(Component.translatable("message.catholicraft.chapel_blocked", pos.getX(), pos.getY(), pos.getZ()));
                    return 0;
                }
                place(level,origin);
                context.getSource().sendSuccess(() -> Component.translatable("message.catholicraft.chapel_built", origin.getX(), origin.getY(), origin.getZ()), false);
                return 1;
            })));
    }

    /** Check the whole occupied volume, including the interior, before any placement. */
    public static Optional<BlockPos> firstObstruction(ServerLevel level, BlockPos origin) {
        for (BlockPos relative : BlockPos.betweenClosed(0, 0, 0, 14, 7, 10)) {
            BlockPos pos = origin.offset(relative);
            if (!level.isInWorldBounds(pos) || !level.hasChunkAt(pos) || !level.isEmptyBlock(pos)) return Optional.of(pos);
        }
        return Optional.empty();
    }

    public static void place(ServerLevel level,BlockPos origin) {
        for (var entry : blueprint().entrySet()) level.setBlock(origin.offset(entry.getKey()), entry.getValue(), 3);
        net.minecraft.world.level.block.LecternBlock.tryPlaceBook(null,level,origin.offset(11,1,2),level.getBlockState(origin.offset(11,1,2)),new ItemStack(ModContent.MISSAL_1962.get()));
        net.minecraft.world.level.block.LecternBlock.tryPlaceBook(null,level,origin.offset(11,1,3),level.getBlockState(origin.offset(11,1,3)),new ItemStack(ModContent.MODERN_MISSAL.get()));
        if (level.getBlockEntity(origin.offset(2,1,2)) instanceof com.catholicraft.block.TabernacleBlockEntity cabinet) {
            cabinet.setItem(0,new ItemStack(ModContent.PRAYER_BOOK.get()));
            cabinet.setItem(1,new ItemStack(ModContent.STATIONS_BOOK.get()));
            cabinet.setItem(2,new ItemStack(ModContent.INCENSE.get(),16));
            cabinet.setItem(3,new ItemStack(ModContent.ROSARY.get()));
            cabinet.setItem(4,new ItemStack(ModContent.ASPERGILLUM.get()));
        }
    }

    /** An east-facing 15 x 11 chapel, expressed as relative positions for reuse and verification. */
    public static Map<BlockPos, BlockState> blueprint() {
        Map<BlockPos, BlockState> plan = new LinkedHashMap<>();
        for (int x = 0; x < 15; x++) for (int z = 0; z < 11; z++) {
            plan.put(new BlockPos(x, 0, z), ((x + z) % 2 == 0 ? Blocks.SMOOTH_QUARTZ : Blocks.POLISHED_DIORITE).defaultBlockState());
            if (x == 0 || x == 14 || z == 0 || z == 10) for (int y = 1; y < 5; y++) {
                if (x == 0 && z == 5 && y <= 3) continue;
                boolean window = (z == 0 || z == 10) && x >= 3 && x <= 11 && x % 4 == 3 && (y == 2 || y == 3);
                plan.put(new BlockPos(x, y, z), (window ? ModContent.STAINED_GLASS.get() : Blocks.STONE_BRICKS).defaultBlockState());
            }
            int roofY = 5 + Math.min(Math.min(z, 10-z) / 2, 2);
            plan.put(new BlockPos(x, roofY, z), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            if (x == 0 || x == 14) for (int y = 5; y < roofY; y++) plan.put(new BlockPos(x, y, z), Blocks.STONE_BRICKS.defaultBlockState());
        }
        for (int x = 3; x <= 7; x += 2) for (int z : new int[]{2, 3, 7, 8}) {
            plan.put(new BlockPos(x, 1, z), ModContent.PEW.get().defaultBlockState().setValue(ChapelBlock.FACING, Direction.EAST));
        }
        plan.put(new BlockPos(10, 1, 5), ModContent.KNEELER.get().defaultBlockState().setValue(ChapelBlock.FACING, Direction.EAST));
        for (int z = 4; z <= 6; z++) plan.put(new BlockPos(12, 1, z), ModContent.ALTAR.get().defaultBlockState().setValue(ChapelBlock.FACING, Direction.WEST));
        plan.put(new BlockPos(12, 2, 4), ModContent.ALTAR_CANDLE.get().defaultBlockState().setValue(ChapelBlock.LIT, true));
        plan.put(new BlockPos(12, 2, 6), ModContent.ALTAR_CANDLE.get().defaultBlockState().setValue(ChapelBlock.LIT, true));
        plan.put(new BlockPos(12, 2, 5), ModContent.CHALICE.get().defaultBlockState());
        plan.put(new BlockPos(13, 1, 5), Blocks.SMOOTH_QUARTZ.defaultBlockState());
        plan.put(new BlockPos(13, 2, 5), ModContent.TABERNACLE.get().defaultBlockState().setValue(com.catholicraft.block.TabernacleBlock.FACING, Direction.WEST));
        plan.put(new BlockPos(13, 3, 5), ModContent.CRUCIFIX.get().defaultBlockState().setValue(ChapelBlock.FACING, Direction.WEST));
        plan.put(new BlockPos(13, 2, 6), ModContent.SANCTUARY_LAMP.get().defaultBlockState().setValue(ChapelBlock.LIT, true));
        plan.put(new BlockPos(1, 1, 4), ModContent.HOLY_WATER_FONT.get().defaultBlockState().setValue(ChapelBlock.WATER, 3));
        plan.put(new BlockPos(2, 1, 8), ModContent.BAPTISMAL_FONT.get().defaultBlockState().setValue(ChapelBlock.WATER, 3));
        plan.put(new BlockPos(11, 1, 2), Blocks.LECTERN.defaultBlockState());
        plan.put(new BlockPos(1, 1, 8), ModContent.CHURCH_BELL.get().defaultBlockState());
        plan.put(new BlockPos(2,1,2),ModContent.SACRISTY_CABINET.get().defaultBlockState());
        plan.put(new BlockPos(1,1,2),ModContent.VOTIVE_RACK.get().defaultBlockState().setValue(ChapelBlock.LIT,true));
        plan.put(new BlockPos(10,1,2),ModContent.ADVENT_WREATH.get().defaultBlockState());
        plan.put(new BlockPos(10,1,8),ModContent.INCENSE_BURNER.get().defaultBlockState());
        plan.put(new BlockPos(11,1,8),ModContent.AMBO.get().defaultBlockState().setValue(ChapelBlock.FACING,Direction.WEST));
        plan.put(new BlockPos(11,1,3),Blocks.LECTERN.defaultBlockState());
        plan.put(new BlockPos(11,1,7),ModContent.CREDENCE_TABLE.get().defaultBlockState());
        plan.put(new BlockPos(11,2,7),ModContent.CIBORIUM.get().defaultBlockState());
        plan.put(new BlockPos(13,1,2),Blocks.SMOOTH_QUARTZ.defaultBlockState());
        plan.put(new BlockPos(13,2,2),ModContent.OUR_LADY_STATUE.get().defaultBlockState().setValue(ChapelBlock.FACING,Direction.WEST));
        plan.put(new BlockPos(13,1,8),Blocks.SMOOTH_QUARTZ.defaultBlockState());
        plan.put(new BlockPos(13,2,8),ModContent.ST_JOSEPH_STATUE.get().defaultBlockState().setValue(ChapelBlock.FACING,Direction.WEST));
        for(int i=0;i<14;i++) plan.put(new BlockPos(3+i%7,2,i<7 ? 1 : 9),ModContent.STATIONS.get().defaultBlockState()
            .setValue(ChapelBlock.STATION,i+1).setValue(ChapelBlock.FACING,i<7 ? Direction.SOUTH : Direction.NORTH));
        return plan;
    }

    private ChapelCommands() {}
}
