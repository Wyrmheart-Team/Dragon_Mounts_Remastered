package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.ModConstants.NBTConstants;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.registry.ModMemoryModuleTypes;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.server.worlddata.DragonWorldDataManager;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Abstract class that implements dragon owner functionality.
 * This extends the dragon entity hierarchy with owner-related capabilities.
 */
abstract class DragonOwnershipComponent extends DragonMovementComponent {

    protected static final EntityDataAccessor<Optional<GlobalPos>> wanderingPosDataAccessor =
            SynchedEntityData.defineId(DragonOwnershipComponent.class, EntityDataSerializers.OPTIONAL_GLOBAL_POS);
    protected static final EntityDataAccessor<Long> lastPoseChangeTickDataAccessor =
            SynchedEntityData.defineId(DragonOwnershipComponent.class, EntityDataSerializers.LONG);
    protected static final EntityDataAccessor<Boolean> orderedToSitDataAccessor =
            SynchedEntityData.defineId(DragonOwnershipComponent.class, EntityDataSerializers.BOOLEAN);

    protected DragonOwnershipComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(orderedToSitDataAccessor, false);
        builder.define(wanderingPosDataAccessor, Optional.empty());
        builder.define(lastPoseChangeTickDataAccessor, 0L);
    }

    /** Gets the owner of the dragon. */
    public LivingEntity getOwner() {
        var dragon = (TameableDragonEntity) this;

        UUID uuid = dragon.getOwnerUUID();

        if (uuid != null) {
            for (Player player : dragon.level().players()) {
                if (player.getUUID().equals(uuid)) {
                    return player;
                }
            }
        }

        return null;
    }

    @Override
    public void setCustomName(Component pName) {
        super.setCustomName(pName);
        updateOwnerData();
    }

    /** Updates the owner data for the dragon. */
    public void updateOwnerData() {
        if (getOwner() instanceof Player player && !player.level().isClientSide) {
            var handler = player.getData(ModCapabilities.PLAYER_CAPABILITY);

            if (handler.isBoundToWhistle(getDragon())) {
                handler.setPlayerInstance(player);
                handler.setDragonToWhistle(
                        getDragon(),
                        DragonWhistleHandler.getDragonSummonIndex(
                                player, getDragon().getDragonUUID()));
            } else {
                DragonWorldDataManager.addDragonHistory(getDragon());
            }
        }
    }

    /** Tames the dragon for a player. */
    public void tamedFor(Player player, boolean successful) {
        if (successful) {
            setTame(true, true);
            getNavigation().stop();
            setTarget(null);
            setOwnerUUID(player.getUUID());
            level().broadcastEntityEvent(this, (byte) 7);
            updateOwnerData();
        } else {
            level().broadcastEntityEvent(this, (byte) 6);
        }
    }

    /** Checks if the dragon is tamed for a specific player. */
    public boolean isTamedFor(Player player) {
        return isTame() && (isOwnedBy(player) || Objects.equals(getOwnerUUID(), player.getUUID()));
    }

    /**
     * Checks if the dragon is ordered to sit.
     */
    public boolean isOrderedToSit() {
        return entityData.get(orderedToSitDataAccessor);
    }

    /** Sets the dragon to sit or not. */
    public void setOrderedToSit(boolean pOrderedToSit) {
        entityData.set(orderedToSitDataAccessor, pOrderedToSit);
        getNavigation().stop();
        setTarget(null);
        setInSittingPose(pOrderedToSit);
    }

    /** Stops the dragon from sitting. */
    public void stopSitting() {
        setOrderedToSit(false);
        setInSittingPose(false);
        setPose(Pose.STANDING);
    }

    @Override
    public void setInSittingPose(boolean sitting) {
        super.setInSittingPose(sitting);

        if (!sitting) {
            resetLastPoseChangeTickToFullStand(level().getGameTime());
        }
    }

    public boolean isRandomlySitting() {
        return isSitting() && !isOrderedToSit();
    }

    public void setRandomlySitting(boolean sit) {
        if (!isOrderedToSit()) { // Randomly sitting is only possible if the dragon is not told to sit
            setInSittingPose(sit);
        }
    }

    public boolean isSitting() {
        return isInSittingPose();
    }

    /** Checks if an item is a taming item for the dragon. */
    public boolean isTamingItem(ItemStack stack) {
        var list = getBreed().getTamingItems();
        return (!stack.isEmpty()
                && (list != null && !list.isEmpty() ? list.contains(stack.getItem()) : stack.is(ItemTags.FISHES)));
    }

    public boolean hasWanderTarget() {
        if (getWanderTarget().isPresent()) {
            var pos = getWanderTarget().get();
            return pos.dimension() == level.dimension();
        }
        return false;
    }

    /**
     * Gets the dragon's wandering target position.
     */
    public Optional<GlobalPos> getWanderTarget() {
        return entityData.get(wanderingPosDataAccessor);
    }

    /**
     * Sets the dragon's wandering target position.
     */
    public void setWanderTarget(Optional<GlobalPos> pos) {
        entityData.set(wanderingPosDataAccessor, pos);

        if (pos.isEmpty() && getBrain().hasMemoryValue(ModMemoryModuleTypes.SHOULD_WANDER.get())) {
            getBrain().eraseMemory(ModMemoryModuleTypes.SHOULD_WANDER.get());
        } else if (pos.isPresent()) {
            getBrain().setMemory(ModMemoryModuleTypes.SHOULD_WANDER.get(), true);
            stopSitting();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putBoolean(NBTConstants.ORDERED_TO_SIT, isOrderedToSit());

        getWanderTarget()
                .flatMap(p_337878_ ->
                        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, p_337878_).resultOrPartial(System.err::println))
                .ifPresent(p_219756_ -> compound.put(NBTConstants.WANDERING_POS, p_219756_));

        compound.putLong("LastPoseTick", this.entityData.get(lastPoseChangeTickDataAccessor));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains(NBTConstants.ORDERED_TO_SIT)) {
            setOrderedToSit(compound.getBoolean(NBTConstants.ORDERED_TO_SIT));
        }

        Optional<GlobalPos> wanderTarget;
        if (compound.contains(NBTConstants.WANDERING_POS)) {
            wanderTarget = GlobalPos.CODEC
                    .parse(NbtOps.INSTANCE, compound.get(NBTConstants.WANDERING_POS))
                    .resultOrPartial(System.err::println);
            setWanderTarget(wanderTarget);
        }
    }

    public boolean canChangePose() {
        return this.wouldNotSuffocateAtTargetPose(this.isInSittingPose() ? Pose.STANDING : Pose.SITTING);
    }

    public long getPoseTime() {
        return this.level().getGameTime() - Math.abs(this.entityData.get(lastPoseChangeTickDataAccessor));
    }

    public void resetLastPoseChangeTickToFullStand(long lastPoseChangedTick) {
        this.resetLastPoseChangeTick(Math.max(0L, lastPoseChangedTick - 52L - 1L));
    }

    public void resetLastPoseChangeTick(long lastPoseChangeTick) {
        this.entityData.set(lastPoseChangeTickDataAccessor, lastPoseChangeTick);
    }
}
