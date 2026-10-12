package com.catholicraft.world;

import com.catholicraft.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Vanilla marker vehicles keep seating compatible with unmodified player rendering. */
public final class PewSeating {
    private static final String TAG="catholicraft.pew_seat";
    public static InteractionResult sit(ServerLevel level,BlockPos pos,BlockState state,Player player) {
        if (player.isPassenger() || player.isShiftKeyDown()) return InteractionResult.PASS;
        for (var seat : level.getEntitiesOfClass(ArmorStand.class,new AABB(pos).inflate(.5))) {
            if (seat.entityTags().contains(TAG) && !seat.getPassengers().isEmpty()) {
                player.sendOverlayMessage(Component.translatable("message.catholicraft.pew_occupied"));
                return InteractionResult.FAIL;
            }
        }
        var seat=new ArmorStand(level,pos.getX()+.5,pos.getY()+.4,pos.getZ()+.5);
        var data=new CompoundTag();
        data.putBoolean("Marker",true);
        data.putBoolean("Small",true);
        seat.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),data));
        seat.setPos(pos.getX()+.5,pos.getY()+.4,pos.getZ()+.5);
        seat.setInvisible(true);
        seat.setInvulnerable(true);
        seat.setNoGravity(true);
        seat.setSilent(true);
        seat.addTag(TAG);
        seat.getPersistentData().putLong("PewPos",pos.asLong());
        seat.setYRot(state.getValue(com.catholicraft.block.ChapelBlock.FACING).toYRot());
        if (!level.addFreshEntity(seat) || !player.startRiding(seat,true,false)) {
            seat.discard();
            return InteractionResult.FAIL;
        }
        player.sendOverlayMessage(Component.translatable("message.catholicraft.pew_sit"));
        return InteractionResult.SUCCESS;
    }
    public static void tick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof ArmorStand seat && seat.level() instanceof ServerLevel && seat.entityTags().contains(TAG)) cleanup(seat);
    }
    public static void cleanup(ArmorStand seat) {
        if (seat.getPassengers().isEmpty() || !seat.level().getBlockState(BlockPos.of(seat.getPersistentData().getLongOr("PewPos",0))).is(ModContent.PEW.get())) seat.discard();
    }
    private PewSeating() {}
}
