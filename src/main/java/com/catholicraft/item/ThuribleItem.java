package com.catholicraft.item;

import com.catholicraft.registry.ModContent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ThuribleItem extends Item {
    public ThuribleItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel server) {
            ItemStack incense = ItemStack.EMPTY;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack candidate = player.getInventory().getItem(i);
                if (candidate.is(ModContent.INCENSE.get())) { incense = candidate; break; }
            }
            if (incense.isEmpty() && !player.hasInfiniteMaterials()) {
                player.sendOverlayMessage(Component.translatable("message.catholicraft.need_incense"));
                return InteractionResult.FAIL;
            }
            incense.consume(1, player);
            server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 1, player.getZ(), 18, .4, .3, .4, .025);
            level.playSound(null, player.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, .6F, .8F);
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 40);
        }
        return InteractionResult.SUCCESS;
    }
}
