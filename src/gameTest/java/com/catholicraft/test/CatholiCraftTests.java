package com.catholicraft.test;

import com.catholicraft.CatholiCraft;
import com.catholicraft.block.MassAltarBlockEntity;
import com.catholicraft.entity.MonkEntity;
import com.catholicraft.world.CathedralCommands;
import com.catholicraft.block.ChapelBlock;
import com.catholicraft.block.TabernacleBlock;
import com.catholicraft.block.IncenseBurnerBlock;
import com.catholicraft.block.IncenseBurnerBlockEntity;
import com.catholicraft.item.AspergillumItem;
import com.catholicraft.world.PewSeating;
import com.catholicraft.block.TabernacleBlockEntity;
import com.catholicraft.registry.ModContent;
import com.catholicraft.world.ChapelCommands;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.BuiltinTestFunctions;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

/** Runs against real Minecraft blocks, containers, recipes, and item components. */
@Mod("catholicraft_tests")
public final class CatholiCraftTests {
    private static final BlockPos POS = new BlockPos(1, 1, 1);
    public CatholiCraftTests(IEventBus bus) { bus.addListener(CatholiCraftTests::register); }

    private static void register(RegisterGameTestsEvent e) {
        var environment = e.registerEnvironment(id("default"));
        expandedTests(e,environment);
        test(e, "tabernacle_save_load", h -> {
            h.setBlock(POS, ModContent.TABERNACLE.get());
            var tab = h.getBlockEntity(POS, TabernacleBlockEntity.class);
            for (int slot = 0; slot < 9; slot++) tab.setItem(slot, new ItemStack(ModContent.INCENSE.get(), slot+1));
            var saved = tab.saveWithFullMetadata(h.getLevel().registryAccess());
            var loaded = (TabernacleBlockEntity) BlockEntity.loadStatic(h.absolutePos(POS), h.getBlockState(POS), saved, h.getLevel().registryAccess());
            h.assertTrue(loaded != null, "Tabernacle must deserialize");
            for (int slot = 0; slot < 9; slot++) {
                h.assertTrue(loaded.getItem(slot).is(ModContent.INCENSE.get()), "Saved item must survive");
                h.assertValueEqual(loaded.getItem(slot).getCount(), slot+1, "saved stack count");
            }
            h.succeed();
        }, new TestData<>(environment, id("empty"), 100, 1, true));
        test(e, "tabernacle_break_drops_contents", h -> {
            h.setBlock(POS, ModContent.TABERNACLE.get());
            var tab = h.getBlockEntity(POS, TabernacleBlockEntity.class);
            tab.setItem(0, new ItemStack(ModContent.THURIBLE.get()));
            tab.setItem(8, new ItemStack(ModContent.INCENSE.get(),16));
            h.getLevel().destroyBlock(h.absolutePos(POS), true);
            h.assertItemEntityCountIs(ModContent.THURIBLE.get(), POS, 2, 1);
            h.assertItemEntityCountIs(ModContent.INCENSE.get(), POS, 2, 16);
            h.assertItemEntityPresent(ModContent.TABERNACLE.get().asItem(), POS, 2);
            h.succeed();
        }, new TestData<>(environment, id("empty"), 100, 1, true));
        test(e, "tabernacle_multiple_openers", h -> {
            h.setBlock(POS, ModContent.TABERNACLE.get());
            var tab = h.getBlockEntity(POS, TabernacleBlockEntity.class);
            var first=h.makeMockPlayer(GameType.SURVIVAL);
            var second=h.makeMockPlayer(GameType.SURVIVAL);
            AbstractContainerMenu menu1=tab.createMenu(1,first.getInventory(),first);
            first.containerMenu=menu1;
            AbstractContainerMenu menu2=tab.createMenu(2,second.getInventory(),second);
            second.containerMenu=menu2;
            h.assertTrue(h.getBlockState(POS).getValue(TabernacleBlock.OPEN),"Door opens with users");
            menu1.removed(first);
            h.assertTrue(h.getBlockState(POS).getValue(TabernacleBlock.OPEN),"Door stays open for second user");
            menu2.removed(second);
            h.assertTrue(!h.getBlockState(POS).getValue(TabernacleBlock.OPEN),"Last user closes door");
            h.succeed();
        }, new TestData<>(environment, id("empty"), 100, 1, true));
        test(e, "font_refill_collect_and_empty", h -> {
            var player=h.makeMockPlayer(GameType.SURVIVAL);
            h.setBlock(POS, ModContent.HOLY_WATER_FONT.get());
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.WATER_BUCKET));
            h.useBlock(POS,player);
            h.assertValueEqual(h.getBlockState(POS).getValue(ChapelBlock.WATER),3,"font charges");
            h.assertTrue(player.getMainHandItem().is(Items.BUCKET),"Refilling returns a bucket");
            for (int i=0;i<3;i++) {
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GLASS_BOTTLE));
                h.useBlock(POS,player);
                h.assertTrue(player.getMainHandItem().is(ModContent.HOLY_WATER.get()),"Bottles fill with holy water");
            }
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GLASS_BOTTLE));
            h.useBlock(POS,player);
            h.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE),"Empty font does not consume bottles");
            h.assertValueEqual(h.getBlockState(POS).getValue(ChapelBlock.WATER),0,"empty font charges");
            h.succeed();
        }, new TestData<>(environment, id("empty"), 100, 1, true));
        test(e, "holy_water_effects_and_container", h -> {
            var player=h.makeMockPlayer(GameType.SURVIVAL);
            player.igniteForSeconds(30);
            for (var effect : List.of(MobEffects.POISON,MobEffects.WEAKNESS,MobEffects.SLOWNESS,MobEffects.NAUSEA)) player.addEffect(new MobEffectInstance(effect,600));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED,600));
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.HOLY_WATER.get()));
            ModContent.HOLY_WATER.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
            h.assertTrue(!player.isOnFire(),"Holy water extinguishes fire");
            for (var effect : List.of(MobEffects.POISON,MobEffects.WEAKNESS,MobEffects.SLOWNESS,MobEffects.NAUSEA)) h.assertTrue(!player.hasEffect(effect),"Holy water removes targeted effects");
            h.assertTrue(player.hasEffect(MobEffects.SPEED),"Beneficial effects remain");
            h.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE),"Holy water returns its bottle");
            h.assertTrue(player.getCooldowns().isOnCooldown(new ItemStack(ModContent.HOLY_WATER.get())),"Cooldown belongs to holy water");
            h.succeed();
        }, new TestData<>(environment, id("empty"), 100, 1, true));
        test(e, "books_and_lecterns", h -> {
            for (var book:List.of(ModContent.MISSAL_1962,ModContent.MODERN_MISSAL,ModContent.FIELD_GUIDE,ModContent.PRAYER_BOOK,ModContent.STATIONS_BOOK)) {
                h.setBlock(POS,Blocks.LECTERN);
                var player=h.makeMockPlayer(GameType.SURVIVAL);
                var stack=new ItemStack(book.get());
                var contents=stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
                h.assertTrue(contents!=null && contents.pages().size()>=8,"Every book has readable pages");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                h.useBlock(POS,player);
                h.assertTrue(h.getBlockEntity(POS,LecternBlockEntity.class).getBook().is(book.get()),"Missal must be accepted by a lectern");
            }
            h.succeed();
        }, new TestData<>(environment, id("empty"), 100, 1, true));
        test(e, "vestments_and_medals_equip", h -> {
            for (var holder:ModContent.ITEMS.getEntries()) {
                var stack=new ItemStack(holder.get());
                var equippable=stack.get(DataComponents.EQUIPPABLE);
                if (equippable==null) continue;
                var player=h.makeMockPlayer(GameType.SURVIVAL);
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                stack.getItem().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
                h.assertTrue(player.getItemBySlot(equippable.slot()).is(holder.get()),"Item equips to its declared slot: "+holder.getId());
            }
            h.succeed();
        }, new TestData<>(environment, id("empty"), 100, 1, true));
        test(e, "lights_and_redstone_bell", h -> {
            var player=h.makeMockPlayer(GameType.SURVIVAL);
            for (var holder:List.of(ModContent.ALTAR_CANDLE,ModContent.SANCTUARY_LAMP)) {
                h.setBlock(POS,holder.get());
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.FLINT_AND_STEEL));
                h.useBlock(POS,player);
                h.assertTrue(h.getBlockState(POS).getValue(ChapelBlock.LIT),"Light ignites");
                h.assertTrue(h.getBlockState(POS).getLightEmission(h.getLevel(),h.absolutePos(POS))>=12,"Lit furnishing emits light");
                player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                h.useBlock(POS,player);
                h.assertTrue(!h.getBlockState(POS).getValue(ChapelBlock.LIT),"Empty hand extinguishes");
            }
            h.setBlock(POS,ModContent.CHURCH_BELL.get());
            h.setBlock(POS.east(),Blocks.REDSTONE_BLOCK);
            h.assertTrue(h.getBlockState(POS).getValue(ChapelBlock.POWERED),"Bell responds to rising redstone");
            h.setBlock(POS.east(),Blocks.AIR);
            h.assertTrue(!h.getBlockState(POS).getValue(ChapelBlock.POWERED),"Bell resets after signal");
            h.succeed();
        }, new TestData<>(environment, id("empty"), 100, 1, true));
        test(e, "devotional_item_state_and_incense", h -> {
            var player=h.makeMockPlayer(GameType.SURVIVAL);
            var rosary=new ItemStack(ModContent.ROSARY.get());
            player.setItemInHand(InteractionHand.MAIN_HAND,rosary);
            for(int i=0;i<6;i++) ModContent.ROSARY.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
            h.assertValueEqual(rosary.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntOr("MysteryIndex",-1),1,"Rosary cycles after five mysteries");
            player.setShiftKeyDown(true);
            ModContent.ROSARY.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
            h.assertValueEqual(rosary.get(DataComponents.CUSTOM_DATA).copyTag().getIntOr("MysterySet",-1),1,"Sneak switches mystery set");
            player.setShiftKeyDown(false);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.THURIBLE.get()));
            h.assertTrue(!ModContent.THURIBLE.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND).consumesAction(),"No smoke without fuel");
            player.getInventory().add(new ItemStack(ModContent.INCENSE.get(),2));
            h.assertTrue(ModContent.THURIBLE.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND).consumesAction(),"Thurible burns incense");
            int count=0;
            for(int i=0;i<player.getInventory().getContainerSize();i++) if(player.getInventory().getItem(i).is(ModContent.INCENSE.get())) count+=player.getInventory().getItem(i).getCount();
            h.assertValueEqual(count,1,"One incense consumed");
            h.succeed();
        }, new TestData<>(environment, id("empty"), 100, 1, true));
        test(e, "all_survival_recipes_craft", CatholiCraftTests::recipes, new TestData<>(environment,id("empty"),100,1,true));
        test(e, "st_josephs_workbench_crafts_without_clashes", CatholiCraftTests::workbench, new TestData<>(environment,id("empty"),100,1,true));
        test(e, "chapel_blueprint_places", h -> {
            var origin=h.absolutePos(BlockPos.ZERO);
            h.assertTrue(ChapelCommands.firstObstruction(h.getLevel(),origin).isEmpty(),"An empty volume is accepted");
            h.setBlock(new BlockPos(7,3,5),Blocks.DIAMOND_BLOCK);
            h.assertTrue(ChapelCommands.firstObstruction(h.getLevel(),origin).isPresent(),"An obstruction in the interior is detected");
            h.assertBlockPresent(Blocks.DIAMOND_BLOCK,new BlockPos(7,3,5));
            h.setBlock(new BlockPos(7,3,5),Blocks.AIR);
            var plan=ChapelCommands.blueprint();
            ChapelCommands.place(h.getLevel(),origin);
            h.assertBlockPresent(ModContent.TABERNACLE.get(),new BlockPos(13,2,5));
            h.assertBlockPresent(ModContent.ALTAR.get(),new BlockPos(12,1,5));
            h.assertBlockPresent(ModContent.CRUCIFIX.get(),new BlockPos(13,3,5));
            h.assertBlockPresent(ModContent.PEW.get(),new BlockPos(3,1,2));
            h.assertTrue(h.getBlockEntity(new BlockPos(13,2,5),TabernacleBlockEntity.class).getContainerSize()==9,"Chapel has usable storage");
            h.assertBlockPresent(Blocks.AIR,new BlockPos(0,1,5));
            h.assertTrue(h.getBlockEntity(new BlockPos(11,1,2),LecternBlockEntity.class).getBook().is(ModContent.MISSAL_1962.get()),"Chapel includes the 1962 missal");
            h.assertTrue(h.getBlockEntity(new BlockPos(11,1,3),LecternBlockEntity.class).getBook().is(ModContent.MODERN_MISSAL.get()),"Chapel includes the modern missal");
            h.assertValueEqual(h.getBlockState(new BlockPos(9,2,9)).getValue(ChapelBlock.STATION),14,"Chapel has all fourteen stations");
            h.succeed();
        },new TestData<>(environment,id("empty_chapel"),100,1,true));
        test(e,"sacristy_cabinet_storage",h -> {
            h.setBlock(POS,ModContent.SACRISTY_CABINET.get());
            var cabinet=h.getBlockEntity(POS,TabernacleBlockEntity.class);
            h.assertValueEqual(cabinet.getContainerSize(),27,"Cabinet capacity");
            cabinet.setItem(26,new ItemStack(ModContent.GREEN_DALMATIC.get()));
            var saved=cabinet.saveWithFullMetadata(h.getLevel().registryAccess());
            var loaded=(TabernacleBlockEntity)BlockEntity.loadStatic(h.absolutePos(POS),h.getBlockState(POS),saved,h.getLevel().registryAccess());
            h.assertTrue(loaded.getItem(26).is(ModContent.GREEN_DALMATIC.get()),"Last cabinet slot persists");
            h.getLevel().destroyBlock(h.absolutePos(POS),true);
            h.assertItemEntityCountIs(ModContent.GREEN_DALMATIC.get(),POS,2,1);
            h.succeed();
        },new TestData<>(environment,id("empty"),100,1,true));
        test(e,"altar_cloth_swap_and_drop",h -> {
            h.setBlock(POS,ModContent.ALTAR.get());
            var player=h.makeMockPlayer(GameType.SURVIVAL);
            var cloth=new ItemStack(ModContent.RED_ALTAR_CLOTH.get(),2);
            player.setItemInHand(InteractionHand.MAIN_HAND,cloth);
            h.useBlock(POS,player);
            h.assertValueEqual(h.getBlockState(POS).getValue(ChapelBlock.CLOTH),1,"Red frontal applied");
            h.assertValueEqual(cloth.getCount(),1,"One new frontal consumed");
            int old=0;
            for(int i=0;i<player.getInventory().getContainerSize();i++) if(player.getInventory().getItem(i).is(ModContent.WHITE_ALTAR_CLOTH.get())) old+=player.getInventory().getItem(i).getCount();
            h.assertValueEqual(old,1,"Original frontal returned");
            h.useBlock(POS,player);
            h.assertValueEqual(cloth.getCount(),1,"Applying same color is free");
            h.getLevel().destroyBlock(h.absolutePos(POS),true);
            var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(h.absolutePos(POS)).inflate(2));
            var altar=drops.stream().filter(d -> d.getItem().is(ModContent.ALTAR.get().asItem())).findFirst().orElseThrow().getItem();
            h.assertValueEqual(altar.get(DataComponents.BLOCK_STATE).properties().get("cloth"),"1","Dropped altar preserves its frontal");
            h.succeed();
        },new TestData<>(environment,id("empty"),100,1,true));
        test(e,"advent_wreath_four_candles",h -> {
            h.setBlock(POS,ModContent.ADVENT_WREATH.get());
            var player=h.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.FLINT_AND_STEEL));
            for(int i=1;i<=4;i++) {
                h.useBlock(POS,player);
                h.assertValueEqual(h.getBlockState(POS).getValue(ChapelBlock.CANDLES),i,"Lit Advent candles");
                h.assertValueEqual(h.getBlockState(POS).getLightEmission(h.getLevel(),h.absolutePos(POS)),i*3,"Advent light level");
            }
            h.useBlock(POS,player);
            h.assertValueEqual(h.getBlockState(POS).getValue(ChapelBlock.CANDLES),4,"No fifth candle");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            for(int i=0;i<5;i++) h.useBlock(POS,player);
            h.assertValueEqual(h.getBlockState(POS).getValue(ChapelBlock.CANDLES),0,"Wreath extinguishes without underflow");
            h.succeed();
        },new TestData<>(environment,id("empty"),100,1,true));
        test(e,"stations_cycle_and_preserve_number",h -> {
            h.setBlock(POS,ModContent.STATIONS.get());
            var player=h.makeMockPlayer(GameType.SURVIVAL);
            for(int i=0;i<14;i++) h.useBlock(POS,player);
            h.assertValueEqual(h.getBlockState(POS).getValue(ChapelBlock.STATION),1,"Stations wrap after fourteen");
            player.setShiftKeyDown(true);
            h.useBlock(POS,player);
            h.assertValueEqual(h.getBlockState(POS).getValue(ChapelBlock.STATION),14,"Reverse stations wrap");
            h.getLevel().destroyBlock(h.absolutePos(POS),true);
            var dropped=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(h.absolutePos(POS)).inflate(2)).stream().filter(d -> d.getItem().is(ModContent.STATIONS.get().asItem())).findFirst().orElseThrow().getItem();
            h.assertValueEqual(dropped.get(DataComponents.BLOCK_STATE).properties().get("station"),"14","Dropped plaque preserves numeral");
            h.succeed();
        },new TestData<>(environment,id("empty"),100,1,true));
        test(e,"incense_burner_fuel_and_save",h -> {
            h.setBlock(POS,ModContent.INCENSE_BURNER.get());
            var player=h.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.INCENSE.get(),6));
            var burner=h.getBlockEntity(POS,IncenseBurnerBlockEntity.class);
            for(int i=0;i<6;i++) h.useBlock(POS,player);
            h.assertValueEqual(burner.remainingTicks(),6000,"Fuel capped at five minutes");
            h.assertValueEqual(player.getMainHandItem().getCount(),1,"Full burner does not waste incense");
            IncenseBurnerBlockEntity.tick(h.getLevel(),h.absolutePos(POS),h.getBlockState(POS),burner);
            var saved=burner.saveWithFullMetadata(h.getLevel().registryAccess());
            var loaded=(IncenseBurnerBlockEntity)BlockEntity.loadStatic(h.absolutePos(POS),h.getBlockState(POS),saved,h.getLevel().registryAccess());
            h.assertValueEqual(loaded.remainingTicks(),5999,"Remaining fuel survives a save");
            for(int i=0;i<5999;i++) IncenseBurnerBlockEntity.tick(h.getLevel(),h.absolutePos(POS),h.getBlockState(POS),burner);
            h.assertValueEqual(burner.remainingTicks(),0,"Fuel expires");
            h.assertTrue(!h.getBlockState(POS).getValue(IncenseBurnerBlock.LIT),"Burner stops glowing when empty");
            h.succeed();
        },new TestData<>(environment,id("empty"),100,1,true));
        test(e,"aspergillum_refill_and_deplete",h -> {
            h.setBlock(POS,ModContent.HOLY_WATER_FONT.get().defaultBlockState().setValue(ChapelBlock.WATER,3));
            var player=h.makeMockPlayer(GameType.SURVIVAL);
            var stack=new ItemStack(ModContent.ASPERGILLUM.get());
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            h.useBlock(POS,player);
            h.assertValueEqual(AspergillumItem.charges(stack),8,"Aspergillum fills");
            h.assertValueEqual(h.getBlockState(POS).getValue(ChapelBlock.WATER),2,"One font charge consumed");
            h.useBlock(POS,player);
            h.assertValueEqual(h.getBlockState(POS).getValue(ChapelBlock.WATER),2,"Full aspergillum wastes no water");
            player.igniteForSeconds(30);
            player.addEffect(new MobEffectInstance(MobEffects.POISON,600));
            for(int i=0;i<8;i++) ModContent.ASPERGILLUM.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
            h.assertTrue(!player.isOnFire() && !player.hasEffect(MobEffects.POISON),"Sprinkling cleanses player");
            h.assertValueEqual(AspergillumItem.charges(stack),0,"Eight uses deplete water");
            h.assertTrue(player.getMainHandItem().is(ModContent.ASPERGILLUM.get()),"Empty tool remains intact");
            h.assertTrue(!ModContent.ASPERGILLUM.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND).consumesAction(),"Empty tool cannot sprinkle");
            h.succeed();
        },new TestData<>(environment,id("empty"),100,1,true));
        test(e,"pew_seating_and_cleanup",h -> {
            h.setBlock(POS,ModContent.PEW.get());
            var player=h.makeMockPlayer(GameType.SURVIVAL);
            h.useBlock(POS,player);
            h.assertTrue(player.getVehicle() instanceof ArmorStand,"Pew seats its user");
            var seat=(ArmorStand)player.getVehicle();
            h.assertTrue(seat.isMarker() && seat.isInvisible(),"Seat is an invisible marker");
            var other=h.makeMockPlayer(GameType.SURVIVAL);
            h.useBlock(POS,other);
            h.assertTrue(!other.isPassenger(),"Occupied pew rejects second user");
            player.stopRiding();
            PewSeating.cleanup(seat);
            h.assertTrue(seat.isRemoved(),"Unused seat is removed");
            h.useBlock(POS,other);
            var second=(ArmorStand)other.getVehicle();
            h.setBlock(POS,Blocks.AIR);
            PewSeating.cleanup(second);
            h.assertTrue(second.isRemoved() && !other.isPassenger(),"Breaking pew removes its seat");
            h.succeed();
        },new TestData<>(environment,id("empty"),100,1,true));
    }

    private static void expandedTests(RegisterGameTestsEvent e, net.minecraft.core.Holder<net.minecraft.gametest.framework.TestEnvironmentDefinition<?>> environment) {
        test(e,"modern_mass_complete",h -> massSequence(h,false),new TestData<>(environment,id("empty_chapel"),250,1,true));
        test(e,"traditional_mass_complete",h -> massSequence(h,true),new TestData<>(environment,id("empty_chapel"),250,1,true));
        test(e,"mass_preparation_ownership_and_gifts",h -> {
            ChapelCommands.place(h.getLevel(),h.absolutePos(BlockPos.ZERO));
            var pos=new BlockPos(12,1,5);
            var altar=h.getBlockEntity(pos,MassAltarBlockEntity.class);
            var player=h.makeMockPlayer(GameType.SURVIVAL);
            altar.interact(player,new ItemStack(ModContent.MODERN_MISSAL.get()));
            h.assertTrue(!altar.isActive(),"Preparation required before service begins");
            equipCelebrant(player);
            altar.interact(player,new ItemStack(ModContent.MODERN_MISSAL.get()));
            h.assertValueEqual(altar.stepIndex(),0,"Entrance begins after preparation");
            var visitor=h.makeMockPlayer(GameType.SURVIVAL);visitor.setUUID(java.util.UUID.randomUUID());equipCelebrant(visitor);
            visitor.setShiftKeyDown(true);altar.interact(visitor,new ItemStack(ModContent.MODERN_MISSAL.get()));
            h.assertTrue(altar.isActive(),"Visitor cannot cancel another celebrant's service");
            for(int i=1;i<=5;i++) {
                final int expected=i;
                h.runAfterDelay(i*10,() -> {altar.interact(player,new ItemStack(ModContent.MODERN_MISSAL.get()));h.assertValueEqual(altar.stepIndex(),expected,"Advance through introductory rites and Word");});
            }
            h.runAfterDelay(60,() -> {
                player.getInventory().add(new ItemStack(ModContent.ALTAR_BREAD.get(),2));
                altar.interact(player,new ItemStack(ModContent.MODERN_MISSAL.get()));
                h.assertValueEqual(altar.stepIndex(),5,"Wine is required before advancing");
                h.assertValueEqual(player.getInventory().countItem(ModContent.ALTAR_BREAD.get()),2,"Failed offertory must consume no bread");
                player.getInventory().add(new ItemStack(ModContent.WINE_CRUET.get(),2));
                altar.interact(player,new ItemStack(ModContent.MODERN_MISSAL.get()));
                h.assertValueEqual(altar.stepIndex(),6,"Offertory advances when both gifts exist");
                h.assertValueEqual(player.getInventory().countItem(ModContent.ALTAR_BREAD.get()),1,"One bread consumed");
                h.assertValueEqual(player.getInventory().countItem(ModContent.WINE_CRUET.get()),1,"One wine cruet consumed");
                h.assertValueEqual(player.getInventory().countItem(Items.GLASS_BOTTLE),1,"Bottle returned");
                altar.interact(player,new ItemStack(ModContent.MODERN_MISSAL.get()));
                h.assertValueEqual(altar.stepIndex(),6,"Repeated click is debounced");
                var saved=altar.saveWithFullMetadata(h.getLevel().registryAccess());
                var loaded=(MassAltarBlockEntity)BlockEntity.loadStatic(h.absolutePos(pos),h.getBlockState(pos),saved,h.getLevel().registryAccess());
                h.assertTrue(loaded!=null && loaded.stepIndex()==6 && !loaded.isTraditional(),"Mass position and order persist");
                player.setShiftKeyDown(true);altar.interact(player,new ItemStack(ModContent.MODERN_MISSAL.get()));
                h.assertTrue(!altar.isActive(),"Celebrant can end the service");
                h.succeed();
            });
        },new TestData<>(environment,id("empty_chapel"),120,1,true));
        test(e,"chant_jukebox_playback_and_persistence",h -> {
            h.setBlock(POS,Blocks.JUKEBOX);
            var box=h.getBlockEntity(POS,net.minecraft.world.level.block.entity.JukeboxBlockEntity.class);
            for(var record:List.of(ModContent.CHANT_VENI_CREATOR,ModContent.CHANT_VENI_SANCTE,ModContent.CHANT_DIES_IRAE)) {
                var disc=new ItemStack(record.get());
                var song=net.minecraft.world.item.JukeboxSong.fromStack(disc).orElseThrow();
                h.assertTrue(song.value().lengthInSeconds()>25,"Full chant duration registered");
                box.setTheItem(disc);h.assertTrue(box.getSongPlayer().isPlaying(),"Chant plays through vanilla jukebox");
                var tag=box.saveWithFullMetadata(h.getLevel().registryAccess());
                var loaded=(net.minecraft.world.level.block.entity.JukeboxBlockEntity)BlockEntity.loadStatic(h.absolutePos(POS),h.getBlockState(POS),tag,h.getLevel().registryAccess());
                h.assertTrue(loaded!=null && loaded.getTheItem().is(record.get()),"Jukebox disc survives save/load");
                box.setTheItem(ItemStack.EMPTY);
            }
            h.succeed();
        },new TestData<>(environment,id("empty"),100,1,true));
        test(e,"monk_trade_and_save",h -> {
            var monk=h.spawnWithNoFreeWill(ModContent.MONK.get(),POS);
            var player=h.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.EMERALD,2));
            monk.interact(player,InteractionHand.MAIN_HAND,net.minecraft.world.phys.Vec3.ZERO);
            h.assertValueEqual(player.getInventory().countItem(ModContent.PRAYER_BOOK.get()),1,"Monk supplies prayer book");
            h.assertValueEqual(player.getItemInHand(InteractionHand.MAIN_HAND).getCount(),1,"One emerald used");
            monk.interact(player,InteractionHand.MAIN_HAND,net.minecraft.world.phys.Vec3.ZERO);
            h.assertValueEqual(player.getInventory().countItem(ModContent.PRAYER_BOOK.get()),1,"Trade cooldown prevents repeated transaction");
            player.setShiftKeyDown(true);monk.interact(player,InteractionHand.MAIN_HAND,net.minecraft.world.phys.Vec3.ZERO);
            h.assertTrue(monk.currentOffer().is(ModContent.INCENSE.get()) && monk.currentOffer().getCount()==8,"Sneak selects next supply");
            var out=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,h.getLevel().registryAccess());
            monk.saveWithoutId(out);
            var copy=ModContent.MONK.get().create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.LOAD);
            copy.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));
            h.assertTrue(copy.currentOffer().is(ModContent.INCENSE.get()),"Monk offer persists");
            h.assertTrue(!copy.removeWhenFarAway(100000),"Resident does not despawn");h.succeed();
        },new TestData<>(environment,id("empty"),100,1,true));
        test(e,"cathedral_furnished_template",h -> {
            var origin=h.absolutePos(new BlockPos(1,1,1));
            h.assertTrue(CathedralCommands.firstObstruction(h.getLevel(),origin).isEmpty(),"Clear cathedral volume accepted");
            h.assertTrue(CathedralCommands.place(h.getLevel(),origin),"Full cathedral template placed");
            h.assertTrue(h.getLevel().getBlockEntity(origin.offset(34,2,14)) instanceof MassAltarBlockEntity,"Sanctuary has functional Mass altar");
            var cabinet=(TabernacleBlockEntity)h.getLevel().getBlockEntity(origin.offset(25,1,25));
            h.assertTrue(cabinet!=null && cabinet.getItem(5).is(ModContent.ALTAR_BREAD.get()),"Sacristy stocks offertory bread");
            h.assertTrue(cabinet.getItem(6).is(ModContent.WINE_CRUET.get()),"Sacristy stocks wine cruets");
            h.assertTrue(cabinet.getItem(10).is(ModContent.CHANT_DIES_IRAE.get()),"Sacristy stocks all three chant discs");
            h.assertValueEqual(h.getLevel().getEntitiesOfClass(MonkEntity.class,new AABB(origin.getX(),origin.getY(),origin.getZ(),origin.getX()+41,origin.getY()+25,origin.getZ()+29)).size(),3,"Three resident monks spawn");
            h.assertTrue(h.getLevel().getBlockState(origin.offset(3,19,14)).is(Blocks.GOLD_BLOCK),"Facade cross exists");h.succeed();
        },new TestData<>(environment,id("empty_cathedral"),100,1,true));
        test(e,"cathedral_obstruction_is_atomic",h -> {
            var origin=h.absolutePos(new BlockPos(1,1,1));
            var obstruction=origin.offset(20,10,14);h.getLevel().setBlock(obstruction,Blocks.DIAMOND_BLOCK.defaultBlockState(),3);
            h.assertTrue(CathedralCommands.firstObstruction(h.getLevel(),origin).isPresent(),"Interior obstruction rejected before construction");
            h.assertTrue(h.getLevel().isEmptyBlock(origin),"Preflight changes no floor block");
            h.assertTrue(h.getLevel().getBlockState(obstruction).is(Blocks.DIAMOND_BLOCK),"Existing block preserved");h.succeed();
        },new TestData<>(environment,id("empty_cathedral"),100,1,true));
        test(e,"cathedral_worldgen_start",h -> {
            var level=h.getLevel();var lookup=level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            var holder=lookup.getOrThrow(ResourceKey.create(Registries.STRUCTURE,CatholiCraft.id("cathedral")));
            var set=level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET).getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET,CatholiCraft.id("cathedrals"))).value();
            h.assertTrue(set.placement() instanceof net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement,"Natural random-spread generation is configured");
            h.assertTrue(set.structures().getFirst().structure().equals(holder),"Structure set points to cathedral");
            var generator=level.getChunkSource().getGenerator();
            var start=holder.value().generate(holder,level.dimension(),level.registryAccess(),generator,generator.getBiomeSource(),level.getChunkSource().randomState(),level.getStructureManager(),level.getSeed(),new net.minecraft.world.level.ChunkPos(h.absolutePos(POS).getX()>>4,h.absolutePos(POS).getZ()>>4),0,level,biome -> true);
            h.assertTrue(start.isValid() && !start.getPieces().isEmpty(),"Vanilla jigsaw generator produces a cathedral start");
            h.assertTrue(start.getBoundingBox().getYSpan()>=25,"Full tower height included in natural start");h.succeed();
        },new TestData<>(environment,id("empty"),100,1,true));
    }
    private static void equipCelebrant(net.minecraft.world.entity.player.Player player) {
        player.setItemSlot(EquipmentSlot.CHEST,new ItemStack(ModContent.WHITE_CHASUBLE.get()));
        player.setItemSlot(EquipmentSlot.LEGS,new ItemStack(ModContent.ALB.get()));
    }
    private static void massSequence(GameTestHelper h,boolean traditional) {
        ChapelCommands.place(h.getLevel(),h.absolutePos(BlockPos.ZERO));
        var player=h.makeMockPlayer(GameType.SURVIVAL);equipCelebrant(player);
        var pos=new BlockPos(12,1,5);var altar=h.getBlockEntity(pos,MassAltarBlockEntity.class);
        var missal=new ItemStack(traditional?ModContent.MISSAL_1962.get():ModContent.MODERN_MISSAL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND,missal);
        player.getInventory().add(new ItemStack(ModContent.ALTAR_BREAD.get(),2));player.getInventory().add(new ItemStack(ModContent.WINE_CRUET.get(),2));
        h.useBlock(pos,player);
        h.assertValueEqual(altar.stepIndex(),0,"Missal on altar starts the service");
        h.assertTrue(altar.isTraditional()==traditional,"Correct Mass order selected");
        int count=traditional?MassAltarBlockEntity.TRADITIONAL.length:MassAltarBlockEntity.MODERN.length;
        for(int i=1;i<=count;i++) {
            final int expected=i;
            h.runAfterDelay(i*10,() -> {
                h.useBlock(pos,player);
                if(expected<count) h.assertValueEqual(altar.stepIndex(),expected,"Every Mass step reached in order");
                else {
                    h.assertTrue(!altar.isActive(),"Complete Mass returns altar to ready state");
                    h.assertValueEqual(player.getInventory().countItem(ModContent.ALTAR_BREAD.get()),1,"Bread consumed once in full service");
                    h.assertValueEqual(player.getInventory().countItem(ModContent.WINE_CRUET.get()),1,"Wine consumed once in full service");h.succeed();
                }
            });
        }
    }

    /**
     * The iron-shovel shape is an iron shovel at a crafting table and an aspergillum at St. Joseph's Workbench;
     * taking the result uses exactly one of each ingredient; vanilla recipes do nothing at the workbench.
     */
    private static void workbench(GameTestHelper h) {
        var level=h.getLevel();
        List<ItemStack> grid=new ArrayList<>(Collections.nCopies(9,ItemStack.EMPTY));
        grid.set(1,new ItemStack(Items.IRON_INGOT)); grid.set(4,new ItemStack(Items.STICK)); grid.set(7,new ItemStack(Items.STICK));
        var input=CraftingInput.of(3,3,grid);
        var table=level.recipeAccess().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,level).orElseThrow(() -> new AssertionError("No crafting-table recipe for the shovel shape"));
        h.assertTrue(table.value().assemble(input).is(Items.IRON_SHOVEL),"The crafting table makes an iron shovel, not "+table.value().assemble(input));
        h.setBlock(new BlockPos(1,1,1),ModContent.ST_JOSEPHS_WORKBENCH.get());
        @SuppressWarnings("removal") var player=h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        var menu=new com.catholicraft.workbench.WorkbenchMenu(7,player.getInventory(),net.minecraft.world.inventory.ContainerLevelAccess.create(level,h.absolutePos(new BlockPos(1,1,1))));
        menu.getSlot(2).set(new ItemStack(Items.IRON_INGOT,2));
        menu.getSlot(5).set(new ItemStack(Items.STICK,3));
        menu.getSlot(8).set(new ItemStack(Items.STICK,3));
        menu.slotsChanged(null);
        var made=menu.getSlot(0).getItem().copy();
        h.assertTrue(made.is(ModContent.ITEMS.getEntries().stream().filter(i -> i.getId().getPath().equals("aspergillum")).findFirst().orElseThrow().get()),
                "The workbench makes an aspergillum, not "+made);
        menu.getSlot(0).onTake(player,made);
        h.assertValueEqual(menu.getSlot(2).getItem().getCount(),1,"iron left after one craft");
        h.assertValueEqual(menu.getSlot(5).getItem().getCount(),2,"sticks left in the middle row");
        h.assertValueEqual(menu.getSlot(8).getItem().getCount(),2,"sticks left in the bottom row");
        for (int i=1;i<10;i++) menu.getSlot(i).set(ItemStack.EMPTY);
        menu.getSlot(2).set(new ItemStack(Items.OAK_PLANKS)); menu.getSlot(5).set(new ItemStack(Items.OAK_PLANKS));
        menu.slotsChanged(null);
        h.assertTrue(menu.getSlot(0).getItem().isEmpty(),"vanilla sticks were craftable at the workbench");
        h.succeed();
    }

    private static void recipes(GameTestHelper h) {
        int count=0;
        for (var holder:ModContent.ITEMS.getEntries()) {
            String name=holder.getId().getPath();
            if(name.equals("holy_water")) continue;
            var key=ResourceKey.create(Registries.RECIPE,CatholiCraft.id(name));
            var recipe=h.getLevel().getServer().getRecipeManager().byKey(key).orElseThrow(() -> new AssertionError("Missing recipe "+name)).value();
            try(var stream=CatholiCraft.class.getResourceAsStream("/data/catholicraft/recipe/"+name+".json")) {
                var json=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
                // Workbench recipes wrap an ordinary crafting recipe.
                if(json.has("recipe")) json=json.getAsJsonObject("recipe");
                List<ItemStack> grid=new ArrayList<>(Collections.nCopies(9,ItemStack.EMPTY));
                if(json.has("pattern")) {
                    var pattern=json.getAsJsonArray("pattern");
                    var ingredients=json.getAsJsonObject("key");
                    for(int y=0;y<pattern.size();y++) {
                        String row=pattern.get(y).getAsString();
                        for(int x=0;x<row.length();x++) if(row.charAt(x)!=' ') grid.set(y*3+x,stack(ingredients.get(String.valueOf(row.charAt(x))).getAsString()));
                    }
                } else {
                    var ingredients=json.getAsJsonArray("ingredients");
                    for(int i=0;i<ingredients.size();i++) grid.set(i,stack(ingredients.get(i).getAsString()));
                }
                var input=CraftingInput.of(3,3,grid);
                var crafting=(CraftingRecipe)recipe;
                h.assertTrue(crafting.matches(input,h.getLevel()),"Recipe matches its ingredients: "+name);
                var output=crafting.assemble(input);
                h.assertTrue(output.is(holder.get()),"Correct crafted item: "+name);
                h.assertValueEqual(output.getCount(),json.getAsJsonObject("result").get("count").getAsInt(),"Crafted quantity: "+name);
                count++;
            } catch(Exception ex) { throw new RuntimeException(name,ex); }
        }
        h.assertValueEqual(count,72,"All survival recipes tested");
        h.succeed();
    }

    private static ItemStack stack(String id) { return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id))); }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("catholicraft_tests",path); }
    private static void test(RegisterGameTestsEvent event,String name,Consumer<GameTestHelper> body,TestData<net.minecraft.core.Holder<net.minecraft.gametest.framework.TestEnvironmentDefinition<?>>> data) {
        event.registerTest(id(name),new FunctionGameTestInstance(BuiltinTestFunctions.ALWAYS_PASS,data) {
            @Override public void run(GameTestHelper h) { body.accept(h); }
        });
    }
}
