package com.catholicraft.item;

import com.catholicraft.block.ChapelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class AspergillumItem extends Item {
    public static final int CAPACITY = 8;
    public AspergillumItem(Properties p) { super(p); }
    public static int charges(ItemStack stack) {
        return Math.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("Charges",0),0,CAPACITY);
    }
    public static void setCharges(ItemStack stack, int value) {
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("Charges",Math.clamp(value,0,CAPACITY));
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }
    public InteractionResult refill(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
        if (charges(stack) == CAPACITY) return InteractionResult.SUCCESS;
        if (state.getValue(ChapelBlock.WATER) == 0) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable("message.catholicraft.empty_font"));
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            setCharges(stack,CAPACITY);
            level.setBlock(pos,state.setValue(ChapelBlock.WATER,state.getValue(ChapelBlock.WATER)-1),3);
            level.playSound(null,pos,SoundEvents.BOTTLE_FILL,SoundSource.PLAYERS,.6F,1.2F);
            player.sendOverlayMessage(Component.translatable("message.catholicraft.aspergillum_charges",CAPACITY));
        }
        return InteractionResult.SUCCESS;
    }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if (charges(stack)==0) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable("message.catholicraft.empty_aspergillum"));
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel server) {
            HolyWaterItem.cleanse(player);
            for (Player nearby : level.getEntitiesOfClass(Player.class,player.getBoundingBox().inflate(3))) {
                if (nearby.distanceToSqr(player)<=9) HolyWaterItem.cleanse(nearby);
            }
            if (!player.hasInfiniteMaterials()) setCharges(stack,charges(stack)-1);
            server.sendParticles(ParticleTypes.SPLASH,player.getX(),player.getY()+1,player.getZ(),32,1.5,.5,1.5,.08);
            level.playSound(null,player.blockPosition(),SoundEvents.BOTTLE_EMPTY,SoundSource.PLAYERS,.7F,1.3F);
            player.getCooldowns().addCooldown(stack,30);
            player.sendOverlayMessage(Component.translatable("message.catholicraft.aspergillum_charges",charges(stack)));
        }
        return InteractionResult.SUCCESS;
    }
    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13F*charges(stack)/CAPACITY); }
    @Override public int getBarColor(ItemStack stack) { return 0x5FADD0; }
}
