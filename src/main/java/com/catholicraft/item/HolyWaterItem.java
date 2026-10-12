package com.catholicraft.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class HolyWaterItem extends Item {
    public HolyWaterItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel server) {
            cleanse(player);
            server.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 1, player.getZ(), 16, .3, .4, .3, .05);
            level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, .6F, 1F);
            ItemStack stack = player.getItemInHand(hand);
            player.getCooldowns().addCooldown(stack, 40);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
        }
        return InteractionResult.SUCCESS;
    }

    public static void cleanse(LivingEntity entity) {
        entity.clearFire();
        entity.removeEffect(MobEffects.POISON);
        entity.removeEffect(MobEffects.WEAKNESS);
        entity.removeEffect(MobEffects.SLOWNESS);
        entity.removeEffect(MobEffects.NAUSEA);
    }
}
