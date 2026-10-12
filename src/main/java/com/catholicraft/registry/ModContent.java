package com.catholicraft.registry;

import com.catholicraft.CatholiCraft;
import com.catholicraft.block.ChapelBlock;
import com.catholicraft.block.TabernacleBlock;
import com.catholicraft.block.TabernacleBlockEntity;
import com.catholicraft.block.SacristyCabinetBlock;
import com.catholicraft.block.IncenseBurnerBlock;
import com.catholicraft.block.IncenseBurnerBlockEntity;
import com.catholicraft.item.AltarClothItem;
import com.catholicraft.item.AspergillumItem;
import com.catholicraft.item.AltarBellItem;
import com.catholicraft.item.DevotionalPages;
import com.catholicraft.item.HolyWaterItem;
import com.catholicraft.item.MissalPages;
import com.catholicraft.item.RosaryItem;
import com.catholicraft.item.ThuribleItem;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CatholiCraft.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CatholiCraft.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CatholiCraft.ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CatholiCraft.ID);

    public static final DeferredBlock<TabernacleBlock> TABERNACLE = BLOCKS.registerBlock("tabernacle", TabernacleBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(2.5F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<ChapelBlock> ALTAR = furnishing("altar", ChapelBlock.Kind.ALTAR, MapColor.QUARTZ);
    public static final DeferredBlock<ChapelBlock> CRUCIFIX = furnishing("crucifix", ChapelBlock.Kind.CRUCIFIX, MapColor.WOOD);
    public static final DeferredBlock<ChapelBlock> HOLY_WATER_FONT = furnishing("holy_water_font", ChapelBlock.Kind.FONT, MapColor.QUARTZ);
    public static final DeferredBlock<ChapelBlock> BAPTISMAL_FONT = furnishing("baptismal_font", ChapelBlock.Kind.FONT, MapColor.QUARTZ);
    public static final DeferredBlock<ChapelBlock> ALTAR_CANDLE = furnishing("altar_candle", ChapelBlock.Kind.CANDLE, MapColor.GOLD);
    public static final DeferredBlock<ChapelBlock> SANCTUARY_LAMP = furnishing("sanctuary_lamp", ChapelBlock.Kind.LAMP, MapColor.COLOR_RED);
    public static final DeferredBlock<ChapelBlock> CHURCH_BELL = furnishing("church_bell", ChapelBlock.Kind.BELL, MapColor.GOLD);
    public static final DeferredBlock<ChapelBlock> KNEELER = furnishing("kneeler", ChapelBlock.Kind.KNEELER, MapColor.WOOD);
    public static final DeferredBlock<ChapelBlock> PEW = furnishing("pew", ChapelBlock.Kind.PEW, MapColor.WOOD);
    public static final DeferredBlock<ChapelBlock> CHALICE = furnishing("chalice", ChapelBlock.Kind.CHALICE, MapColor.GOLD);
    public static final DeferredBlock<ChapelBlock> CIBORIUM = furnishing("ciborium", ChapelBlock.Kind.CIBORIUM, MapColor.GOLD);
    public static final DeferredBlock<ChapelBlock> MONSTRANCE = furnishing("monstrance", ChapelBlock.Kind.MONSTRANCE, MapColor.GOLD);
    public static final DeferredBlock<StainedGlassBlock> STAINED_GLASS = BLOCKS.registerBlock("cross_stained_glass",
        p -> new StainedGlassBlock(DyeColor.BLUE, p),
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(.3F).sound(SoundType.GLASS).noOcclusion());
    public static final DeferredBlock<SacristyCabinetBlock> SACRISTY_CABINET = BLOCKS.registerBlock("sacristy_cabinet",SacristyCabinetBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2F).sound(SoundType.WOOD).noOcclusion());
    public static final DeferredBlock<ChapelBlock> ADVENT_WREATH = furnishing("advent_wreath",ChapelBlock.Kind.WREATH,MapColor.PLANT);
    public static final DeferredBlock<ChapelBlock> VOTIVE_RACK = furnishing("votive_rack",ChapelBlock.Kind.VOTIVE,MapColor.GOLD);
    public static final DeferredBlock<ChapelBlock> STATIONS = furnishing("stations_of_the_cross",ChapelBlock.Kind.STATIONS,MapColor.WOOD);
    public static final DeferredBlock<IncenseBurnerBlock> INCENSE_BURNER = BLOCKS.registerBlock("incense_burner",IncenseBurnerBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(2F).sound(SoundType.METAL).noOcclusion().lightLevel(s -> s.getValue(IncenseBurnerBlock.LIT) ? 6 : 0));
    public static final DeferredBlock<ChapelBlock> OUR_LADY_STATUE = furnishing("our_lady_statue",ChapelBlock.Kind.STATUE,MapColor.QUARTZ);
    public static final DeferredBlock<ChapelBlock> ST_JOSEPH_STATUE = furnishing("st_joseph_statue",ChapelBlock.Kind.STATUE,MapColor.QUARTZ);
    public static final DeferredBlock<ChapelBlock> CREDENCE_TABLE = furnishing("credence_table",ChapelBlock.Kind.TABLE,MapColor.WOOD);
    public static final DeferredBlock<ChapelBlock> AMBO = furnishing("ambo",ChapelBlock.Kind.AMBO,MapColor.WOOD);

    public static final DeferredItem<WrittenBookItem> MISSAL_1962 = ITEMS.registerItem("missal_1962", WrittenBookItem::new,
        p -> lore(p.stacksTo(1).rarity(Rarity.UNCOMMON).component(DataComponents.WRITTEN_BOOK_CONTENT, MissalPages.MISSAL_1962), "missal_1962"));
    public static final DeferredItem<WrittenBookItem> MODERN_MISSAL = ITEMS.registerItem("modern_missal", WrittenBookItem::new,
        p -> lore(p.stacksTo(1).rarity(Rarity.UNCOMMON).component(DataComponents.WRITTEN_BOOK_CONTENT, MissalPages.MODERN_MISSAL), "modern_missal"));
    public static final DeferredItem<WrittenBookItem> FIELD_GUIDE = ITEMS.registerItem("field_guide", WrittenBookItem::new,
        p -> p.stacksTo(1).component(DataComponents.WRITTEN_BOOK_CONTENT, MissalPages.GUIDE));
    public static final DeferredItem<WrittenBookItem> PRAYER_BOOK = ITEMS.registerItem("prayer_book",WrittenBookItem::new,
        p -> p.stacksTo(1).component(DataComponents.WRITTEN_BOOK_CONTENT,DevotionalPages.PRAYERS));
    public static final DeferredItem<WrittenBookItem> STATIONS_BOOK = ITEMS.registerItem("stations_book",WrittenBookItem::new,
        p -> p.stacksTo(1).component(DataComponents.WRITTEN_BOOK_CONTENT,DevotionalPages.STATIONS));
    public static final DeferredItem<HolyWaterItem> HOLY_WATER = ITEMS.registerItem("holy_water", HolyWaterItem::new,
        p -> lore(p.stacksTo(16), "holy_water"));
    public static final DeferredItem<RosaryItem> ROSARY = ITEMS.registerItem("rosary", RosaryItem::new,
        p -> lore(p.stacksTo(1), "rosary"));
    public static final DeferredItem<ThuribleItem> THURIBLE = ITEMS.registerItem("thurible", ThuribleItem::new,
        p -> lore(p.stacksTo(1), "thurible"));
    public static final DeferredItem<Item> INCENSE = ITEMS.registerSimpleItem("incense", p -> lore(p, "incense"));
    public static final DeferredItem<Item> ST_BENEDICT_MEDAL = medal("st_benedict_medal");
    public static final DeferredItem<Item> MIRACULOUS_MEDAL = medal("miraculous_medal");
    public static final DeferredItem<Item> ST_MICHAEL_MEDAL = medal("st_michael_medal");

    public static final DeferredItem<Item> WHITE_CHASUBLE = vestment("white_chasuble", EquipmentSlot.CHEST);
    public static final DeferredItem<Item> RED_CHASUBLE = vestment("red_chasuble", EquipmentSlot.CHEST);
    public static final DeferredItem<Item> GREEN_CHASUBLE = vestment("green_chasuble", EquipmentSlot.CHEST);
    public static final DeferredItem<Item> VIOLET_CHASUBLE = vestment("violet_chasuble", EquipmentSlot.CHEST);
    public static final DeferredItem<Item> ROSE_CHASUBLE = vestment("rose_chasuble", EquipmentSlot.CHEST);
    public static final DeferredItem<Item> BLACK_CHASUBLE = vestment("black_chasuble", EquipmentSlot.CHEST);
    public static final DeferredItem<Item> ALB = vestment("alb", EquipmentSlot.LEGS);
    public static final DeferredItem<Item> BIRETTA = vestment("biretta", EquipmentSlot.HEAD);
    public static final DeferredItem<Item> MITRE = vestment("mitre", EquipmentSlot.HEAD);
    public static final DeferredItem<Item> LITURGICAL_SHOES = vestment("liturgical_shoes", EquipmentSlot.FEET);
    public static final DeferredItem<Item> WHITE_DALMATIC = vestment("white_dalmatic",EquipmentSlot.CHEST);
    public static final DeferredItem<Item> RED_DALMATIC = vestment("red_dalmatic",EquipmentSlot.CHEST);
    public static final DeferredItem<Item> GREEN_DALMATIC = vestment("green_dalmatic",EquipmentSlot.CHEST);
    public static final DeferredItem<Item> VIOLET_DALMATIC = vestment("violet_dalmatic",EquipmentSlot.CHEST);
    public static final DeferredItem<Item> ROSE_DALMATIC = vestment("rose_dalmatic",EquipmentSlot.CHEST);
    public static final DeferredItem<Item> BLACK_DALMATIC = vestment("black_dalmatic",EquipmentSlot.CHEST);
    public static final DeferredItem<Item> CASSOCK = vestment("cassock",EquipmentSlot.CHEST);
    public static final DeferredItem<Item> SURPLICE = vestment("surplice",EquipmentSlot.CHEST);
    public static final DeferredItem<AltarClothItem> WHITE_ALTAR_CLOTH = cloth("white_altar_cloth",0);
    public static final DeferredItem<AltarClothItem> RED_ALTAR_CLOTH = cloth("red_altar_cloth",1);
    public static final DeferredItem<AltarClothItem> GREEN_ALTAR_CLOTH = cloth("green_altar_cloth",2);
    public static final DeferredItem<AltarClothItem> VIOLET_ALTAR_CLOTH = cloth("violet_altar_cloth",3);
    public static final DeferredItem<AltarClothItem> ROSE_ALTAR_CLOTH = cloth("rose_altar_cloth",4);
    public static final DeferredItem<AltarClothItem> BLACK_ALTAR_CLOTH = cloth("black_altar_cloth",5);
    public static final DeferredItem<AspergillumItem> ASPERGILLUM = ITEMS.registerItem("aspergillum",AspergillumItem::new,p -> lore(p.stacksTo(1),"aspergillum"));
    public static final DeferredItem<AltarBellItem> ALTAR_BELL = ITEMS.registerItem("altar_bell",AltarBellItem::new,p -> lore(p.stacksTo(1),"altar_bell"));
    public static final DeferredItem<Item> PROCESSIONAL_CROSS = ITEMS.registerSimpleItem("processional_cross",p -> lore(p.stacksTo(1).equippable(EquipmentSlot.OFFHAND),"processional_cross"));
    public static final DeferredItem<Item> SCAPULAR = ITEMS.registerSimpleItem("scapular",p -> lore(p.stacksTo(1).equippable(EquipmentSlot.OFFHAND),"scapular"));
    public static final DeferredItem<Item> BEESWAX = ITEMS.registerSimpleItem("beeswax",p -> lore(p,"beeswax"));
    public static final DeferredItem<Item> ALTAR_BREAD = ITEMS.registerSimpleItem("altar_bread",p -> lore(p,"altar_bread"));

    // Register one block item for each furnishing, with names shared by block and item resources.
    static { for (var block : BLOCKS.getEntries()) ITEMS.registerSimpleBlockItem((DeferredBlock<? extends Block>) block, p -> lore(p, block.getId().getPath())); }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TabernacleBlockEntity>> TABERNACLE_ENTITY =
        BLOCK_ENTITIES.register("tabernacle", () -> new BlockEntityType<>(TabernacleBlockEntity::new, TABERNACLE.get(),SACRISTY_CABINET.get()));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<IncenseBurnerBlockEntity>> INCENSE_BURNER_ENTITY =
        BLOCK_ENTITIES.register("incense_burner",() -> new BlockEntityType<>(IncenseBurnerBlockEntity::new,INCENSE_BURNER.get()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("chapel", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.catholicraft"))
        .icon(() -> new ItemStack(TABERNACLE.get()))
        .displayItems((parameters, output) -> ITEMS.getEntries().forEach(item -> output.accept(item.get())))
        .build());

    private static DeferredBlock<ChapelBlock> furnishing(String name, ChapelBlock.Kind kind, MapColor color) {
        return BLOCKS.registerBlock(name, p -> switch (kind) {
            case ALTAR -> new ChapelBlock.Altar(p);
            case WREATH -> new ChapelBlock.Wreath(p);
            case STATIONS -> new ChapelBlock.Stations(p);
            default -> new ChapelBlock(p,kind);
        },
            () -> BlockBehaviour.Properties.of().mapColor(color).strength(1.5F)
                .sound(color == MapColor.WOOD ? SoundType.WOOD : color == MapColor.GOLD ? SoundType.METAL : SoundType.STONE).noOcclusion()
                .lightLevel(s -> kind == ChapelBlock.Kind.WREATH ? s.getValue(ChapelBlock.CANDLES)*3
                    : s.getValue(ChapelBlock.LIT) && (kind == ChapelBlock.Kind.CANDLE || kind == ChapelBlock.Kind.LAMP || kind == ChapelBlock.Kind.VOTIVE) ? (kind == ChapelBlock.Kind.CANDLE ? 12 : 15) : 0));
    }

    private static DeferredItem<Item> medal(String name) {
        return ITEMS.registerSimpleItem(name, p -> lore(p.stacksTo(1).equippable(EquipmentSlot.OFFHAND).rarity(Rarity.UNCOMMON), name));
    }

    private static DeferredItem<Item> vestment(String name, EquipmentSlot slot) {
        return ITEMS.registerSimpleItem(name, p -> lore(p.stacksTo(1).component(DataComponents.EQUIPPABLE,
            Equippable.builder(slot).setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID, CatholiCraft.id(name)))
                .setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER).setDamageOnHurt(false).build()), "vestment"));
    }

    private static Item.Properties lore(Item.Properties p, String name) {
        return p.component(DataComponents.LORE, new ItemLore(List.of(Component.translatable("tooltip.catholicraft." + name))));
    }

    private static DeferredItem<AltarClothItem> cloth(String name,int color) {
        return ITEMS.registerItem(name,p -> new AltarClothItem(p,color),p -> lore(p,"altar_cloth"));
    }
    public static Item altarCloth(int color) {
        return switch (color) {
            case 1 -> RED_ALTAR_CLOTH.get(); case 2 -> GREEN_ALTAR_CLOTH.get(); case 3 -> VIOLET_ALTAR_CLOTH.get();
            case 4 -> ROSE_ALTAR_CLOTH.get(); case 5 -> BLACK_ALTAR_CLOTH.get(); default -> WHITE_ALTAR_CLOTH.get();
        };
    }

    private ModContent() {}
}
