package com.catholicraft.world;

import com.catholicraft.CatholiCraft;
import com.catholicraft.registry.ModContent;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public final class CathedralCommands {
    public static Optional<BlockPos> firstObstruction(ServerLevel level, BlockPos origin) {
        for (BlockPos relative : BlockPos.betweenClosed(0, 0, 0, 40, 24, 28)) {
            BlockPos pos = origin.offset(relative);
            if (!level.isInWorldBounds(pos) || !level.hasChunkAt(pos) || !level.isEmptyBlock(pos)) return Optional.of(pos.immutable());
        }
        return Optional.empty();
    }
    public static boolean place(ServerLevel level, BlockPos origin) {
        var template = level.getStructureManager().get(CatholiCraft.id("cathedral"));
        return template.isPresent() && template.get().placeInWorld(level, origin, origin,
            new StructurePlaceSettings().setIgnoreEntities(false), level.getRandom(), 3);
    }
    public static int build(net.minecraft.commands.CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        BlockPos origin = player.blockPosition().offset(3, 0, -14);
        var obstruction = firstObstruction(source.getLevel(), origin);
        if (obstruction.isPresent()) {
            var pos = obstruction.get(); source.sendFailure(Component.translatable("message.catholicraft.cathedral_blocked", pos.getX(), pos.getY(), pos.getZ())); return 0;
        }
        if (!place(source.getLevel(), origin)) return 0;
        source.sendSuccess(() -> Component.translatable("message.catholicraft.cathedral_built", origin.getX(), origin.getY(), origin.getZ()), false); return 1;
    }
    public static int monk(net.minecraft.commands.CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var monk = ModContent.MONK.get().spawn(source.getLevel(), player.blockPosition(), EntitySpawnReason.COMMAND);
        if (monk == null) return 0;
        source.sendSuccess(() -> Component.translatable("message.catholicraft.monk_spawned"), false); return 1;
    }
    private CathedralCommands() {}
}
