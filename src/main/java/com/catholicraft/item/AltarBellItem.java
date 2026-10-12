package com.catholicraft.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class AltarBellItem extends Item {
    public AltarBellItem(Properties p) { super(p); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel server) {
            level.playSound(null,player.blockPosition(),SoundEvents.BELL_BLOCK,SoundSource.PLAYERS,.8F,1.65F);
            server.sendParticles(ParticleTypes.NOTE,player.getX(),player.getY()+1.8,player.getZ(),1,0,0,0,0);
            player.getCooldowns().addCooldown(player.getItemInHand(hand),15);
        }
        return InteractionResult.SUCCESS;
    }
}
