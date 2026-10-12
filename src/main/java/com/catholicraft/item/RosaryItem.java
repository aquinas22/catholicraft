package com.catholicraft.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public final class RosaryItem extends Item {
    private static final String[][] MYSTERIES = {
        {"Joyful", "The Annunciation", "The Visitation", "The Nativity", "The Presentation", "The Finding in the Temple"},
        {"Sorrowful", "The Agony in the Garden", "The Scourging at the Pillar", "The Crowning with Thorns", "The Carrying of the Cross", "The Crucifixion"},
        {"Glorious", "The Resurrection", "The Ascension", "The Descent of the Holy Spirit", "The Assumption", "The Coronation of Mary"},
        {"Luminous", "The Baptism of Jesus", "The Wedding at Cana", "The Proclamation of the Kingdom", "The Transfiguration", "The Institution of the Eucharist"}
    };

    public RosaryItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            int set = Math.floorMod(data.getIntOr("MysterySet", 0), MYSTERIES.length);
            int index = Math.floorMod(data.getIntOr("MysteryIndex", 0), 5);
            if (player.isShiftKeyDown()) { set = (set + 1) % MYSTERIES.length; index = 0; }
            player.sendOverlayMessage(Component.translatable("message.catholicraft.rosary", MYSTERIES[set][0], index + 1, MYSTERIES[set][index + 1]));
            data.putInt("MysterySet", set);
            data.putInt("MysteryIndex", player.isShiftKeyDown() ? 0 : (index + 1) % 5);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            player.getCooldowns().addCooldown(stack, 10);
        }
        return InteractionResult.SUCCESS;
    }
}
