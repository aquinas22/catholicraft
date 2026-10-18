package com.catholicraft.entity;

import com.catholicraft.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Peaceful cathedral residents; one emerald buys the displayed devotional supply. */
public final class MonkEntity extends PathfinderMob {
    private BlockPos home;
    private int offer;
    private long nextTrade;
    public MonkEntity(EntityType<? extends MonkEntity> type, Level level) {
        super(type, level); setPersistenceRequired(); getNavigation().setCanOpenDoors(true); getNavigation().setCanFloat(true);
    }
    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, .25).add(Attributes.FOLLOW_RANGE, 24);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 10, 1, 1.2));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, .6));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }
    public ItemStack currentOffer() {
        return switch (offer) {
            case 1 -> new ItemStack(ModContent.INCENSE.get(), 8);
            case 2 -> new ItemStack(ModContent.ROSARY.get());
            case 3 -> new ItemStack(ModContent.ALTAR_BREAD.get(), 12);
            case 4 -> new ItemStack(ModContent.CHANT_VENI_CREATOR.get());
            case 5 -> new ItemStack(ModContent.WINE_CRUET.get(), 4);
            default -> new ItemStack(ModContent.PRAYER_BOOK.get());
        };
    }
    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!(level() instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) {
            offer = (offer + 1) % 6;
        } else if (player.getItemInHand(hand).is(Items.EMERALD)) {
            if (server.getGameTime() < nextTrade) return InteractionResult.SUCCESS;
            ItemStack result = currentOffer(); player.getItemInHand(hand).consume(1, player);
            if (!player.getInventory().add(result)) player.drop(result, false);
            nextTrade = server.getGameTime() + 20;
            playSound(SoundEvents.VILLAGER_YES, .8F, .9F);
        }
        player.sendSystemMessage(Component.translatable("message.catholicraft.monk_offer", currentOffer().getCount(), currentOffer().getHoverName()));
        return InteractionResult.SUCCESS;
    }
    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (home == null) home = blockPosition();
        if (tickCount % 100 == 0 && blockPosition().distSqr(home) > 18 * 18)
            getNavigation().moveTo(home.getX() + .5, home.getY(), home.getZ() + .5, .8);
    }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.VILLAGER_AMBIENT; }
    @Override protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out); out.putInt("Offer", offer);
        if (home != null) out.putLong("MonasteryHome", home.asLong());
    }
    @Override protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in); offer = Math.clamp(in.getIntOr("Offer", 0), 0, 5);
        home = in.getLong("MonasteryHome").map(BlockPos::of).orElse(null); nextTrade = 0;
    }
}
