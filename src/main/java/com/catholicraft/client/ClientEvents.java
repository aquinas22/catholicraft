package com.catholicraft.client;

import com.catholicraft.CatholiCraft;
import com.catholicraft.registry.ModContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CatholiCraft.ID, value = Dist.CLIENT)
public final class ClientEvents {
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) { event.registerLayerDefinition(MonkModel.LAYER, MonkModel::layer); }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) { event.registerEntityRenderer(ModContent.MONK.get(), MonkRenderer::new); }
    /** The server's workbench recipes, for JEI. */
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void recipes(net.neoforged.neoforge.client.event.RecipesReceivedEvent event) {
        com.catholicraft.compat.jei.ClientRecipes.map = event.getRecipeMap();
    }
}
