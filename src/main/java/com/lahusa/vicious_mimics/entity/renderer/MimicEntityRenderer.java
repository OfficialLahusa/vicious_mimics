package com.lahusa.vicious_mimics.entity.renderer;

import com.lahusa.vicious_mimics.entity.MimicEntity;
import com.lahusa.vicious_mimics.entity.model.MimicEntityModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MimicEntityRenderer extends GeoEntityRenderer<MimicEntity> {
    public MimicEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new MimicEntityModel());
    }
}
