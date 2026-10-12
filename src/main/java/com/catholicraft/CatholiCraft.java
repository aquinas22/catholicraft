package com.catholicraft;

import com.catholicraft.registry.ModContent;
import com.catholicraft.world.ChapelCommands;
import com.catholicraft.world.PewSeating;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CatholiCraft.ID)
public final class CatholiCraft {
    public static final String ID = "catholicraft";

    public CatholiCraft(IEventBus bus) {
        ModContent.BLOCKS.register(bus);
        ModContent.ITEMS.register(bus);
        ModContent.BLOCK_ENTITIES.register(bus);
        ModContent.TABS.register(bus);
        NeoForge.EVENT_BUS.addListener(ChapelCommands::register);
        NeoForge.EVENT_BUS.addListener(PewSeating::tick);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }
}
