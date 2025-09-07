package dmr.DragonMounts.server.entity.dragon;

import static net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED;

import dmr.DragonMounts.registry.ModMemoryModuleTypes;
import dmr.DragonMounts.server.ai.DragonBodyController;
import dmr.DragonMounts.server.ai.DragonMoveController;
import dmr.DragonMounts.server.ai.navigation.DragonPathNavigation;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Abstract class that implements additional dragon movement functionality.
 * This extends the dragon entity hierarchy with more advanced movement capabilities.
 */
@Getter
abstract class DragonMovementComponent extends DragonInventoryComponent {

    protected boolean isFlying = false;

    protected DragonMovementComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        moveControl = new DragonMoveController(getDragon());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        DragonPathNavigation dragonNavigation = new DragonPathNavigation((TameableDragonEntity) this, level);
        dragonNavigation.setCanFloat(true);
        return dragonNavigation;
    }

    @Override
    public BodyRotationControl createBodyControl() {
        return new DragonBodyController(this);
    }

    /**
     * Sets the flying state of the dragon.
     */
    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    /**
     * Gets the flying speed of the dragon.
     */
    @Override
    public float getFlyingSpeed() {
        return (isSprinting() ? 1.25f : 1) * (float) getAttributeValue(FLYING_SPEED);
    }

    /**
     * Gets the movement speed of the dragon.
     */
    @Override
    public float getSpeed() {
        return ((isSprinting() ? 1.25f : 1) * (float) getAttributeValue(Attributes.MOVEMENT_SPEED));
    }
    /** Checks if the dragon can fly. */
    public boolean canFly() {
        // hatchling's can't fly
        return !isHatchling() && getEyeInFluidType().isAir();
    }

    /** Makes the dragon lift off from the ground. */
    public void liftOff() {
        if (canFly()) jumpFromGround();
    }

    /**
     * Checks if the dragon should fly.
     */
    public boolean shouldFly() {
        if (!canFly()) return false;
        if (isFlying()) return !onGround(); // more natural landings
        return canFly() && !isInWater() && !isNearGround() && !jumping;
    }

    private static final int GROUND_CLEARENCE_THRESHOLD = 1;

    /** Checks if the dragon is near the ground. */
    public boolean isNearGround() {
        var dimensions = getDimensions(getPose());
        return (onGround()
                || !level().noCollision(
                                this,
                                new AABB(
                                        getX() - dimensions.width() / 2,
                                        getY(),
                                        getZ() - dimensions.width() / 2,
                                        getX() + dimensions.width() / 2,
                                        getY() - (GROUND_CLEARENCE_THRESHOLD * getScale()),
                                        getZ() + dimensions.width() / 2)));
    }

    public void tick() {
        super.tick();

        boolean flying = shouldFly();
        if (flying != isFlying()) {
            setFlying(flying);
        }

        if (isNoGravity() != shouldFly() && !isInWater()) {
            setNoGravity(shouldFly());
        }

        if (getControllingPassenger() == null && !getDragon().hasWanderTarget() && !isOrderedToSit()) {
            if (isPathFinding()) {
                var dest = getNavigation().getTargetPos();
                var farDist = dest.distManhattan(blockPosition()) >= 16d;
                setSprinting(farDist);
            } else {
                if (isSprinting()) setSprinting(false);
            }
        }

        if (isPathFinding()) {
            if (getNavigation().getPath() != null) {
                var type = getNavigation().getPath().getNextNode().type;
                if (type == PathType.WALKABLE) {
                    setFlying(false);
                } else if (type == PathType.WATER || type == PathType.WATER_BORDER) {
                    setFlying(false);
                    setSwimming(true);
                }
            }
        }
    }

    public void aiStep() {
        super.aiStep();

        if (getControllingPassenger() == null && !getDragon().hasWanderTarget() && !isOrderedToSit()) {
            if (isPathFinding()) {
                var dest = getNavigation().getTargetPos();
                var farDist = dest.distManhattan(blockPosition()) >= 16d;
                setSprinting(farDist);
            } else {
                if (isSprinting()) setSprinting(false);
            }
        }
    }

    /** Checks if the dragon is affected by fluids. */
    public boolean isAffectedByFluids() {
        return canDrownInFluidType(Fluids.WATER.getFluidType());
    }
	
	@Override
	public float getWaterSlowDown() {
		return 0.5f;
	}

    /** Checks if the dragon can drown in a specific fluid type. */
    public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        if (type == Fluids.WATER.getFluidType()) {
            if (getBreed() != null && getBreed().getImmunities().contains("drown")) {
                return false;
            }
        }
        return super.canDrownInFluidType(type);
    }

    /**
     * Gets the pathfinding malus for a specific path type.
     */
    @Override
    public float getPathfindingMalus(PathType pathType) {
        var originalMalus = super.getPathfindingMalus(pathType);
        if (pathType == PathType.WATER) {
            return canDrownInFluidType(NeoForgeMod.WATER_TYPE.getDelegate().value())
                    ? originalMalus
                    : originalMalus * 2f;
        }

        if (pathType == PathType.OPEN) {
            return originalMalus * 16.0F;
        }

        return originalMalus;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return 0.0F;
    }

    @Override
    public void refreshDimensions() {
        double posXTmp = getX();
        double posYTmp = getY();
        double posZTmp = getZ();
        boolean onGroundTmp = onGround();

        super.refreshDimensions();

        // workaround for a vanilla bug; the position is apparently not set correcty
        // after changing the entity size, causing asynchronous server/client
        // positioning
        setPos(posXTmp, posYTmp, posZTmp);

        // otherwise, setScale stops the dragon from landing while it is growing
        setOnGround(onGroundTmp);
    }

    @Override
    protected void onChangedBlock(ServerLevel level, BlockPos pos) {
        super.onChangedBlock(level, pos);
        getBreed().onMove(getDragon());
        getBrain().eraseMemory(ModMemoryModuleTypes.IDLE_TICKS.get());
    }
}
