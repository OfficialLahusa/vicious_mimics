package com.lahusa.vicious_mimics.entity;

import com.lahusa.vicious_mimics.entity.ai.goal.MimicSitGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
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
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0));
    }

    private <E extends GeoEntity> PlayState predicate(AnimationState<E> event) {
        RawAnimation anim = RawAnimation.begin();

        if(isSitting()) {
            if(transitioning) anim = anim.thenPlay("animation.mimic.close");
            event.getController().setAnimation(anim.thenPlay("animation.mimic.idle_closed"));
            return PlayState.CONTINUE;
        }

        if(transitioning) anim = anim.thenPlay("animation.mimic.open");
        event.getController().setAnimation(anim.thenPlay("animation.mimic.idle_open"));
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
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    private static class MimicMoveControl extends MoveControl {
        private float targetYaw;
        private int ticksUntilJump;
        private final MimicEntity mimic;
        private boolean jumpOften;

        public MimicMoveControl(MimicEntity mimic) {
            super(mimic);
            this.mimic = mimic;
            this.targetYaw = 180.0F * mimic.getYaw() / 3.1415927F;
        }

        public void look(float targetYaw, boolean jumpOften) {
            this.targetYaw = targetYaw;
            this.jumpOften = jumpOften;
        }

        public void move(double speed) {
            this.speed = speed;
            this.state = State.MOVE_TO;
        }

        public void tick() {
            this.entity.setYaw(this.wrapDegrees(this.entity.getYaw(), this.targetYaw, 90.0F));
            this.entity.headYaw = this.entity.getYaw();
            this.entity.bodyYaw = this.entity.getYaw();
            if (this.state != State.MOVE_TO) {
                this.entity.setForwardSpeed(0.0F);
            } else {
                this.state = State.WAIT;
                if (this.entity.isOnGround()) {
                    this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
                    if (this.ticksUntilJump-- <= 0) {
                        this.ticksUntilJump = 5; //this.mimic.getTicksUntilNextJump();
                        if (this.jumpOften) {
                            this.ticksUntilJump /= 3;
                        }

                        this.mimic.getJumpControl().setActive();

                        if (!mimic.world.isClient) {
                            mimic.world.playSound(null, mimic.getX(), mimic.getY(), mimic.getZ(), SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.HOSTILE, 1f, 1f);
                        }
                    } else {
                        this.mimic.sidewaysSpeed = 0.0F;
                        this.mimic.forwardSpeed = 0.0F;
                        this.entity.setMovementSpeed(0.0F);
                    }
                } else {
                    this.entity.setMovementSpeed((float)(this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
                }

            }
        }
    }
}
