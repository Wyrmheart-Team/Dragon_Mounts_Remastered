package dmr.DragonMounts.server.entity;

import com.mojang.serialization.Dynamic;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.ModConstants;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler.DragonInstance;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.ModCriterionTriggers;
import dmr.DragonMounts.server.ai.DragonAI;
import dmr.DragonMounts.server.entity.dragon.AbstractDragonEntity;
import dmr.DragonMounts.server.inventory.DragonInventoryHandler.DragonInventory;
import dmr.DragonMounts.server.worlddata.DragonWorldDataManager;
import dmr.DragonMounts.util.PlayerStateUtils;
import java.util.Optional;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.Brain.Provider;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SaddleItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

@Getter
public class TameableDragonEntity extends AbstractDragonEntity {

    public TameableDragonEntity(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return DragonAI.makeBrain(
                (Brain<TameableDragonEntity>) this.brainProvider().makeBrain(dynamic));
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("dragonBrain");
        this.getBrain().tick((ServerLevel) this.level, this);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("dragonActivityUpdate");
        DragonAI.selectMostAppropriateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public Brain<TameableDragonEntity> getBrain() {
        return (Brain<TameableDragonEntity>) super.getBrain();
    }

    @Override
    protected Provider<?> brainProvider() {
        return DragonAI.brainProvider();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return ((wasHatched() || this.tickCount > 2400 || isNaturalSpawn())
                && !isTame()
                && distanceToClosestPlayer > Mth.sqrt(32)
                && !this.hasCustomName());
    }

    @Override
    protected Component getTypeName() {
        if (hasVariant()) {
            return Component.translatable(
                    DMR.MOD_ID + ".dragon_breed." + getBreed().getId() + ModConstants.VARIANT_DIVIDER + getVariantId());
        }

        return getBreed().getName();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        var stackResult = stack.interactLivingEntity(player, this, hand);
        if (stackResult.consumesAction()) return stackResult;

        // tame
        if (!isTame()) {
            if (isServer() && isTamingItem(stack)) {
                stack.shrink(1);
                var shouldTame = getRandom().nextInt(5) == 0;
                tamedFor(player, shouldTame);

                if (shouldTame) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        ModCriterionTriggers.TAME_DRAGON.get().trigger(serverPlayer);
                    }
                }

                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS; // pass regardless. We don't want to perform breeding, age ups, etc. on
            // untamed.
        }

        // heal
        if ((getHealthRelative() < 1 && getHealth() < (getMaxHealth() - 1)) && isFoodItem(stack)) {
            // noinspection ConstantConditions
            heal(stack.getItem().getFoodProperties(stack, this).nutrition());
            playSound(getEatingSound(stack), 0.7f, 1);
            stack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // saddle up!
        if (isTamedFor(player) && isSaddleable() && !isSaddled() && stack.getItem() instanceof SaddleItem) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            equipSaddle(stack, getSoundSource());
            updateContainerEquipment();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // equip armor
        if (isTamedFor(player) && isArmor(stack)) {
            equipArmor(player, stack);
            updateContainerEquipment();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (isTamedFor(player) && !hasChest() && stack.is(Items.CHEST)) {
            this.getInventory().setItem(DragonInventory.CHEST_SLOT, stack.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            updateContainerEquipment();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // open menu
        if (isTamedFor(player) && player.isSecondaryUseActive()) {
            if (!level.isClientSide) this.openCustomInventoryScreen(player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // ride on
        if (isTamedFor(player) && isSaddled() && !isHatchling() && !isFood(stack)) {
            if (isServer()) {
                setRidingPlayer(player);
                navigation.stop();
            }
            setTarget(null);
            setWanderTarget(Optional.empty());
            stopSitting();
            getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            updateOwnerData();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void baseTick() {
        super.baseTick();

        if (!this.level.isClientSide && this.isAlive() && this.tickCount % 20 == 0) {
            this.heal((float) ServerConfig.HEALTH_REGEN);
        }

        if (getDragonInventory() != null && getDragonInventory().isDirty()) {
            updateContainerEquipment();
        }
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (DATA_FLAGS_ID.equals(data)) {
            refreshDimensions();
        } else {
            super.onSyncedDataUpdated(data);
        }
    }

    @Override
    public @Nullable Entity changeDimension(DimensionTransition transition) {
        var entity = super.changeDimension(transition);

        if (entity instanceof TameableDragonEntity dragon) {
            var owner = getOwner();

            DMR.LOGGER.debug(
                    "Changing dimension of dragon {} to {}",
                    getDragonUUID(),
                    transition.newLevel().dimension().location());

            if (owner instanceof Player player) {
                var handler = PlayerStateUtils.getHandler(player);
                var index = DragonWhistleHandler.getDragonSummonIndex(player, getDragonUUID());
                handler.setDragonInstance(index, new DragonInstance(dragon));

                // Update lastSummon to new UUID to prevent despawns
                if (handler.lastSummons.get(index) != null
                        && handler.lastSummons.get(index).equals(getUUID())) {
                    handler.lastSummons.put(index, entity.getUUID());
                }
            }

            var worldData1 = DragonWorldDataManager.getInstance(level);
            var worldData2 = DragonWorldDataManager.getInstance(transition.newLevel());

            // Transfer the dragon inventory
            worldData2.dragonInventories.put(getDragonUUID(), worldData1.dragonInventories.get(getDragonUUID()));
            worldData1.dragonInventories.remove(getDragonUUID());

            return dragon;
        }

        return null;
    }
	
	@Override
	public void travel(Vec3 travelVector) {
		if (!this.isInWater() || !this.canDrownInFluidType(Fluids.WATER.getFluidType())) {
			super.travel(travelVector);
			return;
		}
		
		double y0 = this.getY();
		float waterFriction = this.isSprinting() ? 0.7F : 0.5F;
		float inWaterSpeedModifier = (float) this.getAttributeValue(NeoForgeMod.SWIM_SPEED);
		
		this.moveRelative(inWaterSpeedModifier, travelVector);
		this.move(MoverType.SELF, this.getDeltaMovement());
		
		Vec3 deltaVector = this.getDeltaMovement();
		
		if (this.horizontalCollision && this.onClimbable()) {
			deltaVector = new Vec3(deltaVector.x, 0.2, deltaVector.z);
		}
		
		Vec3 waterDragVector = new Vec3(
				deltaVector.x * waterFriction * 0.6D,
				deltaVector.y * getWaterSlowDown(),
				deltaVector.z * waterFriction * 0.6D
		);
		
		this.setDeltaMovement(waterDragVector);
		Vec3 nextDeltaVector = this.getDeltaMovement();
		if (this.horizontalCollision && this.isFree(nextDeltaVector.x, nextDeltaVector.y + 0.6D - this.getY() + y0, nextDeltaVector.z)) {
			this.setDeltaMovement(nextDeltaVector.x, 0.3D, nextDeltaVector.z);
		}
	}
}
