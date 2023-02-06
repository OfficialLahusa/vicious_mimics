package com.lahusa.vicious_mimics.entity;

import com.lahusa.vicious_mimics.entity.ai.goal.MimicSitGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.FollowMobGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
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

    public static final TrackedData<Boolean> IS_JUMPING = DataTracker.registerData(MimicEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int delayTicks;
    private boolean transitioning;

    public MimicEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.ignoreCameraFrustum = true;
        this.delayTicks = 0;
        this.transitioning = false;
        this.moveControl = new MimicMoveControl(this);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new MimicSitGoal(this));
        this.goalSelector.add(3, new WanderAroundGoal(this, 1.0));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 1.0));
    }

    private <E extends GeoEntity> PlayState predicate(AnimationState<E> event) {
        RawAnimation anim = RawAnimation.begin();

        if(isJumping()) {
            anim = anim.thenPlay("animation.mimic.jump");
        }
        else if(isSitting()) {
            if(transitioning) anim = anim.thenPlay("animation.mimic.close");
            anim = anim.thenPlay("animation.mimic.idle_closed");
        }
        else {
            if(transitioning) anim = anim.thenPlay("animation.mimic.open");
            anim = anim.thenPlay("animation.mimic.idle_open");
        }

        event.getController().setAnimation(anim);
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        super.tick();

        // Decrement delay
        if(delayTicks > 0) --delayTicks;

        // Stop jumping when on ground or in fluid
        if(isJumping() && (isOnGround() || isTouchingWater() || isInLava())) {
            setIsJumping(false);
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if(delayTicks > 0) return ActionResult.PASS;

        boolean sitting = isSitting();
        setSitting(!sitting);

        if (!world.isClient) {
            world.playSound(null, getX(), getY(), getZ(), sitting ? SoundEvents.BLOCK_CHEST_OPEN : SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.HOSTILE, 1f, 1f);
        }

        delayTicks += 8;
        transitioning = true;

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
        dataTracker.startTracking(IS_JUMPING, false);
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public boolean isJumping() {
        return dataTracker.get(IS_JUMPING);
    }

    protected void setIsJumping(boolean value) {
        dataTracker.set(IS_JUMPING, value);
    }

    private static class MimicMoveControl extends MoveControl {
        private final float targetYaw;
        private int ticksUntilJump;
        private final MimicEntity mimic;
        private boolean jumpOften;

        public MimicMoveControl(MimicEntity mimic) {
            super(mimic);
            this.mimic = mimic;
            targetYaw = 180.0F * mimic.getYaw() / 3.1415927F;
        }

        public void tick() {
            entity.setYaw(wrapDegrees(entity.getYaw(), targetYaw, 90.0F));
            entity.headYaw = entity.getYaw();
            entity.bodyYaw = entity.getYaw();
            if (state != State.MOVE_TO) {
                entity.setForwardSpeed(0.0F);
            } else {
                state = State.WAIT;
                if (entity.isOnGround()) {
                    entity.setMovementSpeed((float)(speed * entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
                    if (ticksUntilJump-- <= 0) {
                        ticksUntilJump = 5; //this.mimic.getTicksUntilNextJump();
                        if (jumpOften) {
                            ticksUntilJump /= 3;
                        }

                        mimic.getJumpControl().setActive();
                        mimic.setIsJumping(true);

                        if (!mimic.world.isClient) {
                            mimic.world.playSound(null, mimic.getX(), mimic.getY(), mimic.getZ(), SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.HOSTILE, 1f, 1f);
                        }
                    } else {
                        mimic.sidewaysSpeed = 0.0F;
                        mimic.forwardSpeed = 0.0F;
                        entity.setMovementSpeed(0.0F);
                    }
                } else {
                    entity.setMovementSpeed((float)(speed * entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
                }

            }
        }
    }
}
