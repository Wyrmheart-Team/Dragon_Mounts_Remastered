package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.server.entity.DragonConstants;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Base abstract class for dragon entities.
 * This is the foundation of the dragon entity hierarchy.
 */
abstract class CoreDragonComponent extends TamableAnimal
        implements Saddleable, FlyingAnimal, PlayerRideable, GeoEntity, HasCustomInventoryScreen, ContainerListener {

    protected final AnimatableInstanceCache cache;

    protected CoreDragonComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.cache = GeckoLibUtil.createInstanceCache(this);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(5, 5, 5);
    }

    @Override
    protected AABB getAttackBoundingBox() {
        return super.getAttackBoundingBox().inflate(2, 2, 2);
    }

    @Override
    public Vec3 getLightProbePosition(float p_20309_) {
        return new Vec3(getX(), getY() + getBbHeight(), getZ());
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        var height = isInSittingPose() ? 2.15f : isShiftKeyDown() ? 2.5f : DragonConstants.BASE_HEIGHT;
        var scale = getScale();
        var dimWidth = DragonConstants.BASE_WIDTH * scale;
        var dimHeight = height * scale;
        return EntityDimensions.scalable(dimWidth, dimHeight)
                .withAttachments(EntityAttachments.builder()
                        .attach(EntityAttachment.PASSENGER, 0.0F, dimHeight - 0.15625F, getScale()));
    }

    public TameableDragonEntity getDragon() {
        return (TameableDragonEntity) this;
    }

    public void finalizeDragon(@Nullable TameableDragonEntity parent1, @Nullable TameableDragonEntity parent2) {}
}
