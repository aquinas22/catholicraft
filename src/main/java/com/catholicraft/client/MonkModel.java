package com.catholicraft.client;

import com.catholicraft.CatholiCraft;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;

/** Folded arms, a long habit and a cowl collar, using vanilla walking animation. */
public final class MonkModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(CatholiCraft.id("monk"), "main");
    public static LayerDefinition layer() {
        var mesh = VillagerModel.createBodyModel();
        mesh.getRoot().getChild("head").addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        mesh.getRoot().getChild("head").getChild("hat").addOrReplaceChild("hat_rim", CubeListBuilder.create(), PartPose.ZERO);
        mesh.getRoot().getChild("body").addOrReplaceChild("cowl", CubeListBuilder.create().texOffs(16, 20)
            .addBox(-5, -.5F, -3.5F, 10, 3, 7), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
    private MonkModel() {}
}
