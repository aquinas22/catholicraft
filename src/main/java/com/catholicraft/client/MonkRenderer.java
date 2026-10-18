package com.catholicraft.client;

import com.catholicraft.CatholiCraft;
import com.catholicraft.entity.MonkEntity;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

public final class MonkRenderer extends MobRenderer<MonkEntity, VillagerRenderState, VillagerModel> {
    public MonkRenderer(EntityRendererProvider.Context context) { super(context, new VillagerModel(context.bakeLayer(MonkModel.LAYER)), .5F); }
    @Override public VillagerRenderState createRenderState() { return new VillagerRenderState(); }
    @Override public Identifier getTextureLocation(VillagerRenderState state) { return CatholiCraft.id("textures/entity/monk.png"); }
}
