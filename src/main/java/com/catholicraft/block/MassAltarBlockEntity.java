package com.catholicraft.block;

import com.catholicraft.registry.ModContent;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** A shared, persistent roleplay service controlled by the player who started it. */
public final class MassAltarBlockEntity extends BlockEntity {
    public record Step(String title, String instruction) {}
    public static final Step[] TRADITIONAL = {
        new Step("Prayers at the foot of the altar", "Begin with the Sign of the Cross and the preparatory prayers."),
        new Step("Introit and Kyrie", "Read the entrance antiphon; Kyrie eleison, Christe eleison, Kyrie eleison."),
        new Step("Gloria and Collect", "Say the Gloria when appointed, then the Collect of the day."),
        new Step("Epistle and Gradual", "Proclaim the Epistle and the Gradual or Tract from your chosen readings."),
        new Step("Gospel", "Proclaim the Gospel. The congregation stands."),
        new Step("Homily and Credo", "Give a reflection and profess the Creed when appointed."),
        new Step("Offertory", "Present the bread and wine props. One altar bread and one wine cruet are used."),
        new Step("Lavabo and Orate fratres", "Represent the washing of hands, then invite the assembly to prayer."),
        new Step("Secret and Preface", "Pray the Secret and the Preface appropriate to the day."),
        new Step("Sanctus", "Sanctus, Sanctus, Sanctus. The congregation kneels."),
        new Step("Roman Canon", "Follow the Canon in your missal. Ring the altar bell at the represented elevations."),
        new Step("Pater noster and Agnus Dei", "Pray the Lord's Prayer, then Agnus Dei."),
        new Step("Communion", "Represent the Communion procession with the congregation."),
        new Step("Postcommunion and dismissal", "Pray the Postcommunion, dismissal and final blessing."),
        new Step("Last Gospel", "Read the opening of John's Gospel; conclude in thanksgiving.")
    };
    public static final Step[] MODERN = {
        new Step("Entrance and greeting", "Gather, make the Sign of the Cross and greet the assembly."),
        new Step("Penitential Act and Kyrie", "Invite recollection; Kyrie eleison, Christe eleison, Kyrie eleison."),
        new Step("Gloria and Collect", "Say the Gloria when appointed and the Collect of the day."),
        new Step("Readings and psalm", "Proclaim the readings and responsorial psalm at the ambo."),
        new Step("Gospel and homily", "Stand for the Gospel; offer a reflection on the readings."),
        new Step("Creed and Universal Prayer", "Profess the Creed when appointed and offer the intercessions."),
        new Step("Preparation of the gifts", "Present the bread and wine props. One altar bread and one wine cruet are used."),
        new Step("Preface and Sanctus", "Pray the Preface and Sanctus; invite the congregation to kneel."),
        new Step("Eucharistic Prayer", "Follow your chosen Eucharistic Prayer; ring the altar bell at the represented elevations."),
        new Step("Lord's Prayer and sign of peace", "Pray the Lord's Prayer and exchange the sign of peace."),
        new Step("Agnus Dei and Communion", "Pray Agnus Dei and represent the Communion procession."),
        new Step("Prayer after Communion", "Offer the concluding prayer in thanksgiving."),
        new Step("Blessing and dismissal", "Offer the blessing and send the assembly forth.")
    };
    private int step = -1;
    private boolean traditional;
    private String celebrant = "";
    private long lastAdvance = -20;

    public MassAltarBlockEntity(BlockPos pos, BlockState state) { super(ModContent.MASS_ALTAR_ENTITY.get(), pos, state); }
    public int stepIndex() { return step; }
    public boolean isTraditional() { return traditional; }
    public boolean isActive() { return step >= 0; }
    public Step[] steps() { return traditional ? TRADITIONAL : MODERN; }

    public InteractionResult interact(Player player, ItemStack missal) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (isActive() && !celebrant.equals(player.getUUID().toString())) {
            player.sendSystemMessage(Component.translatable("message.catholicraft.mass_owned"));
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            if (isActive()) { step = -1; celebrant = ""; setChanged(); broadcast(server, Component.translatable("message.catholicraft.mass_cancelled")); }
            else showStatus(player);
            return InteractionResult.SUCCESS;
        }
        // Debounce each shared altar, including its final step, to prevent duplicate gifts or sessions.
        if (server.getGameTime() - lastAdvance < 10) return InteractionResult.SUCCESS;
        String missing = missingPreparation(server, player);
        if (!missing.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.catholicraft.mass_missing", missing));
            return InteractionResult.SUCCESS;
        }
        if (!isActive()) {
            traditional = missal.is(ModContent.MISSAL_1962.get());
            celebrant = player.getUUID().toString();
            step = 0;
        } else if (step + 1 == steps().length) {
            step = -1; celebrant = ""; lastAdvance = server.getGameTime(); setChanged();
            broadcast(server, Component.translatable("message.catholicraft.mass_complete"));
            return InteractionResult.SUCCESS;
        } else {
            // Consume both props together only when entering the offertory; failed checks change nothing.
            if (step + 1 == 6 && !player.hasInfiniteMaterials()) {
                if (!has(player, ModContent.ALTAR_BREAD.get()) || !has(player, ModContent.WINE_CRUET.get())) {
                    player.sendSystemMessage(Component.translatable("message.catholicraft.mass_gifts"));
                    return InteractionResult.SUCCESS;
                }
                take(player, ModContent.ALTAR_BREAD.get());
                take(player, ModContent.WINE_CRUET.get());
                ItemStack bottle = new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE);
                if (!player.getInventory().add(bottle)) player.drop(bottle, false);
            }
            step++;
        }
        lastAdvance = server.getGameTime(); setChanged();
        Step current = steps()[step];
        broadcast(server, Component.translatable("message.catholicraft.mass_step", traditional ? "1962" : "Modern", step + 1, steps().length, current.title(), current.instruction()));
        if (current.title().equals("Roman Canon") || current.title().equals("Eucharistic Prayer"))
            server.playSound(null, worldPosition, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, .6F, 1.6F);
        return InteractionResult.SUCCESS;
    }

    public void showStatus(Player player) {
        player.sendSystemMessage(isActive()
            ? Component.translatable("message.catholicraft.mass_step", traditional ? "1962" : "Modern", step + 1, steps().length, steps()[step].title(), steps()[step].instruction())
            : Component.translatable("message.catholicraft.mass_ready"));
    }
    public String missingPreparation(ServerLevel server, Player player) {
        var worn = player.getItemBySlot(EquipmentSlot.CHEST);
        var id = BuiltInRegistries.ITEM.getKey(worn.getItem());
        if (!id.getNamespace().equals("catholicraft") || !id.getPath().endsWith("_chasuble") || !player.getItemBySlot(EquipmentSlot.LEGS).is(ModContent.ALB.get())) return "wear an alb and chasuble";
        boolean crucifix = false, chalice = false, ambo = false; int candles = 0;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-7, -2, -7), worldPosition.offset(7, 4, 7))) {
            if (!server.hasChunkAt(pos)) continue;
            BlockState state = server.getBlockState(pos);
            crucifix |= state.is(ModContent.CRUCIFIX.get());
            chalice |= state.is(ModContent.CHALICE.get());
            ambo |= state.is(ModContent.AMBO.get()) || state.is(net.minecraft.world.level.block.Blocks.LECTERN);
            if (state.is(ModContent.ALTAR_CANDLE.get()) && state.getValue(ChapelBlock.LIT)) candles++;
        }
        if (!crucifix) return "a crucifix within seven blocks";
        if (!chalice) return "a chalice within seven blocks";
        if (candles < 2) return "two lit altar candles within seven blocks";
        if (!ambo) return "an ambo or lectern within seven blocks";
        return "";
    }
    private void broadcast(ServerLevel server, Component message) {
        for (Player listener : server.players()) if (listener.blockPosition().distSqr(worldPosition) <= 32 * 32) listener.sendSystemMessage(message);
    }
    private static boolean has(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) if (player.getInventory().getItem(i).is(item)) return true;
        return false;
    }
    private static void take(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) { stack.shrink(1); player.getInventory().setChanged(); return; }
        }
    }
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out); out.putInt("MassStep", step); out.putBoolean("Traditional", traditional); out.putString("Celebrant", celebrant);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in); traditional = in.getBooleanOr("Traditional", false);
        step = Math.clamp(in.getIntOr("MassStep", -1), -1, steps().length - 1);
        celebrant = in.getStringOr("Celebrant", "");
        try { if (isActive()) UUID.fromString(celebrant); } catch (IllegalArgumentException e) { step = -1; celebrant = ""; }
        lastAdvance = -20;
    }
}
