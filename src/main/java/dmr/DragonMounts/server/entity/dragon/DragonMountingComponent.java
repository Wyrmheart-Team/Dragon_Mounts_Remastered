package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.client.handlers.KeyInputHandler;
import dmr.DragonMounts.server.entity.DragonConstants;
import dmr.DragonMounts.util.PlayerStateUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Abstract class that implements dragon riding functionality.
 * This extends the dragon entity hierarchy with riding capabilities.
 */
abstract class DragonMountingComponent extends DragonOwnershipComponent {

    protected DragonMountingComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Positions the rider on the dragon.
     */
    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction callback) {
        LivingEntity riddenByEntity = getControllingPassenger();
        if (riddenByEntity != null) {
            boolean customRidingPos = false;

            if (!customRidingPos) {
                Vec3 vec3 = getPassengerRidingPosition(passenger);
                Vec3 vec31 = passenger.getVehicleAttachmentPoint(this);
                Vec3 riderPos = new Vec3(vec3.x - vec31.x, vec3.y - vec31.y, vec3.z - vec31.z);
                callback.accept(passenger, riderPos.x, riderPos.y, riderPos.z);
            }

            // fix rider rotation
            if (getFirstPassenger() instanceof LivingEntity) {
                riddenByEntity.xRotO = riddenByEntity.getXRot();
                riddenByEntity.yRotO = riddenByEntity.getYRot();
                riddenByEntity.yBodyRot = yBodyRot;
            }
        }
    }

    /**
     * Processes rider input for controlling the dragon.
     */
    @Override
    protected Vec3 getRiddenInput(Player driver, Vec3 move) {

        double moveSideways = move.x;
        double moveY = 0;
        double moveForward = Math.min(Math.abs(driver.zza) + Math.abs(driver.xxa), 1);
        var handler = PlayerStateUtils.getHandler(driver);

        if (isFlying()) {
            moveForward = moveForward > 0 ? moveForward : 0;
            if (moveForward > 0 && handler.cameraFlight) moveY = -(driver.getXRot() * (Math.PI / 180) * 0.5f);

            if (driver.jumping) moveY += 0.5;
            if (driver.isShiftKeyDown()) {
                moveY += -0.5;
            } else if (level().isClientSide()) {
                if (KeyInputHandler.DESCEND_KEY.isDown()) {
                    moveY -= 0.5;
                }
            }
        } else if (isInFluidType()) {
            moveForward = moveForward > 0 ? moveForward : 0;

            if (moveForward > 0 && handler.cameraFlight) {
                moveY = (-driver.getXRot() * (Math.PI / 180)) * 2;
            }

            if (driver.jumping) moveY += 2;
            if (driver.isShiftKeyDown()) {
                moveY += -2;
            } else if (level().isClientSide()) {
                if (KeyInputHandler.DESCEND_KEY.isDown()) {
                    moveY += -0.5;
                }
            }
        }

        float f = isShiftKeyDown() ? 0.3F : 1f;
        Vec3 movement = new Vec3(moveSideways * f, moveY, moveForward * f);

        return maybeBackOffFromEdge(movement, MoverType.SELF);
    }

    @Override
    public boolean canSprint() {
        return true;
    }

    protected boolean isAboveGround() {
        return (this.onGround()
                || (this.fallDistance < this.maxUpStep()
                        && !this.level()
                                .noCollision(
                                        this,
                                        this.getBoundingBox().move(0.0, this.fallDistance - this.maxUpStep(), 0.0))));
    }

    public Vec3 maybeBackOffFromEdge(Vec3 pVec, MoverType pMover) {
        if (pVec.y <= 0.0 && this.isShiftKeyDown() && isAboveGround()) {
            double d0 = pVec.x;
            double d1 = pVec.z;

            while (d0 != 0.0
                    && this.level.noCollision(this, this.getBoundingBox().move(d0, -this.maxUpStep(), 0.0))) {
                if (d0 < 0.05 && d0 >= -0.05) {
                    d0 = 0.0;
                } else if (d0 > 0.0) {
                    d0 -= 0.05;
                } else {
                    d0 += 0.05;
                }
            }

            while (d1 != 0.0
                    && this.level.noCollision(this, this.getBoundingBox().move(0.0, -this.maxUpStep(), d1))) {
                if (d1 < 0.05 && d1 >= -0.05) {
                    d1 = 0.0;
                } else if (d1 > 0.0) {
                    d1 -= 0.05;
                } else {
                    d1 += 0.05;
                }
            }

            while (d0 != 0.0
                    && d1 != 0.0
                    && this.level.noCollision(this, this.getBoundingBox().move(d0, -this.maxUpStep(), d1))) {
                if (d0 < 0.05 && d0 >= -0.05) {
                    d0 = 0.0;
                } else if (d0 > 0.0) {
                    d0 -= 0.05;
                } else {
                    d0 += 0.05;
                }

                if (d1 < 0.05 && d1 >= -0.05) {
                    d1 = 0.0;
                } else if (d1 > 0.0) {
                    d1 -= 0.05;
                } else {
                    d1 += 0.05;
                }
            }

            pVec = new Vec3(d0, pVec.y, d1);
        }

        return pVec;
    }

    /**
     * Handles the dragon's behavior when being ridden.
     */
    @Override
    protected void tickRidden(Player driver, Vec3 move) {
        super.tickRidden(driver, move);

        // rotate head to match driver
        float yaw = driver.yHeadRot;
        if (move.z > 0) { // rotate in the direction of the drivers controls
            yaw += (float) Mth.atan2(driver.zza, driver.xxa) * (180f / (float) Math.PI) - 90;
        }
        yHeadRot = driver.yHeadRot;
        setXRot(driver.getXRot() * 0.68f);

        // rotate body towards the head
        setYRot(Mth.rotateIfNecessary(yaw, getYRot(), 8));

        if (isControlledByLocalInstance()) {
            if (driver.jumping) {
                if (!isFlying() && canFly()) {
                    liftOff();
                } else if (onGround() && !canFly()) {
                    jumpFromGround();
                }
            }
        }
    }

    /**
     * Updates the dragon's state based on rider input.
     */
    public void tick() {
        super.tick();

        if (getPose() == Pose.STANDING) {
            if (isShiftKeyDown()) setPose(Pose.CROUCHING);
        } else if (getPose() == Pose.CROUCHING) {
            if (!isShiftKeyDown()) setPose(Pose.STANDING);
        }
    }

    public void baseTick() {
        super.baseTick();

        if (isControlledByLocalInstance() && getControllingPassenger() != null) {
            if (isRandomlySitting()) {
                setRandomlySitting(false);
            }

            setSprinting(getControllingPassenger().isSprinting());
        }
    }

    /**
     * Sets the player as riding the dragon.
     */
    public void setRidingPlayer(Player player) {
        player.setYRot(getYRot());
        player.setXRot(getXRot());
        player.startRiding(this);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity) {
        Vec3 vec3 = getCollisionHorizontalEscapeVector(
                this.getBbWidth(),
                pLivingEntity.getBbWidth(),
                this.getYRot() + (pLivingEntity.getMainArm() == HumanoidArm.RIGHT ? 90.0F : -90.0F));
        Vec3 vec31 = this.getDismountLocationInDirection(vec3, pLivingEntity);
        if (vec31 != null) {
            return vec31;
        } else {
            Vec3 vec32 = getCollisionHorizontalEscapeVector(
                    this.getBbWidth(),
                    pLivingEntity.getBbWidth(),
                    this.getYRot() + (pLivingEntity.getMainArm() == HumanoidArm.LEFT ? 90.0F : -90.0F));
            Vec3 vec33 = this.getDismountLocationInDirection(vec32, pLivingEntity);
            return vec33 != null ? vec33 : this.position();
        }
    }

    /**
     * Determines the suitable dismount location for a passenger in a specific direction.
     */
    public Vec3 getDismountLocationInDirection(Vec3 pDirection, LivingEntity pPassenger) {
        double d0 = getX() + pDirection.x;
        double d1 = getBoundingBox().minY;
        double d2 = getZ() + pDirection.z;
        BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

        for (Pose pose : pPassenger.getDismountPoses()) {
            blockpos_mutableblockpos.set(d0, d1, d2);
            double d3 = getBoundingBox().maxY + 0.75D;

            while (true) {
                double d4 = level.getBlockFloorHeight(blockpos_mutableblockpos);
                if ((double) blockpos_mutableblockpos.getY() + d4 > d3) {
                    break;
                }

                if (DismountHelper.isBlockFloorValid(d4)) {
                    AABB aabb = pPassenger.getLocalBoundsForPose(pose);
                    Vec3 vec3 = new Vec3(d0, (double) blockpos_mutableblockpos.getY() + d4, d2);
                    if (DismountHelper.canDismountTo(level, pPassenger, aabb.move(vec3))) {
                        pPassenger.setPose(pose);
                        return vec3;
                    }
                }

                blockpos_mutableblockpos.move(Direction.UP);
                if (!((double) blockpos_mutableblockpos.getY() < d3)) {
                    break;
                }
            }
        }

        return null;
    }

    @Override
    public boolean isShiftKeyDown() {
        if (getControllingPassenger() != null && getControllingPassenger().isShiftKeyDown()) {
            return true;
        }

        if (getControllingPassenger() == null && getOwner() != null) {
            if (!hasWanderTarget() && !isOrderedToSit() && getPose() != Pose.SLEEPING) {
                if (getOwner() instanceof Player player && distanceTo(player) <= DragonConstants.BASE_FOLLOW_RANGE) {
                    return player.isShiftKeyDown();
                }
            }
        }

        return super.isShiftKeyDown();
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof LivingEntity driver && isOwnedBy(driver) ? driver : null;
    }
}
