package dmr.DragonMounts.server.entity.dragon;

import dmr.DragonMounts.client.particle.particleoptions.DragonBreathParticleOptions;
import dmr.DragonMounts.network.packets.DragonBreathTargetSyncPacket;
import dmr.DragonMounts.types.breath.DragonBreathType;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3d;

/**
 * Abstract class that implements dragon breath attack functionality.
 * This extends the dragon entity hierarchy with breath attack capabilities.
 */
abstract class DragonBreathComponent extends DragonAnimationComponent {

    @Getter
    private static final double breathLength = 2.5; // 5 * 0.5

    private static final double breathRange = 4;

    // Breath attack properties
    @Getter
    @Setter
    protected Vector3d breathSourcePosition;

    @Getter
    protected PositionTracker breathTarget;

    protected int breathTime = -1;

    protected DragonBreathComponent(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Renders the visual effects of the dragon's breath attack.
     */
    @OnlyIn(Dist.CLIENT)
    public void renderDragonBreath() {
        if (breathSourcePosition != null) {
            var dragonSource = position().add(breathSourcePosition.x, breathSourcePosition.y, breathSourcePosition.z);
            var target = getBreathTarget().currentPosition().add(0, 0.5, 0);

            var diff = target.subtract(dragonSource).normalize();

            var breathSource = new Vec3(breathSourcePosition.x, breathSourcePosition.y, breathSourcePosition.z)
                    .add(diff.scale(0.5f));

            var breathType = getDragon().getBreed().getBreathType();
            if (breathType == null) return;
            var particleDensity = breathType.getParticleDensity();

            for (int i = 0; i < particleDensity; i++) {
                Vec3 speed = new Vec3(
                        diff.x * (0.5f + (getRandom().nextFloat() / 4)),
                        diff.y,
                        diff.z * (0.5f + (getRandom().nextFloat() / 4)));

                var breathOptions = new DragonBreathParticleOptions(breathType);

                level().addParticle(
                                breathOptions,
                                getX() + breathSource.x,
                                getY() + breathSource.y,
                                getZ() + breathSource.z,
                                speed.x,
                                speed.y,
                                speed.z);
            }
        }
    }

    public void setBreathAttackTarget(LivingEntity target) {
        setTarget(new EntityTracker(target, true));
    }

    private void setTarget(PositionTracker tracker) {
        if (isValidTarget(tracker)) {
            breathTarget = tracker;
            breathTime = -1;

            // Send packet to sync the target to clients
            if (!level().isClientSide) {
                if (tracker instanceof EntityTracker entityTracker) {
                    Entity targetEntity = entityTracker.getEntity();
                    PacketDistributor.sendToPlayersTrackingEntity(
                            this, DragonBreathTargetSyncPacket.forEntityTarget(getId(), targetEntity.getId()));
                } else if (tracker instanceof BlockPosTracker blockPosTracker) {
                    PacketDistributor.sendToPlayersTrackingEntity(
                            this,
                            DragonBreathTargetSyncPacket.forPositionTarget(
                                    getId(), blockPosTracker.currentBlockPosition()));
                }
            }
        }
    }

    private boolean isValidTarget(PositionTracker tracker) {
        if (tracker == null) return false;

        // TODO Check if attack is out of range in terms of pitch and yaw

        return tracker.isVisibleBy(this) || level.isClientSide;
    }

    public void setBreathAttackPosition(Vec3 pos) {
        setTarget(new BlockPosTracker(pos));
    }

    public void setBreathAttackBlock(BlockPos pos) {
        setTarget(new BlockPosTracker(pos));
    }

    /**
     * Ticks the breath component.
     */
    public void tick() {
        super.tick();

        if (hasBreathAttack()) {
            if (hasBreathTarget()) {
                if (breathTime == -1) {
                    breathTime = (int) (20 * breathLength);
                    this.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, getBreathTarget());
                } else if (breathTime == 0) {
                    stopBreathAttack();
                    breathTime = -1;
                } else {
                    breathTime--;
                    this.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, getBreathTarget());
                    this.getDragon().triggerAnim("head-controller", "breath");

                    if (getBreathTarget() instanceof EntityTracker entityTracker) {
                        Entity targetEntity = entityTracker.getEntity();

                        if (!targetEntity.isAlive()) {
                            stopBreathAttack();
                            return;
                        }

                        attackWithBreath((LivingEntity) targetEntity);
                    }

                    var dragonPos = position();
                    var target = getBreathTarget().currentPosition().add(0, 0.5, 0);
                    var direction = target.subtract(dragonPos).normalize();
                    var breathEnd = dragonPos.add(direction.scale(breathRange));
                    var breathWidth = 2;

                    // Create a bounding box along the breath path
                    var minX = Math.min(dragonPos.x, breathEnd.x) - breathWidth; // Add width to the breath
                    var minY = Math.min(dragonPos.y, breathEnd.y) - breathWidth;
                    var minZ = Math.min(dragonPos.z, breathEnd.z) - breathWidth;
                    var maxX = Math.max(dragonPos.x, breathEnd.x) + breathWidth;
                    var maxY = Math.max(dragonPos.y, breathEnd.y) + breathWidth;
                    var maxZ = Math.max(dragonPos.z, breathEnd.z) + breathWidth;

                    var breathBoundingBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

                    var entitiesInRange = level
                            .getEntities(
                                    this,
                                    breathBoundingBox,
                                    ent -> ent instanceof LivingEntity living && canHarmWithBreath(living))
                            .stream()
                            .map(s -> (LivingEntity) s)
                            .toList();
                    for (LivingEntity entity : entitiesInRange) {
                        if (getBreathTarget() instanceof EntityTracker entityTracker) {
                            if (entityTracker.getEntity().equals(entity)) continue;
                        }
                        attackWithBreath(entity);
                    }
                }
            }
        }
    }

    /**
     * Checks if the dragon has a breath attack.
     */
    public boolean hasBreathAttack() {
        return getDragon().getBreed().getBreathType() != null;
    }

    public boolean hasBreathTarget() {
        return breathTarget != null;
    }

    public void stopBreathAttack() {
        breathTime = 0;
        breathTarget = null;
        this.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        this.getDragon().stopTriggeredAnim("head-controller", "breath");

        // Send packet to notify clients to stop the breath attack
        if (!level().isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntity(this, DragonBreathTargetSyncPacket.forStopBreath(getId()));
        }
    }

    /**
     * Applies damage to the target from the breath attack.
     */
    public void attackWithBreath(LivingEntity target) {
        if (!canHarmWithBreath(target)) return;
        if (getControllingPassenger() != null && getControllingPassenger() == target) return;

        var breathType = getDragon().getBreed().getBreathType();
        if (breathType == null) return;

        // Apply damage using the custom damage source from the breath type
        target.hurt(breathType.getDamageSource(this), breathType.getDamage());

        // Apply fire if applicable
        if (breathType.getFireTime() > 0) {
            target.setRemainingFireTicks(breathType.getFireTime());
        }

        // Apply effects
        for (DragonBreathType.BreathEffect effect : breathType.getEffects()) {
            if (getRandom().nextFloat() <= effect.getChance()) {
                var effectId = effect.getEffectId();
                if (effectId == null) continue;

                var effectResourceLoc = ResourceLocation.parse(effectId);
                var effectType = BuiltInRegistries.MOB_EFFECT
                        .getHolder(effectResourceLoc)
                        .orElse(null);
                if (effectType == null) continue;

                var instance = new MobEffectInstance(effectType, effect.getDuration(), effect.getAmplifier());
                target.addEffect(instance);
            }
        }
    }

    /**
     * Checks if the dragon can harm the target with its breath.
     */
    public boolean canHarmWithBreath(LivingEntity target) {
        return getOwner() == null
                || target != getOwner() && getOwner().canAttack(target) && !target.isAlliedTo(getOwner());
    }
}
