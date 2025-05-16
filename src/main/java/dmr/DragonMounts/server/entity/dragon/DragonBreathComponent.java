package dmr.DragonMounts.server.entity.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3d;

/**
 * Abstract class that implements dragon breath attack functionality.
 * This extends the dragon entity hierarchy with breath attack capabilities.
 */
abstract class DragonBreathComponent extends DragonAnimationComponent {

    private static final double breathLength = 2.5; // 5 * 0.5

    // Breath attack properties
    @Getter
    @Setter
    protected Vector3d breathSourcePosition;

    protected long breathTime = -1;

    protected DragonBreathComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Renders the visual effects of the dragon's breath attack.
     */
    @OnlyIn(Dist.CLIENT)
    public void renderDragonBreath(
            float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer) {
        if (getControllingPassenger() == null) return;

        double yawRadians = Math.toRadians(entityYaw);
        double f4 = -Math.sin(yawRadians);
        double f5 = Math.cos(yawRadians);
        Vec3 lookVector = new Vec3(f4, 0, f5);

        var viewVector = getControllingPassenger().getViewVector(1f);

        if (breathSourcePosition != null) {
            for (int i = 0; i < 20; i++) {
                Vec3 speed = new Vec3(
                        lookVector.x * (0.5f + (getRandom().nextFloat() / 2)),
                        viewVector.y,
                        lookVector.z * (0.5f + (getRandom().nextFloat() / 2)));

                var particle = ParticleTypes.FLAME;
                level().addParticle(
                                particle,
                                getX() + breathSourcePosition.x,
                                getY() + breathSourcePosition.y,
                                getZ() + breathSourcePosition.z,
                                speed.x,
                                speed.y,
                                speed.z);
            }
        }
    }

    /**
     * Handles the breath attack logic.
     */
    public void doBreathAttack() {
        if (breathTime == -1) {
            breathTime = 0;
        } else {
            if (breathTime >= (int) (breathLength * 20)) {
                breathTime = -1;
            } else {
                breathTime++;

                if (getControllingPassenger() == null) return;
                var viewVector = getControllingPassenger().getViewVector(1f);

                float degrees = Mth.wrapDegrees(getControllingPassenger().yBodyRot);

                double yawRadians = Math.toRadians(degrees);
                double f4 = -Math.sin(yawRadians);
                double f5 = Math.cos(yawRadians);
                Vec3 lookVector = new Vec3(f4, viewVector.y, f5);

                var dimensions = getDimensions(getPose());
                float size = 15f;

                var offsetBoundingBox = new AABB(
                        getX() + (dimensions.width() / 2),
                        getY() + (dimensions.height() / 2),
                        getZ() + (dimensions.width() / 2),
                        getX() + (dimensions.width() / 2) + lookVector.x * size,
                        getY() + (dimensions.height() / 2) + lookVector.y * size,
                        getZ() + (dimensions.width() / 2) + lookVector.z * size);
                var entities = level().getNearbyEntities(
                                LivingEntity.class,
                                breathAttackTargetConditions(),
                                getControllingPassenger(),
                                offsetBoundingBox);

                entities.stream()
                        .filter(e -> e != this && e != getControllingPassenger())
                        .forEach(this::attackWithBreath);
            }
        }
    }

    /**
     * Returns the targeting conditions for breath attacks.
     */
    public TargetingConditions breathAttackTargetConditions() {
        return TargetingConditions.forCombat().ignoreInvisibilityTesting().selector(this::canHarmWithBreath);
    }

    /**
     * Checks if the dragon has a breath attack.
     */
    public boolean hasBreathAttack() {
        return true;
    }

    /**
     * Checks if the dragon can harm the target with its breath.
     */
    public boolean canHarmWithBreath(LivingEntity target) {
        return Objects.requireNonNull(getOwner()).canAttack(target) && !target.isAlliedTo(getOwner());
    }

    /**
     * Applies damage to the target from the breath attack.
     */
    public void attackWithBreath(LivingEntity target) {
        target.hurt(level().damageSources().mobAttack(this), 2);
        target.setRemainingFireTicks(5);
    }

    /**
     * Ticks the breath component.
     */
    public void tick() {
        super.tick();

        if (hasBreathAttack() && !level().isClientSide) {
            if (breathTime != -1) {
                doBreathAttack();
            }
        }
    }
}
