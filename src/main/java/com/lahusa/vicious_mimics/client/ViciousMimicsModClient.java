package com.lahusa.vicious_mimics.client;

import com.lahusa.vicious_mimics.ViciousMimicsMod;
import com.lahusa.vicious_mimics.entity.renderer.MimicEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class ViciousMimicsModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ViciousMimicsMod.MIMIC_ENTITY_TYPE, MimicEntityRenderer::new);
    }
}
