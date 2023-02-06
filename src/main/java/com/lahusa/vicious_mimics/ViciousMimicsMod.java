package com.lahusa.vicious_mimics;

import com.lahusa.vicious_mimics.entity.MimicEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ViciousMimicsMod implements ModInitializer {

    public static final String MODID = "vicious_mimics";

    public static final EntityType<MimicEntity> MIMIC_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MODID, "mimic"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, MimicEntity::new).dimensions(EntityDimensions.fixed(0.875f, 0.875f)).build()
    );

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(MIMIC_ENTITY_TYPE, MimicEntity.createMimicAttributes());
    }
}
