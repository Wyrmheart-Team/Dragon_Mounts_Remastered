package dmr.DragonMounts.server.entity;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.network.packets.DragonAgeSyncPacket;
import dmr.DragonMounts.registry.*;
import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.server.ai.*;
import dmr.DragonMounts.server.container.DragonContainerMenu;
import dmr.DragonMounts.common.config.DMRConfig;
import dmr.DragonMounts.server.blocks.DragonMountsEggBlock;
import dmr.DragonMounts.server.items.DragonArmorItem;
import dmr.DragonMounts.server.items.DragonSpawnEgg;
import dmr.DragonMounts.types.armor.DragonArmor;
import dmr.DragonMounts.types.dragonBreeds.DragonHybridBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import dmr.DragonMounts.util.BreedingUtils;
import dmr.DragonMounts.util.PlayerStateUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
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
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Vector3d;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;
import java.util.List;

import static net.minecraft.world.entity.ai.attributes.Attributes.*;
import static net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED;
import static net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED;

public class DMRDragonEntity extends AbstractDMRDragonEntity
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	
	// base attributes
	public static final double BASE_SPEED_GROUND = 0.25;
	public static final double BASE_SPEED_WATER = 2;

	public static final double BASE_SPEED_FLYING = 0.2;

	public static final double BASE_DAMAGE = 8;
	public static final double BASE_HEALTH = 60;
	public static final double BASE_FOLLOW_RANGE = 32;
	public static final int BASE_KB_RESISTANCE = 1;
	public static final float BASE_WIDTH = 2.75f; // adult sizes
	public static final float BASE_HEIGHT = 2.75f;
	
	public static final int BREATH_COUNT = 5;
	
	// other constants
	public static final ResourceLocation SCALE_MODIFIER = ResourceLocation.fromNamespaceAndPath(DragonMountsRemaster.MOD_ID, "scale_attribute"); // just a random uuid I took online
	public static final int GROUND_CLEARENCE_THRESHOLD = 2;
	
	public DMRDragonEntity(EntityType<? extends TamableAnimal> pEntityType, Level pLevel)
	{
		super(pEntityType, pLevel);
		moveControl = new DragonMoveController(this);
		setDragonUUID(UUID.randomUUID());
	}
	
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
	
	private AnimationController<?> animationController;
	private AnimationController<?> headController;
	
	private void addDragonAnimations(ControllerRegistrar data)
	{
		headController = new AnimationController<>(this, "head-controller", 0, state -> {
			return state.setAndContinue(isFlying() ? NECK_TURN_FLIGHT : NECK_TURN);
		});
		
		headController.triggerableAnim("bite", BITE);
		headController.triggerableAnim("breath", BREATH);
		data.add(headController);
		
		animationController = new AnimationController<>(this, "controller", 5, state -> {
			Vec3 motio = new Vec3(getX() - xo, getY() - yo, getZ() - zo);
			boolean isMovingHorizontal = Math.sqrt(Math.pow(motio.x, 2) + Math.pow(motio.z, 2)) > 0.1;
			state.setControllerSpeed(1);
			
			if(isFlying()) {
				if (isMovingHorizontal || isPathFinding()) {
					if(isSprinting()){
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
					}else {
						return state.setAndContinue(FLY);
					}
				} else {
					return state.setAndContinue(HOVER);
				}
			}else if(swinging && getTarget() != null) {
				return state.setAndContinue(BITE);
				
			}else if(isInSittingPose()) {
				var lookAtContext = TargetingConditions.forNonCombat().range(10).selector((p_25531_) -> EntitySelector.notRiding(this).test(p_25531_));
				var lookAt = level.getNearestPlayer(lookAtContext, this, getX(),getEyeY(), getZ());
				
				if(lookAt != null){
					return state.setAndContinue(SIT_ALT);
				}else {
					return state.setAndContinue(SIT);
				}
				
			}else if(isSwimming()) {
				return state.setAndContinue(SWIM);
				
			}else if(isInWater()) {
				if(isMovingHorizontal){
					return state.setAndContinue(SWIM);
				}else{
					return state.setAndContinue(SWIM_IDLE);
				}
				
			}else if(isMovingHorizontal) {
				if(isSprinting()){
					return state.setAndContinue(SPRINT);
				}else{
					state.setControllerSpeed(1f + (isShiftKeyDown() ? 2f : getSpeed()));
					return state.setAndContinue(isShiftKeyDown() ? SNEAK_WALK : WALK);
				}
			}
			
			return state.setAndContinue(isShiftKeyDown() ? SNEAK_IDLE : IDLE);
		});
		
		animationController.setSoundKeyframeHandler(sound -> {
			if(sound.getKeyframeData().getSound().equalsIgnoreCase("flap")){
				onFlap();
			}
		});
		
		data.add(animationController);
	}
	
	public static AttributeSupplier.Builder createAttributes()
	{
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
	protected void registerGoals() // TODO: Much Smarter AI and features
	{
		goalSelector.addGoal(1, new FloatGoal(this));
		goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
		goalSelector.addGoal(3, new MeleeAttackGoal(this, 1, true));
		
		goalSelector.addGoal(4, new DragonFollowOwnerGoal(this, 1.5f, 20f, 5f));
		goalSelector.addGoal(4, new DragonPathingGoal(this, 2f));
		
		goalSelector.addGoal(4, new DragonWanderGoal(this, 1));
		goalSelector.addGoal(4, new DragonBreedGoal(this));
		//goalSelector.addGoal(5, new DragonLandGoal(this));
		goalSelector.addGoal(7, new LookAtPlayerGoal(this, LivingEntity.class, 16f));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));

		targetSelector.addGoal(0, new OwnerHurtByTargetGoal(this));
		targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
		targetSelector.addGoal(2, new HurtByTargetGoal(this));
		targetSelector.addGoal(3, new NonTameRandomTargetGoal<>(this, Animal.class, false, e -> !(e instanceof DMRDragonEntity)));
	}

	@Override
	public boolean isSaddleable()
	{
		return isAlive() && !isHatchling() && isTame();
	}

	@Override
	public void equipSaddle(ItemStack stack, SoundSource source)
	{
		setSaddled(true);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_SADDLE, getSoundSource(), 1, 1);
		inventory.setItem(SADDLE_SLOT, stack);
	}
	
	public void equipArmor(Player pPlayer, ItemStack pArmor) {
		if (this.isArmor(pArmor)) {
			this.inventory.setItem(ARMOR_SLOT, pArmor.copyWithCount(1));
			if (!pPlayer.getAbilities().instabuild) {
				pArmor.shrink(1);
			}
			setArmor();
		}
	}
	
	private static final ResourceLocation ARMOR_MODIFIER = ResourceLocation.fromNamespaceAndPath(DragonMountsRemaster.MOD_ID, "armor_attribute");
	
	public void setArmor(){
		ItemStack itemstack = this.inventory.getItem(ARMOR_SLOT);
		if (!this.level().isClientSide) {
			this.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER);
			if(this.isArmor(itemstack)) {
				DragonArmor armor = DragonArmor.getArmorType(itemstack);
				if(armor != null) {
					int i = armor.getProtection();
					if (i != 0) {
						this.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(ARMOR_MODIFIER, i, Operation.ADD_VALUE));
					}
				}
			}
		}
	}
	
	public boolean isArmor(ItemStack pStack) {
		return pStack.getItem() instanceof DragonArmorItem;
	}

	public boolean canFly()
	{
		// hatchling's can't fly
		return !isHatchling() && getEyeInFluidType().isAir();
	}

	public boolean shouldFly()
	{
		if(!canFly()) return false;
		if (isFlying()) return !onGround(); // more natural landings
		return canFly() && !isInWater() && !isNearGround();
	}
	

	public void liftOff()
	{
		if (canFly()) jumpFromGround();
	}
	
	@Override
	public ItemStack getPickedResult(HitResult target)
	{
		return DragonSpawnEgg.create(getBreed());
	}
	
	@Override
	public void registerControllers(ControllerRegistrar controllers)
	{
		addDragonAnimations(controllers);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}

	@Override
	public EntityDimensions getDimensions(Pose poseIn)
	{
		var height = isInSittingPose() ? 2.15f : isShiftKeyDown() ? 2.5f : BASE_HEIGHT;
		var scale = getScale();
		var dimWidth = BASE_WIDTH * scale;
		var dimHeight = height * scale;
		var dim = EntityDimensions.scalable(dimWidth, dimHeight).withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0F, dimHeight - 0.15625F, getScale()));
		return dim;
	}
	
	@Override
	public void positionRider(Entity passenger, Entity.MoveFunction pCallback)
	{
		LivingEntity riddenByEntity = getControllingPassenger();
		if (riddenByEntity != null)
		{
			boolean customRidingPos = false;

			if(!customRidingPos){
				Vec3 vec3 = this.getPassengerRidingPosition(passenger);
				Vec3 vec31 = passenger.getVehicleAttachmentPoint(this);
				Vec3 riderPos = new Vec3(vec3.x - vec31.x, vec3.y - vec31.y, vec3.z - vec31.z); //.yRot((float) Math.toRadians(-yBodyRot)).add(0, getBbHeight() + breed.getVerticalRidingOffset(), getScale())
				pCallback.accept(passenger, riderPos.x, riderPos.y, riderPos.z);
			}
			
			// fix rider rotation
			if (getFirstPassenger() instanceof LivingEntity)
			{
				riddenByEntity.xRotO = riddenByEntity.getXRot();
				riddenByEntity.yRotO = riddenByEntity.getYRot();
				riddenByEntity.yBodyRot = yBodyRot;
			}
		}
	}
	

	@Override
	public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource)
	{
		return !canFly() && super.causeFallDamage(pFallDistance, pMultiplier, pSource);
	}
	
	@Override
	public void baseTick()
	{
		super.baseTick();
		
		if (!this.level.isClientSide && this.isAlive() && this.tickCount % 20 == 0) {
			this.heal(1.0F);
		}
		
		if (isControlledByLocalInstance() && getControllingPassenger() != null){
			setSprinting(getControllingPassenger().isSprinting());
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		if (getPose() == Pose.STANDING) {
			if(isShiftKeyDown()) setPose(Pose.CROUCHING);
			
		}else if(getPose() == Pose.CROUCHING){
			if(!isShiftKeyDown()) setPose(Pose.STANDING);
		}
		
		if(this.breed == null && getBreed() != null){
			setBreed(getBreed());
		}
		
		// update nearGround state when moving for flight and animation logic
		var dimensions = getDimensions(getPose());
		nearGround = onGround() || !level.noCollision(this, new AABB(getX() - dimensions.width() / 2, getY(), getZ() - dimensions.width() / 2, getX() + dimensions.width() / 2, getY() - (GROUND_CLEARENCE_THRESHOLD * getScale()), getZ() + dimensions.width() / 2));
		
		// update flying state based on the distance to the ground
		boolean flying = shouldFly();
		if (flying != isFlying())
		{
			setFlying(flying);
		}

		if(tickCount % 20 == 0)
			getBreed().tick(this);
		
		if(level.isClientSide){
			if(headController != null && headController.isPlayingTriggeredAnimation()){
				if(headController.getCurrentAnimation() != null && Objects.equals(headController.getCurrentAnimation().animation().name(), "breath")){
					clientBreathAttack();
				}
			}
		}else{
			if(breathTime != -1) {
				serverBreathAttack();
			}
		}
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
	public void aiStep()
	{
		super.aiStep();
		
		if(isNoGravity() != shouldFly()){
			setNoGravity(shouldFly());
		}
		
		if(getControllingPassenger() == null && !hasWanderTarget() && !isOrderedToSit()) {
			if (isPathFinding()) {
				var dest = getNavigation().getTargetPos();
				var farDist = dest.distManhattan(blockPosition()) >= 16d;
				var dist = (dest.getY() - blockPosition().getY()) >= 8d || dest.distManhattan(blockPosition()) >= 64d;
				var shouldFly = !isFlying() && canFly();
				
				setSprinting(farDist);
				
				if (shouldFly && dist) {
					liftOff();
				}
			} else {
				if (isSprinting()) setSprinting(false);
			}
		}
	}
	
	@Override
	public void setAge(int pAge)
	{
		super.setAge(pAge);
		if(!level.isClientSide){
			PacketDistributor.sendToPlayersTrackingEntity(this, new DragonAgeSyncPacket(getId(), pAge));
		}
	}
	
	@Override
	public void refreshDimensions()
	{
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
	protected Vec3 getRiddenInput(Player driver, Vec3 move) {
		double moveSideways = move.x;
		double moveY = 0;
		double moveForward = Math.min(Math.abs(driver.zza) + Math.abs(driver.xxa), 1);
		
		if (isFlying()) {
			moveForward = moveForward > 0 ? moveForward : 0;
			if (DMRConfig.CLIENT.isLoaded() && moveForward > 0 && DMRConfig.CAMERA_FLIGHT.get()) moveY = -(driver.getXRot() * (Math.PI / 180) * 0.5f);
			
			if (driver.jumping) moveY += 0.5;
			if(driver.isShiftKeyDown()) moveY += -0.5;
		}else if(isInFluidType()){
			moveForward = moveForward > 0 ? moveForward : 0;

			if (moveForward > 0 && DMRConfig.CAMERA_FLIGHT.get()){
				moveY = (-driver.getXRot() * (Math.PI / 180)) * 2;
			}
			
			if (driver.jumping) moveY += 2;
			if(driver.isShiftKeyDown()) moveY += -2;
		}
		
		float f = isShiftKeyDown() ? 0.3F : 1f;
		Vec3 movement = new Vec3(moveSideways * f, moveY, moveForward * f);
		
		return maybeBackOffFromEdge(movement, MoverType.SELF);
	}

	@Override
	protected void tickRidden(Player driver, Vec3 move) {
		super.tickRidden(driver, move);
		// rotate head to match driver.
		float yaw = driver.yHeadRot;
		if (move.z > 0) // rotate in the direction of the drivers controls1
			yaw += (float) Mth.atan2(driver.zza, driver.xxa) * (180f / (float) Math.PI) - 90;
		yHeadRot = driver.yHeadRot;
		setXRot(driver.getXRot() * 0.68f);

		// rotate body towards the head
		setYRot(Mth.rotateIfNecessary(yaw, getYRot(), 8));

 		if (isControlledByLocalInstance())
		{
			if(driver.jumping){
				if (!isFlying() && canFly()) {
					liftOff();
				} else if (onGround() && !canFly()){
					jumpFromGround();
				}
			}
			
		}
	}
	
	@Override
	public float getSpeed()
	{
		return (isSprinting() ? 1.25f : 1) * (float)getAttributeValue(MOVEMENT_SPEED);
	}
	
	@Override
	protected float getFlyingSpeed()
	{
		return (isSprinting() ? 1.25f : 1) * (float)getAttributeValue(FLYING_SPEED);
	}
	
	@Override
	public boolean isInvulnerableTo(DamageSource src) {
		Entity srcEnt = src.getEntity();
		if (srcEnt != null && (srcEnt == this || hasPassenger(srcEnt))) return true;

		if (src == level.damageSources().dragonBreath() // inherited from it anyway
		    || src == level.damageSources().cactus() // assume cactus needles don't hurt thick scaled lizards
			|| src == level.damageSources().inWall())
			return true;

		return getBreed().getImmunities().contains(src.getMsgId()) || super.isInvulnerableTo(src);
	}

	public boolean isTamingItem(ItemStack stack)
	{
		var list = breed.getTamingItems();
		return !stack.isEmpty() && (list != null && list.size() > 0 ? list.contains(stack.getItem()) : stack.is(ItemTags.FISHES));
	}
	
	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand)
	{
		var stack = player.getItemInHand(hand);

		var stackResult = stack.interactLivingEntity(player, this, hand);
		if (stackResult.consumesAction()) return stackResult;

		// tame
		if (!isTame())
		{
			if (isServer() && isTamingItem(stack))
			{
				stack.shrink(1);
				tamedFor(player, getRandom().nextInt(5) == 0);
				return InteractionResult.SUCCESS;
			}

			return InteractionResult.PASS; // pass regardless. We don't want to perform breeding, age ups, etc. on untamed.
		}

		// heal
		if (getHealthRelative() < 1 && isFoodItem(stack))
		{
			//noinspection ConstantConditions
			heal(stack.getItem().getFoodProperties(stack, this).nutrition());
			playSound(getEatingSound(stack), 0.7f, 1);
			stack.shrink(1);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}

		// saddle up!
		if (isTamedFor(player) && isSaddleable() && !isSaddled() && stack.getItem() instanceof SaddleItem)
		{
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
			equipSaddle(stack, getSoundSource());
			updateContainerEquipment();
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		
		// equip armor
		if (isTamedFor(player) && isArmor(stack))
		{
			equipArmor(player, stack);
			updateContainerEquipment();
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		
		if (isTamedFor(player) && !hasChest() && stack.is(Items.CHEST))
		{
			this.inventory.setItem(CHEST_SLOT, stack.copyWithCount(1));
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
			updateContainerEquipment();
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		
		// open menu
		if (isTamedFor(player) && player.isSecondaryUseActive())
		{
			if(!level.isClientSide)
				this.openCustomInventoryScreen(player);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}

		// ride on
		if (isTamedFor(player) && isSaddled() && !isHatchling() && !isFood(stack))
		{
			if (isServer())
			{
				setRidingPlayer(player);
				navigation.stop();
			}
			setTarget(null);
			setOrderedToSit(false);
			setInSittingPose(false);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}

		return super.mobInteract(player, hand);
	}
	
	public void updateAgeProperties()
	{
		refreshDimensions();
		
		AttributeInstance stepHeightInstance = getAttribute(STEP_HEIGHT);
		stepHeightInstance.setBaseValue(Math.max(2 * getAgeProgress(), 1));
		
		AttributeInstance baseHealthInstance = getAttribute(MAX_HEALTH);
		baseHealthInstance.setBaseValue(DMRConfig.BASE_HEALTH.get());
		
		AttributeInstance attackDamageInstance = getAttribute(ATTACK_DAMAGE);
		attackDamageInstance.setBaseValue(DMRConfig.BASE_DAMAGE.get());
		
		
		var mod = new AttributeModifier(SCALE_MODIFIER, getScale(), Operation.ADD_VALUE);
		for (var attribute : new Holder[]{MAX_HEALTH, ATTACK_DAMAGE}) // avoid duped code
		{
			AttributeInstance instance = getAttribute(attribute);
			instance.removeModifier(SCALE_MODIFIER);
			instance.addTransientModifier(mod);
		}
	}
	
	public boolean isPushedByFluid() {
		if (getBreed() != null && getBreed().getImmunities().contains("drown")) return true;
		
		return super.isPushedByFluid();
	}
	
	@Override
	public boolean canDrownInFluidType(FluidType type)
	{
		if(type == Fluids.WATER.getFluidType()){
			if (getBreed() != null && getBreed().getImmunities().contains("drown")) return false;
		}
		return super.canDrownInFluidType(type);
	}
	
	@Override
	public boolean fireImmune()
	{
		return super.fireImmune() || getBreed() != null && getBreed().getImmunities().contains("onFire");
	}
	
	@Override
	public boolean canSwimInFluidType(FluidType type)
	{
		if(type == Fluids.WATER.getFluidType()){
			if (getBreed() != null && getBreed().getImmunities().contains("drown")) return true;
		}
		return super.canSwimInFluidType(type);
	}
	
	@Override
	protected void onChangedBlock(ServerLevel level, BlockPos pos)
	{
		super.onChangedBlock(level, pos);
		getBreed().onMove(this);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn)
	{
		return SoundEvents.ENDER_DRAGON_HURT;
	}
	
	@Override
	protected void playStepSound(BlockPos entityPos, BlockState state)
	{
		if (isInWater()) return;

		if (isHatchling())
		{
			super.playStepSound(entityPos, state);
			return;
		}
		
		// override sound type if the top block is snowy
		var soundType = state.getSoundType();
		if (level.getBlockState(entityPos.above()).getBlock() == Blocks.SNOW)
			soundType = Blocks.SNOW.getSoundType(state, level, entityPos, this);

		// play stomping for bigger dragons
		playSound(getStepSound(), soundType.getVolume() * 0.15f, soundType.getPitch() * getVoicePitch());
	}

	public SoundEvent getStepSound()
	{
		return DMRSounds.DRAGON_STEP_SOUND.get();
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound()
	{
		return DMRSounds.DRAGON_DEATH_SOUND.get();
	}
	
	@Override
	public Vec3 getLightProbePosition(float p_20309_)
	{
		return new Vec3(getX(), getY() + getBbHeight(), getZ());
	}
	
	@Override
	protected SoundEvent getAmbientSound()
	{
		return getBreed().getAmbientSound() != null ? getBreed().getAmbientSound() : DMRSounds.DRAGON_AMBIENT_SOUND.get();
	}
	
	@Override
	public SoundEvent getEatingSound(ItemStack itemStackIn)
	{
		return SoundEvents.GENERIC_EAT;
	}

	public SoundEvent getAttackSound()
	{
		return SoundEvents.GENERIC_EAT;
	}
	
	public SoundEvent getWingsSound()
	{
		return SoundEvents.ENDER_DRAGON_FLAP;
	}
	
	public void onFlap() {
		if (this.level().isClientSide && !this.isSilent()) {
			this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), getWingsSound(), this.getSoundSource(), 2.0F, 0.8F + this.random.nextFloat() * 0.3F, false);
		}
	}
	
	public Vector3d breathSourcePosition;
	
	private static final double breathLength = 0.5 * BREATH_COUNT;
	private long breathTime = -1;
	
	public void serverBreathAttack(){
		if(breathTime == -1){
			breathTime = 0;
		}else{
			if(breathTime >= (int)(breathLength * 20)){
				breathTime = -1;
			}else{
				breathTime++;
				
				if(getControllingPassenger() == null) return;
				var viewVector = getControllingPassenger().getViewVector(1f);
				
				float degrees = Mth.wrapDegrees(getControllingPassenger().yBodyRot);
				
				double yawRadians = Math.toRadians(degrees);
				double f4 = -Math.sin(yawRadians);
				double f5 = Math.cos(yawRadians);
				Vec3 lookVector = new Vec3(f4, viewVector.y, f5);
				
				var dimensions = getDimensions(getPose());
				float size = 15f;
				
				var offsetBoundingBox = new AABB(getX() + (dimensions.width() / 2), getY() + (dimensions.height() / 2), getZ() + (dimensions.width() / 2),
				                                 getX() + (dimensions.width() / 2) + lookVector.x * size, getY() + (dimensions.height() / 2) + lookVector.y * size, getZ() +  (dimensions.width() / 2) + lookVector.z * size);
				var entities = level.getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat().ignoreInvisibilityTesting()
						.selector(getControllingPassenger()::canAttack).selector(s -> !s.isAlliedTo(getControllingPassenger())), getControllingPassenger(), offsetBoundingBox);
				
				entities.stream().filter(e -> e != this && e != getControllingPassenger()).forEach(ent -> {
					ent.hurt(level.damageSources().mobAttack(this), 2);
					ent.setRemainingFireTicks(5);
				});
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public void clientBreathAttack(){
		if(getControllingPassenger() == null) return;
		
		float degrees = Mth.wrapDegrees(getControllingPassenger().yBodyRot);
		
		double yawRadians = Math.toRadians(degrees);
		double f4 = -Math.sin(yawRadians);
		double f5 = Math.cos(yawRadians);
		Vec3 lookVector = new Vec3(f4, 0, f5);
		
		var viewVector = getControllingPassenger().getViewVector(1f);
		
		if(breathSourcePosition != null){
			for(int i = 0; i < 20; i++) {
				Vec3 speed = new Vec3(
						lookVector.x * (0.5f + (random.nextFloat() / 2)),
						viewVector.y,
						lookVector.z * (0.5f + (random.nextFloat() / 2)));
				
				var particle = ParticleTypes.FLAME;
				level.addParticle(particle, getX() + breathSourcePosition.x, getY() + breathSourcePosition.y, getZ() + breathSourcePosition.z, speed.x, speed.y, speed.z);
			}
		}
	}

	@Override
	public boolean isFood(ItemStack stack)
	{
		var list = breed.getBreedingItems();
		return !stack.isEmpty() && (list != null && list.size() > 0 ? list.contains(stack.getItem()) : stack.is(ItemTags.FISHES));
	}

	public boolean isFoodItem(ItemStack stack)
	{
		var food = stack.getItem().getFoodProperties(stack, this);
		return food != null && stack.is(ItemTags.MEAT);
	}

	public void tamedFor(Player player, boolean successful)
	{
		if (successful)
		{
			setTame(true, true);
			navigation.stop();
			setTarget(null);
			setOwnerUUID(player.getUUID());
			level.broadcastEntityEvent(this, (byte) 7);
		}
		else
		{
			level.broadcastEntityEvent(this, (byte) 6);
		}
	}
	
	public boolean isTamedFor(Player player)
	{
		return isTame() && isOwnedBy(player);
	}
	
	@Override
	public double getEyeY()
	{
		return super.getEyeY() * 1.2f;
	}
	
	//TODO
//	@Override
//	public float getMyRidingOffset(Entity pEntity)
//	{
//		return getBbHeight() - 0.175f + breed.getVerticalRidingOffset();
//	}

	public void setRidingPlayer(Player player)
	{
		player.setYRot(getYRot());
		player.setXRot(getXRot());
		player.startRiding(this);
	}
	
	@Override
	public void setOrderedToSit(boolean pOrderedToSit)
	{
		super.setOrderedToSit(pOrderedToSit);
		setWanderTarget(null);
		navigation.stop();
		setTarget(null);
	}
	
	@Override
	public float getScale()
	{
		var scale = getBreed() != null ? getBreed().getSizeModifier() : 1;
		return scale * (isBaby() ? 0.5f : 1f);
	}
	
	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit)
	{
		super.dropCustomDeathLoot(level, damageSource, recentlyHit);
		if (isSaddled()) spawnAtLocation(Items.SADDLE);
	}
	
	@Override
	protected ResourceKey<LootTable> getDefaultLootTable()
	{
		return getBreed().getDeathLootTable() != null ? ResourceKey.create(Registries.LOOT_TABLE, getBreed().getDeathLootTable()) : super.getDefaultLootTable();
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public boolean doHurtTarget(Entity entityIn)
	{
		DamageSource damageSource = level().damageSources().mobAttack(this);
		boolean attacked = entityIn.hurt(damageSource, (float) getAttribute(ATTACK_DAMAGE).getValue());
		if (attacked){
			if (this.level() instanceof ServerLevel serverlevel1) {
				EnchantmentHelper.doPostAttackEffects(serverlevel1, entityIn, damageSource);
			}
		}
		
		if(attacked){
			triggerAnim("head-controller", "bite");
		}
		
		return attacked;
	}

	@Override
	public void swing(InteractionHand hand)
	{
		playSound(getAttackSound(), 1, 0.7f);
		super.swing(hand);
	}

	@Override
	public boolean hurt(DamageSource src, float par2)
	{
		if (isInvulnerableTo(src)) return false;
		setOrderedToSit(false);
		return super.hurt(src, par2);
	}

	@Override
	public boolean canMate(Animal mate)
	{
		if (mate == this) return false;
		else if (!(mate instanceof DMRDragonEntity)) return false;
		else if (!canReproduce()) return false;

		DMRDragonEntity dragonMate = (DMRDragonEntity) mate;
		
		if (!dragonMate.isTame()) return false;
		else if (!dragonMate.canReproduce()) return false;
		else return isInLove() && dragonMate.isInLove();
	}

	private List<IDragonBreed> getBreeds(){
		List<IDragonBreed> breeds = new ArrayList<>();
		if(getBreed() instanceof DragonHybridBreed hybridBreed){
			breeds.add(hybridBreed.parent1);
			breeds.add(hybridBreed.parent2);
			return breeds;
		}
		breeds.add(getBreed());
		return breeds;
	}
	
	@Override
	public void spawnChildFromBreeding(ServerLevel level, Animal animal)
	{
		if (!(animal instanceof DMRDragonEntity mate)) return;
		
		// pick a breed to inherit from, and place hatching.
		var state = DMRBlocks.DRAGON_EGG_BLOCK.get().defaultBlockState().setValue(DragonMountsEggBlock.HATCHING, true);
		var eggOutcomes = getEggOutcomes(level, mate);
		
		// Pick a random breed from the list to use as the offspring
		var offSpringBreed = eggOutcomes.get(getRandom().nextInt(eggOutcomes.size()));
		var egg = DragonMountsEggBlock.place(level, blockPosition(), state, offSpringBreed);

		// mix the custom names in case both parents have one
		if (hasCustomName() && animal.hasCustomName())
		{
			String babyName = BreedingUtils.generateCustomName(this, animal);
			egg.setCustomName(Component.literal(babyName));
		}

		// increase reproduction counter
		addReproCount();
		mate.addReproCount();
	}
	
	private ArrayList<IDragonBreed> getEggOutcomes(ServerLevel level, DMRDragonEntity mate)
	{
		var eggOutcomes = new ArrayList<IDragonBreed>();
		
		eggOutcomes.addAll(getBreeds());
		eggOutcomes.addAll(mate.getBreeds());
		
		if(DMRConfig.HABITAT_OFFSPRING.get()) {
			IDragonBreed highestBreed1 = BreedingUtils.getHabitatBreedOutcome(level, blockPosition());
			IDragonBreed highestBreed2 = BreedingUtils.getHabitatBreedOutcome(level, mate.blockPosition());
			
			if (highestBreed1 != null) {
				if (!eggOutcomes.contains(highestBreed1)) eggOutcomes.add(highestBreed1);
			}
			
			if (highestBreed2 != null) {
				if (!eggOutcomes.contains(highestBreed2)) eggOutcomes.add(highestBreed2);
			}
		}
		
		if(DMRConfig.ALLOW_HYBRIDIZATION.get()) {
			var newList = new ArrayList<IDragonBreed>();
			
			for (IDragonBreed breed1 : eggOutcomes) {
				for (IDragonBreed breed2 : eggOutcomes) {
					if (breed1 != breed2) {
						var hybrid = DragonBreedsRegistry.getHybridBreed(breed1, breed2);
						if (hybrid != null) {
							newList.add(hybrid);
						}
					}
				}
			}
			eggOutcomes.addAll(newList);
		}
		
		return eggOutcomes;
	}
	
	@Override
	@SuppressWarnings("ConstantConditions")
	public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob)
	{
		var offspring = DMREntities.DRAGON_ENTITY.get().create(level);
		offspring.setBreed(getBreed());
		return offspring;
	}

	@Override
	public boolean wantsToAttack(LivingEntity target, LivingEntity owner)
	{
		return !(target instanceof TamableAnimal tameable) || !Objects.equals(tameable.getOwner(), owner);
	}

	@Override
	public boolean canAttack(LivingEntity target)
	{
		return !isHatchling() && !hasControllingPassenger() && super.canAttack(target);
	}
	
	@Override
	protected void dropEquipment()
	{
		super.dropEquipment();
		
		//Dont drop equipment if the dragon is selected and can be summoned again
		if(getOwner() instanceof Player player){
			DragonOwnerCapability capability = PlayerStateUtils.getHandler(player);
			
			if(capability.isSelectedDragon(this)){
				return;
			}
		}
		
		if (this.inventory != null) {
			for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
				ItemStack itemstack = this.inventory.getItem(i);
				if (!itemstack.isEmpty() && !EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
					this.spawnAtLocation(itemstack);
				}
			}
		}
	}
	
	@Override
	public boolean isShiftKeyDown()
	{
		if(getControllingPassenger() != null && getControllingPassenger().isShiftKeyDown()){
			return true;
		}
		
		if(getControllingPassenger() == null && getOwner() != null){
			if(getWanderTarget() == BlockPos.ZERO && !isOrderedToSit() && getPose() != Pose.SLEEPING) {
				if (getOwner() instanceof Player player && distanceTo(player) <= BASE_FOLLOW_RANGE) {
					return player.isShiftKeyDown();
				}
			}
		}
		
		return super.isShiftKeyDown();
	}
	
	@Override
	public void openCustomInventoryScreen(Player pPlayer)
	{
		pPlayer.openMenu(new SimpleMenuProvider((pId, pInventory, pPlayer1) -> createMenu(pId, pInventory), getDisplayName()), buf -> buf.writeInt(getId()));
	}
	
	private DragonContainerMenu createMenu(int pId, Inventory pInventory)
	{
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
		buffer.writeInt(getId());
		return new DragonContainerMenu(pId, pInventory, buffer);
	}
	
	public void updateOwnerData(){
		if(getOwner() instanceof Player player && !player.level.isClientSide){
			var handler = player.getData(DMRCapability.PLAYER_CAPABILITY);
			
			if(handler.isSelectedDragon(this)) {
				handler.setPlayer(player);
				handler.setDragon(this, DragonWhistleHandler.getDragonSummonIndex(player, getDragonUUID()));
			}
		}
	}
	
	@Override
	public void containerChanged(Container pContainer)
	{
		// Update the saved dragon so that summoning the dragon doesnt wipe the inventory
		updateOwnerData();
		setArmor();
	}
	
	@Override
	public void setCustomName(Component pName)
	{
		super.setCustomName(pName);
		updateOwnerData();
	}
}
