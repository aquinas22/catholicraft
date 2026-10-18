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
        ModContent.ENTITIES.register(bus);
        ModContent.SOUNDS.register(bus);
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event) -> event.put(ModContent.MONK.get(), com.catholicraft.entity.MonkEntity.attributes().build()));
        ModContent.BLOCKS.register(bus);
        ModContent.ITEMS.register(bus);
        ModContent.BLOCK_ENTITIES.register(bus);
        ModContent.TABS.register(bus);
        com.catholicraft.workbench.WorkbenchRecipe.TYPES.register(bus);
        com.catholicraft.workbench.WorkbenchRecipe.SERIALIZERS.register(bus);
        // Workbench recipes travel to clients so JEI can show them.
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.OnDatapackSyncEvent event) ->
                event.sendRecipes(com.catholicraft.workbench.WorkbenchRecipe.TYPE.get()));
        NeoForge.EVENT_BUS.addListener(ChapelCommands::register);
        NeoForge.EVENT_BUS.addListener(PewSeating::tick);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }
}
