package dmr.DragonMounts.server.entity;

import static net.minecraft.world.entity.ai.attributes.Attributes.*;
import static net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Dynamic;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.client.handlers.KeyInputHandler;
import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.network.packets.DragonAgeSyncPacket;
import dmr.DragonMounts.registry.*;
import dmr.DragonMounts.server.ai.DragonAI;
import dmr.DragonMounts.server.ai.DragonMoveController;
import dmr.DragonMounts.server.blocks.DMREggBlock;
import dmr.DragonMounts.server.container.DragonContainerMenu;
import dmr.DragonMounts.server.items.DragonArmorItem;
import dmr.DragonMounts.server.items.DragonSpawnEgg;
import dmr.DragonMounts.server.worlddata.DragonWorldDataManager;
import dmr.DragonMounts.types.armor.DragonArmor;
import dmr.DragonMounts.util.BreedingUtils;
import dmr.DragonMounts.util.PlayerStateUtils;
import io.netty.buffer.Unpooled;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.Brain.Provider;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SaddleItem;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3d;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DMRDragonEntity extends AbstractDMRDragonEntity {

	// base attributes
	public static final double BASE_SPEED_GROUND = 0.25;
	public static final double BASE_SPEED_WATER = 0.25;
	public static final double BASE_SPEED_FLYING = 0.2;
	public static final double BASE_DAMAGE = 8;
	public static final double BASE_HEALTH = 60;
	public static final double BASE_FOLLOW_RANGE = 32;
	public static final int BASE_KB_RESISTANCE = 1;
	public static final float BASE_WIDTH = 2.75f; // adult sizes
	public static final float BASE_HEIGHT = 2.75f;
	public static final int BREATH_COUNT = 5;
	// other constants
	public static final ResourceLocation SCALE_MODIFIER = ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, "scale_attribute"); // just a random uuid I took online
	public static final int GROUND_CLEARENCE_THRESHOLD = 1;
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
	public static final RawAnimation BREATH = RawAnimation.begin().thenPlayXTimes("breath", BREATH_COUNT);
	public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
	private static final ResourceLocation ARMOR_MODIFIER = ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, "armor_attribute");
	private static final double breathLength = 0.5 * BREATH_COUNT;
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	public Vector3d breathSourcePosition;
	public AnimationController<?> animationController;
	public AnimationController<?> headController;
	private long breathTime = -1;

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return DragonAI.makeBrain((Brain<DMRDragonEntity>) this.brainProvider().makeBrain(dynamic));
	}

	public DMRDragonEntity(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
		moveControl = new DragonMoveController(this);
		setDragonUUID(UUID.randomUUID());
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(MOVEMENT_SPEED, BASE_SPEED_GROUND)
			.add(MAX_HEALTH, BASE_HEALTH)
			.add(FOLLOW_RANGE, BASE_FOLLOW_RANGE)
			.add(KNOCKBACK_RESISTANCE, BASE_KB_RESISTANCE)
			.add(ATTACK_DAMAGE, BASE_DAMAGE)
			.add(FLYING_SPEED, BASE_SPEED_FLYING)
			.add(SWIM_SPEED, BASE_SPEED_WATER);
	}

	@Override
	protected Provider<?> brainProvider() {
		return DragonAI.brainProvider();
	}

	@Override
	protected void customServerAiStep() {
		this.level().getProfiler().push("dragonBrain");
		this.getBrain().tick((ServerLevel) this.level, this);
		this.level().getProfiler().pop();
		this.level().getProfiler().push("dragonActivityUpdate");
		DragonAI.updateActivity(this);
		this.level().getProfiler().pop();
		super.customServerAiStep();
	}

	@Override
	public Brain<DMRDragonEntity> getBrain() {
		return (Brain<DMRDragonEntity>) super.getBrain();
	}

	@Override
	public boolean isSaddleable() {
		return isAlive() && !isHatchling() && isTame();
	}

	@Override
	public void aiStep() {
		super.aiStep();

		if (isNoGravity() != shouldFly()) {
			setNoGravity(shouldFly());
		}

		if (getControllingPassenger() == null && !hasWanderTarget() && !isOrderedToSit()) {
			if (isPathFinding()) {
				var dest = getNavigation().getTargetPos();
				var farDist = dest.distManhattan(blockPosition()) >= 16d;
				setSprinting(farDist);
			} else {
				if (isSprinting()) setSprinting(false);
			}
		}
	}

	@Override
	public void equipSaddle(ItemStack stack, SoundSource source) {
		setSaddled(true);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_SADDLE, getSoundSource(), 1, 1);
		inventory.setItem(SADDLE_SLOT, stack);
	}

	@Override
	public ItemStack getPickedResult(HitResult target) {
		return DragonSpawnEgg.create(getBreed());
	}

	public void equipArmor(Player pPlayer, ItemStack pArmor) {
		if (!isWearingArmor()) {
			this.setItemSlot(EquipmentSlot.BODY, pArmor.copyWithCount(1));
			if (!pPlayer.getAbilities().instabuild) {
				pArmor.shrink(1);
			}
			setArmor();
		}
	}

	@Override
	protected boolean isAffectedByFluids() {
		return canDrownInFluidType(Fluids.WATER.getFluidType());
	}

	@Override
	public boolean canDrownInFluidType(FluidType type) {
		if (type == Fluids.WATER.getFluidType()) {
			if (getBreed() != null && getBreed().getImmunities().contains("drown")) {
				return false;
			}
		}
		return super.canDrownInFluidType(type);
	}

	public void setArmor() {
		ItemStack itemstack = this.getBodyArmorItem();
		if (!this.level().isClientSide) {
			this.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER);
			if (this.isArmor(itemstack)) {
				DragonArmor armor = DragonArmor.getArmorType(itemstack);
				if (armor != null) {
					int i = armor.getProtection();
					if (i != 0) {
						this.getAttribute(Attributes.ARMOR).addTransientModifier(
								new AttributeModifier(ARMOR_MODIFIER, i, Operation.ADD_VALUE)
							);
					}
				}
			}
		}
	}

	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		addDragonAnimations(controllers);
	}

	public boolean isArmor(ItemStack pStack) {
		return pStack.getItem() instanceof DragonArmorItem;
	}

	private void addDragonAnimations(ControllerRegistrar data) {
		headController = new AnimationController<>(this, "head-controller", 0, state -> {
			return state.setAndContinue(isFlying() ? NECK_TURN_FLIGHT : NECK_TURN);
		});

		headController.triggerableAnim("bite", BITE);
		headController.triggerableAnim("breath", BREATH);
		data.add(headController);

		animationController = new AnimationController<>(this, "controller", 5, state -> {
			Vec3 motio = new Vec3(getX() - xo, getY() - yo, getZ() - zo);
			boolean isMovingHorizontal = Math.sqrt(Math.pow(motio.x, 2) + Math.pow(motio.z, 2)) > 0.05;
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
			} else if (isRandomlySitting()) {
				return state.setAndContinue(SIT);
			} else if (isOrderedToSit()) {
				var lookAtContext = TargetingConditions.forNonCombat()
					.range(10)
					.selector(p_25531_ -> EntitySelector.notRiding(this).test(p_25531_));
				var lookAt = level.getNearestPlayer(lookAtContext, this, getX(), getEyeY(), getZ());

				if (lookAt != null) {
					return state.setAndContinue(SIT_ALT);
				} else {
					return state.setAndContinue(SIT);
				}
			} else if (isMovingHorizontal) {
				if (isSprinting()) {
					return state.setAndContinue(SPRINT);
				} else {
					state.setControllerSpeed(1.1f + (isShiftKeyDown() ? 2f : getSpeed()));
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

	public boolean canFly() {
		// hatchling's can't fly
		return !isHatchling() && getEyeInFluidType().isAir();
	}

	@Override
	public boolean isShiftKeyDown() {
		if (getControllingPassenger() != null && getControllingPassenger().isShiftKeyDown()) {
			return true;
		}

		if (getControllingPassenger() == null && getOwner() != null) {
			if (!hasWanderTarget() && !isOrderedToSit() && getPose() != Pose.SLEEPING) {
				if (getOwner() instanceof Player player && distanceTo(player) <= BASE_FOLLOW_RANGE) {
					return player.isShiftKeyDown();
				}
			}
		}

		return super.isShiftKeyDown();
	}

	public void liftOff() {
		if (canFly()) jumpFromGround();
	}

	@Override
	public float getSpeed() {
		return (isSprinting() ? 1.25f : 1) * (float) getAttributeValue(MOVEMENT_SPEED);
	}

	public void onFlap() {
		if (this.level().isClientSide && !this.isSilent()) {
			this.level()
				.playLocalSound(
					this.getX(),
					this.getY(),
					this.getZ(),
					getWingsSound(),
					this.getSoundSource(),
					2.0F,
					0.8F + this.random.nextFloat() * 0.3F,
					false
				);
		}
	}

	@Override
	public boolean fireImmune() {
		return super.fireImmune() || (getBreed() != null && getBreed().getImmunities().contains("onFire"));
	}

	@Override
	public Vec3 getLightProbePosition(float p_20309_) {
		return new Vec3(getX(), getY() + getBbHeight(), getZ());
	}

	@Override
	public void positionRider(Entity passenger, Entity.MoveFunction pCallback) {
		LivingEntity riddenByEntity = getControllingPassenger();
		if (riddenByEntity != null) {
			boolean customRidingPos = false;

			if (!customRidingPos) {
				Vec3 vec3 = this.getPassengerRidingPosition(passenger);
				Vec3 vec31 = passenger.getVehicleAttachmentPoint(this);
				Vec3 riderPos = new Vec3(vec3.x - vec31.x, vec3.y - vec31.y, vec3.z - vec31.z); //.yRot((float) Math.toRadians(-yBodyRot)).add(0, getBbHeight() + breed.getVerticalRidingOffset(), getScale())
				pCallback.accept(passenger, riderPos.x, riderPos.y, riderPos.z);
			}

			// fix rider rotation
			if (getFirstPassenger() instanceof LivingEntity) {
				riddenByEntity.xRotO = riddenByEntity.getXRot();
				riddenByEntity.yRotO = riddenByEntity.getYRot();
				riddenByEntity.yBodyRot = yBodyRot;
			}
		}
	}

	public SoundEvent getWingsSound() {
		return SoundEvents.ENDER_DRAGON_FLAP;
	}

	@Override
	public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
		return !canFly() && super.causeFallDamage(pFallDistance, pMultiplier, pSource);
	}

	@Nullable
	@Override
	public LivingEntity getTarget() {
		return this.getTargetFromBrain();
	}

	@Override
	public void setCustomName(Component pName) {
		super.setCustomName(pName);
		updateOwnerData();
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	public void updateAgeProperties() {
		refreshDimensions();

		AttributeInstance stepHeightInstance = getAttribute(STEP_HEIGHT);
		stepHeightInstance.setBaseValue(Math.max(2 * getAgeProgress(), 1));

		AttributeInstance baseHealthInstance = getAttribute(MAX_HEALTH);
		baseHealthInstance.setBaseValue(ServerConfig.BASE_HEALTH.get());

		AttributeInstance attackDamageInstance = getAttribute(ATTACK_DAMAGE);
		attackDamageInstance.setBaseValue(ServerConfig.BASE_DAMAGE.get());

		var mod = new AttributeModifier(SCALE_MODIFIER, getScale(), Operation.ADD_VALUE);
		for (var attribute : new Holder[] { MAX_HEALTH, ATTACK_DAMAGE }) { // avoid duped code
			AttributeInstance instance = getAttribute(attribute);
			instance.removeModifier(SCALE_MODIFIER);
			instance.addTransientModifier(mod);
		}
	}

	@Override
	public void refreshDimensions() {
		double posXTmp = getX();
		double posYTmp = getY();
		double posZTmp = getZ();
		boolean onGroundTmp = onGround();

		super.refreshDimensions();

		// workaround for a vanilla bug; the position is apparently not set correcty
		// after changing the entity size, causing asynchronous server/client positioning
		setPos(posXTmp, posYTmp, posZTmp);

		// otherwise, setScale stops the dragon from landing while it is growing
		setOnGround(onGroundTmp);
	}

	@Override
	public float getPathfindingMalus(PathType pathType) {
		var originalMalus = super.getPathfindingMalus(pathType);
		if (pathType == PathType.WATER) {
			return canDrownInFluidType(NeoForgeMod.WATER_TYPE.getDelegate().value()) ? originalMalus : originalMalus * 8f;
		}

		if (pathType == PathType.OPEN) {
			return originalMalus * 16.0F;
		}

		return originalMalus;
	}

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
			if (driver.isShiftKeyDown()) moveY += -0.5;
			else if (level.isClientSide()) {
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
			if (driver.isShiftKeyDown()) moveY += -2;
			else if (level.isClientSide()) {
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
	public void baseTick() {
		super.baseTick();

		if (!this.level.isClientSide && this.isAlive() && this.tickCount % 20 == 0) {
			this.heal(1.0F);
		}

		if (isControlledByLocalInstance() && getControllingPassenger() != null) {
			setSprinting(getControllingPassenger().isSprinting());
		}
	}

	@Override
	protected void tickRidden(Player driver, Vec3 move) {
		super.tickRidden(driver, move);
		// rotate head to match driver.
		float yaw = driver.yHeadRot;
		if (move.z > 0) { // rotate in the direction of the drivers controls1
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

	@Override
	public EntityDimensions getDimensions(Pose poseIn) {
		var height = isInSittingPose() ? 2.15f : isShiftKeyDown() ? 2.5f : BASE_HEIGHT;
		var scale = getScale();
		var dimWidth = BASE_WIDTH * scale;
		var dimHeight = height * scale;
		return EntityDimensions.scalable(dimWidth, dimHeight).withAttachments(
			EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0F, dimHeight - 0.15625F, getScale())
		);
	}

	@Override
	protected float getFlyingSpeed() {
		return (isSprinting() ? 1.25f : 1) * (float) getAttributeValue(FLYING_SPEED);
	}

	@Override
	public float getScale() {
		var scale = getBreed() != null ? getBreed().getSizeModifier() : 1;
		return scale * (isBaby() ? 0.5f : 1f);
	}

	public boolean shouldFly() {
		if (!canFly()) return false;
		if (isFlying()) return !onGround(); // more natural landings
		return canFly() && !isInWater() && !isNearGround() && !jumping;
	}

	public boolean isTamingItem(ItemStack stack) {
		var list = getBreed().getTamingItems();
		return !stack.isEmpty() && (list != null && !list.isEmpty() ? list.contains(stack.getItem()) : stack.is(ItemTags.FISHES));
	}

	@Override
	public void tick() {
		super.tick();

		if (getPose() == Pose.STANDING) {
			if (isShiftKeyDown()) setPose(Pose.CROUCHING);
		} else if (getPose() == Pose.CROUCHING) {
			if (!isShiftKeyDown()) setPose(Pose.STANDING);
		}

		if (this.breed == null && getBreed() != null) {
			setBreed(getBreed());
		}

		// update nearGround state when moving for flight and animation logic
		var dimensions = getDimensions(getPose());
		nearGround =
			onGround() ||
			!level.noCollision(
				this,
				new AABB(
					getX() - dimensions.width() / 2,
					getY(),
					getZ() - dimensions.width() / 2,
					getX() + dimensions.width() / 2,
					getY() - (GROUND_CLEARENCE_THRESHOLD * getScale()),
					getZ() + dimensions.width() / 2
				)
			);

		// update flying state based on the distance to the ground
		boolean flying = shouldFly();
		if (flying != isFlying()) {
			setFlying(flying);
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

		if (tickCount % 20 == 0) getBreed().tick(this);

		if (hasBreathAttack() && !level.isClientSide) {
			if (breathTime != -1) {
				doBreathAttack();
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void renderDragonBreath(float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer) {
		if (getControllingPassenger() == null) return;

		double yawRadians = Math.toRadians(entityYaw);
		double f4 = -Math.sin(yawRadians);
		double f5 = Math.cos(yawRadians);
		Vec3 lookVector = new Vec3(f4, 0, f5);

		var viewVector = getControllingPassenger().getViewVector(1f);

		if (breathSourcePosition != null) {
			for (int i = 0; i < 20; i++) {
				Vec3 speed = new Vec3(
					lookVector.x * (0.5f + (random.nextFloat() / 2)),
					viewVector.y,
					lookVector.z * (0.5f + (random.nextFloat() / 2))
				);

				var particle = ParticleTypes.FLAME;
				level.addParticle(
					particle,
					getX() + breathSourcePosition.x,
					getY() + breathSourcePosition.y,
					getZ() + breathSourcePosition.z,
					speed.x,
					speed.y,
					speed.z
				);
			}
		}
	}

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
					getZ() + (dimensions.width() / 2) + lookVector.z * size
				);
				var entities = level.getNearbyEntities(
					LivingEntity.class,
					breathAttackTargetConditions(),
					getControllingPassenger(),
					offsetBoundingBox
				);

				entities.stream().filter(e -> e != this && e != getControllingPassenger()).forEach(this::attackWithBreath);
			}
		}
	}

	public TargetingConditions breathAttackTargetConditions() {
		return TargetingConditions.forCombat().ignoreInvisibilityTesting().selector(this::canHarmWithBreath);
	}

	public boolean hasBreathAttack() {
		return true;
	}

	public boolean canHarmWithBreath(LivingEntity target) {
		return Objects.requireNonNull(getOwner()).canAttack(target) && !target.isAlliedTo(getOwner());
	}

	public void attackWithBreath(LivingEntity target) {
		target.hurt(level.damageSources().mobAttack(this), 2);
		target.setRemainingFireTicks(5);
	}

	@Override
	protected void tickDeath() {
		// unmount any riding entities
		ejectPassengers();

		// freeze at place
		setDeltaMovement(Vec3.ZERO);
		setYRot(yRotO);
		setYHeadRot(yHeadRotO);

		if (deathTime >= getMaxDeathTime()) remove(RemovalReason.KILLED); // actually delete entity after the time is up

		deathTime++;
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
				tamedFor(player, getRandom().nextInt(5) == 0);

				if (player instanceof ServerPlayer serverPlayer) {
					ModCriterionTriggers.TAME_DRAGON.get().trigger(serverPlayer);
				}

				return InteractionResult.SUCCESS;
			}

			return InteractionResult.PASS; // pass regardless. We don't want to perform breeding, age ups, etc. on untamed.
		}

		// heal
		if (getHealthRelative() < 1 && isFoodItem(stack)) {
			//noinspection ConstantConditions
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
			this.inventory.setItem(CHEST_SLOT, stack.copyWithCount(1));
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
	public boolean hurt(DamageSource src, float par2) {
		if (isInvulnerableTo(src)) return false;
		stopSitting();
		boolean flag = super.hurt(src, par2);

		if (flag && src.getEntity() instanceof LivingEntity) {
			DragonAI.wasHurtBy(this, (LivingEntity) src.getEntity());
		}
		return flag;
	}

	@Override
	public boolean isInvulnerableTo(DamageSource src) {
		Entity srcEnt = src.getEntity();
		if (srcEnt != null && (srcEnt == this || hasPassenger(srcEnt))) return true;

		if (
			src == level.damageSources().dragonBreath() || // inherited from it anyway
			src == level.damageSources().cactus() || // assume cactus needles don't hurt thick scaled lizards
			src == level.damageSources().inWall()
		) {
			return true;
		}

		return getBreed().getImmunities().contains(src.getMsgId()) || super.isInvulnerableTo(src);
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();

		//Dont drop equipment if the dragon is selected and can be summoned again
		if (getOwner() instanceof Player player) {
			DragonOwnerCapability capability = PlayerStateUtils.getHandler(player);

			if (capability.isBoundToWhistle(this)) {
				return;
			}
		}

		//		if (isSaddled()) spawnAtLocation(Items.SADDLE);

		if (this.inventory != null) {
			for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
				ItemStack itemstack = this.inventory.getItem(i);
				if (!itemstack.isEmpty() && !EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
					this.spawnAtLocation(itemstack);
				}
			}
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENDER_DRAGON_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.DRAGON_DEATH_SOUND.get();
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return getBreed().getAmbientSound() != null ? getBreed().getAmbientSound() : ModSounds.DRAGON_AMBIENT_SOUND.get();
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return getBreed().getDeathLootTable() != null
			? ResourceKey.create(Registries.LOOT_TABLE, getBreed().getDeathLootTable())
			: super.getDefaultLootTable();
	}

	@Override
	protected void onChangedBlock(ServerLevel level, BlockPos pos) {
		super.onChangedBlock(level, pos);
		getBreed().onMove(this);
		getBrain().eraseMemory(ModMemoryModuleTypes.IDLE_TICKS.get());
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, damageSource, recentlyHit);
		if (isSaddled()) spawnAtLocation(Items.SADDLE);
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public boolean doHurtTarget(Entity entityIn) {
		DamageSource damageSource = level().damageSources().mobAttack(this);
		boolean attacked = entityIn.hurt(damageSource, (float) getAttribute(ATTACK_DAMAGE).getValue());
		if (attacked) {
			if (this.level() instanceof ServerLevel serverlevel1) {
				EnchantmentHelper.doPostAttackEffects(serverlevel1, entityIn, damageSource);
			}
		}

		if (attacked) {
			triggerAnim("head-controller", "bite");
		}

		return attacked;
	}

	@Override
	protected void playStepSound(BlockPos entityPos, BlockState state) {
		if (isInWater()) return;

		if (isHatchling()) {
			super.playStepSound(entityPos, state);
			return;
		}

		// override sound type if the top block is snowy
		var soundType = state.getSoundType();
		if (level.getBlockState(entityPos.above()).getBlock() == Blocks.SNOW) {
			soundType = Blocks.SNOW.getSoundType(state, level, entityPos, this);
		}

		// play stomping for bigger dragons
		playSound(getStepSound(), soundType.getVolume() * 0.15f, soundType.getPitch() * getVoicePitch());
	}

	public SoundEvent getStepSound() {
		return ModSounds.DRAGON_STEP_SOUND.get();
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
		var offspring = ModEntities.DRAGON_ENTITY.get().create(level);
		offspring.setBreed(getBreed());
		return offspring;
	}

	@Override
	public void setAge(int pAge) {
		super.setAge(pAge);
		if (!level.isClientSide) {
			PacketDistributor.sendToPlayersTrackingEntity(this, new DragonAgeSyncPacket(getId(), pAge));
		}
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		return !isHatchling() && !hasControllingPassenger() && super.canAttack(target);
	}

	@Override
	public SoundEvent getEatingSound(ItemStack itemStackIn) {
		return SoundEvents.GENERIC_EAT;
	}

	@Override
	public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
		return !(target instanceof TamableAnimal tameable) || !Objects.equals(tameable.getOwner(), owner);
	}

	@Override
	public void setTarget(@org.jetbrains.annotations.Nullable LivingEntity target) {
		super.setTarget(target);
		getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
		if (target != null) {
			getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
		}
	}

	public SoundEvent getAttackSound() {
		return SoundEvents.GENERIC_EAT;
	}

	@Override
	public void containerChanged(Container pContainer) {
		// Update the saved dragon so that summoning the dragon doesnt wipe the inventory
		if (!isBeingSummoned) updateOwnerData();
		setArmor();
	}

	public void updateOwnerData() {
		if (getOwner() instanceof Player player && !player.level.isClientSide) {
			var handler = player.getData(ModCapabilities.PLAYER_CAPABILITY);

			if (handler.isBoundToWhistle(this)) {
				handler.setPlayerInstance(player);
				handler.setDragonToWhistle(this, DragonWhistleHandler.getDragonSummonIndex(player, getDragonUUID()));
			} else {
				DragonWorldDataManager.addDragonHistory(this);
			}
		}
	}

	@Override
	public @org.jetbrains.annotations.Nullable LivingEntity getOwner() {
		UUID uuid = this.getOwnerUUID();

		if (uuid != null) {
			for (Player player : level.players()) {
				if (player.getUUID().equals(uuid)) {
					return player;
				}
			}
		}

		return null;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		var list = getBreed().getBreedingItems();
		return !stack.isEmpty() && (list != null && list.size() > 0 ? list.contains(stack.getItem()) : stack.is(ItemTags.FISHES));
	}

	public boolean isFoodItem(ItemStack stack) {
		var food = stack.getItem().getFoodProperties(stack, this);
		return food != null && stack.is(ItemTags.MEAT);
	}

	public void tamedFor(Player player, boolean successful) {
		if (successful) {
			setTame(true, true);
			navigation.stop();
			setTarget(null);
			setOwnerUUID(player.getUUID());
			level.broadcastEntityEvent(this, (byte) 7);
			updateOwnerData();
		} else {
			level.broadcastEntityEvent(this, (byte) 6);
		}
	}

	public boolean isTamedFor(Player player) {
		return isTame() && (isOwnedBy(player) || Objects.equals(getOwnerUUID(), player.getUUID()));
	}

	public void setRidingPlayer(Player player) {
		player.setYRot(getYRot());
		player.setXRot(getXRot());
		player.startRiding(this);
	}

	@Override
	public void setOrderedToSit(boolean pOrderedToSit) {
		super.setOrderedToSit(pOrderedToSit);
		navigation.stop();
		setTarget(null);
		setInSittingPose(pOrderedToSit);
	}

	@Override
	public void swing(InteractionHand hand) {
		playSound(getAttackSound(), 1, 0.7f);
		super.swing(hand);
	}

	@Override
	public boolean canMate(Animal mate) {
		if (mate == this) {
			return false;
		} else if (!(mate instanceof DMRDragonEntity)) {
			return false;
		} else if (!canReproduce()) return false;

		DMRDragonEntity dragonMate = (DMRDragonEntity) mate;

		if (!dragonMate.isTame()) {
			return false;
		} else if (!dragonMate.canReproduce()) {
			return false;
		} else {
			return isInLove() && dragonMate.isInLove();
		}
	}

	@Override
	public void spawnChildFromBreeding(ServerLevel level, Animal animal) {
		if (!(animal instanceof DMRDragonEntity mate)) return;

		// pick a breed to inherit from, and place hatching.
		var state = ModBlocks.DRAGON_EGG_BLOCK.get().defaultBlockState().setValue(DMREggBlock.HATCHING, true);
		var eggOutcomes = DragonBreedsRegistry.getEggOutcomes(this, level, mate);

		// Pick a random breed from the list to use as the offspring
		var offSpringBreed = eggOutcomes.get(getRandom().nextInt(eggOutcomes.size()));
		var variant = !offSpringBreed.getVariants().isEmpty()
			? offSpringBreed.getVariants().get(getRandom().nextInt(offSpringBreed.getVariants().size()))
			: null;
		var egg = DMREggBlock.place(level, blockPosition(), state, offSpringBreed, variant);

		// mix the custom names in case both parents have one
		if (hasCustomName() && animal.hasCustomName()) {
			String babyName = BreedingUtils.generateCustomName(this, animal);
			egg.setCustomName(Component.literal(babyName));
		}

		// increase reproduction counter
		addReproCount();
		mate.addReproCount();
		updateOwnerData();
	}

	@Override
	public void openCustomInventoryScreen(Player pPlayer) {
		pPlayer.openMenu(new SimpleMenuProvider((pId, pInventory, pPlayer1) -> createMenu(pId, pInventory), getDisplayName()), buf ->
			buf.writeInt(getId())
		);
	}

	private DragonContainerMenu createMenu(int pId, Inventory pInventory) {
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
		buffer.writeInt(getId());
		return new DragonContainerMenu(pId, pInventory, buffer);
	}
}
