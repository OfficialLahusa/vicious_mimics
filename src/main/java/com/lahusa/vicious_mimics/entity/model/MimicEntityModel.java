package com.lahusa.vicious_mimics.entity.model;

import com.lahusa.vicious_mimics.ViciousMimicsMod;
import com.lahusa.vicious_mimics.entity.MimicEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class MimicEntityModel extends GeoModel<MimicEntity> {
    private static final Identifier modelResource = new Identifier(ViciousMimicsMod.MODID, "geo/mimic.geo.json");
    private static final Identifier textureResource = new Identifier(ViciousMimicsMod.MODID, "textures/entity/mimic.png");
    private static final Identifier animationResource = new Identifier(ViciousMimicsMod.MODID, "animations/mimic.animation.json");

    @Override
    public Identifier getModelResource(MimicEntity object) {
        return modelResource;
    }

    @Override
    public Identifier getTextureResource(MimicEntity object) {
        return textureResource;
    }

    @Override
    public Identifier getAnimationResource(MimicEntity object) {
        return animationResource;
    }
}
