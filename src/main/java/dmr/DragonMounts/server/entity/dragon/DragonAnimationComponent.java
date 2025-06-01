package dmr.DragonMounts.server.entity.dragon;

import lombok.Getter;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Abstract class that implements dragon animation functionality.
 * This extends the dragon entity hierarchy with animation capabilities.
 */
abstract class DragonAnimationComponent extends CoreDragonComponent {

    // Animation constants
    public static final RawAnimation NECK_TURN = RawAnimation.begin().thenLoop("neck_turn");
    public static final RawAnimation NECK_TURN_FLIGHT = RawAnimation.begin().thenLoop("neck_turn_flight");
    public static final RawAnimation GLIDE = RawAnimation.begin().thenLoop("glide");
    public static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly");
    public static final RawAnimation FLY_CLIMB = RawAnimation.begin().thenLoop("fly_climb");
    public static final RawAnimation DIVE = RawAnimation.begin().thenLoop("dive");
    public static final RawAnimation HOVER = RawAnimation.begin().thenLoop("hover");
    public static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    public static final RawAnimation SPRINT = RawAnimation.begin().thenLoop("run");
    public static final RawAnimation SNEAK_IDLE = RawAnimation.begin().thenLoop("sneak_idle");
    public static final RawAnimation SNEAK_WALK = RawAnimation.begin().thenLoop("sneak_walk");
    public static final RawAnimation SWIM = RawAnimation.begin().thenLoop("swim");
    public static final RawAnimation SWIM_IDLE = RawAnimation.begin().thenLoop("swim_idle");
    public static final RawAnimation SIT = RawAnimation.begin().thenLoop("sit");
    public static final RawAnimation SIT_ALT = RawAnimation.begin().thenLoop("sit-alt");
    public static final RawAnimation BITE = RawAnimation.begin().thenPlay("bite");
    public static final RawAnimation BREATH = RawAnimation.begin().thenPlay("breath");
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    // Animation controllers
    @Getter
    protected AnimationController<?> animationController;

    @Getter
    protected AnimationController<?> headController;

    protected final AnimatableInstanceCache cache;

    protected DragonAnimationComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.cache = GeckoLibUtil.createInstanceCache(this);
    }

    /**
     * Registers animation controllers with the dragon entity.
     */
    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        addDragonAnimations(controllers);
    }

    /** Sets up animation controllers for the dragon. */
    private void addDragonAnimations(ControllerRegistrar data) {
        headController = new AnimationController<>(this, "head-controller", 0, state -> {
            return state.setAndContinue(isFlying() ? NECK_TURN_FLIGHT : NECK_TURN);
        });

        headController.triggerableAnim("bite", BITE);
        headController.triggerableAnim("breath", BREATH);
        data.add(headController);

        animationController = new AnimationController<>(this, "controller", 5, state -> {
            Vec3 motio = new Vec3(getX() - xo, getY() - yo, getZ() - zo);
            boolean isMovingHorizontal = Math.sqrt(Math.pow(motio.x, 2) + Math.pow(motio.z, 2)) > 0.01;
            state.setControllerSpeed(1);

            if (isSwimming()) {
                return state.setAndContinue(SWIM);
            } else if (isInWater()) {
                if (isMovingHorizontal) {
                    return state.setAndContinue(SWIM);
                } else {
                    return state.setAndContinue(SWIM_IDLE);
                }
            } else if (isFlying() || (isPathFinding() && !onGround() && !isInWater())) {
                if (isMovingHorizontal) {
                    if (isSprinting()) {
                        var delta = getDeltaMovement().multiply(0, 0.25, 0);

                        if (delta.y < -0.25) {
                            return state.setAndContinue(DIVE);
                        } else if (delta.y > 0.25) {
                            return state.setAndContinue(FLY_CLIMB);
                        } else if (delta.y > 0) {
                            return state.setAndContinue(FLY);
                        } else {
                            return state.setAndContinue(GLIDE);
                        }
                    } else {
                        return state.setAndContinue(FLY);
                    }
                } else {
                    return state.setAndContinue(HOVER);
                }
            } else if (swinging && getTarget() != null) {
                return state.setAndContinue(BITE);
            } else if (getDragon().isRandomlySitting()) {
                return state.setAndContinue(SIT);
            } else if (isOrderedToSit()) {
                var lookAtContext = TargetingConditions.forNonCombat()
                        .range(10)
                        .selector(p_25531_ -> EntitySelector.notRiding(this).test(p_25531_));
                var lookAt = level().getNearestPlayer(lookAtContext, this, getX(), getEyeY(), getZ());

                if (lookAt != null) {
                    return state.setAndContinue(SIT_ALT);
                } else {
                    return state.setAndContinue(SIT);
                }
            } else if (isMovingHorizontal) {
                if (isSprinting()) {
                    return state.setAndContinue(SPRINT);
                } else {
                    state.setControllerSpeed(0.5f + (isShiftKeyDown() ? 2f : getSpeed()));
                    return state.setAndContinue(isShiftKeyDown() ? SNEAK_WALK : WALK);
                }
            }

            return state.setAndContinue(isShiftKeyDown() ? SNEAK_IDLE : IDLE);
        });

        animationController.setSoundKeyframeHandler(sound -> {
            if (sound.getKeyframeData().getSound().equalsIgnoreCase("flap")) {
                onFlap();
            }
        });

        data.add(animationController);
    }

    /**
     * Gets the animation instance cache.
     */
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
