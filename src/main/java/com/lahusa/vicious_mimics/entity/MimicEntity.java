package com.lahusa.vicious_mimics.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MimicEntity extends TameableEntity implements GeoEntity {

    //private static final TrackedData<Boolean> DORMANT;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int delayTicks;

    public MimicEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.ignoreCameraFrustum = true;
        this.delayTicks = 0;

    }

    private <E extends GeoEntity> PlayState predicate(AnimationState<E> event) {
        if(isDormant()) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("animation.mimic.close").thenPlay("animation.mimic.idle_closed"));
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(RawAnimation.begin().thenPlay("animation.mimic.open").thenPlay("animation.mimic.idle_open"));
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        super.tick();
        if(delayTicks > 0) --delayTicks;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if(delayTicks > 0) return ActionResult.PASS;

        boolean dormant = isDormant();
        setDormant(!dormant);

        if (!world.isClient) {
            world.playSound(null, getX(), getY(), getZ(), dormant ? SoundEvents.BLOCK_CHEST_OPEN : SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.HOSTILE, 1f, 1f);
        }

        delayTicks += 8;

        return ActionResult.SUCCESS;
    }

    public static DefaultAttributeContainer.Builder createMimicAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0f);
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        //dataTracker.startTracking(DORMANT, true);
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public boolean isDormant() {
        return isSitting();
        //return dataTracker.get(DORMANT);
    }

    public void setDormant(boolean value) {
        setSitting(value);
        //dataTracker.set(DORMANT, value);
    }

    static {
        //DORMANT = DataTracker.registerData(MimicEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }
}
