package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet for triggering a dragon breath attack.
 */
public class DragonBreathPacket extends AbstractMessage<DragonBreathPacket> {
    private static final StreamCodec<FriendlyByteBuf, DragonBreathPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, DragonBreathPacket::getEntityId, DragonBreathPacket::new);

    @Getter
    private final int entityId;

    /**
     * Empty constructor for NetworkHandler.
     */
    DragonBreathPacket() {
        this.entityId = -1;
    }

    /**
     * Creates a new packet with the given entity ID.
     *
     * @param entityId The ID of the entity
     */
    public DragonBreathPacket(int entityId) {
        this.entityId = entityId;
    }

    @Override
    protected String getTypeName() {
        return "dragon_breath";
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, DragonBreathPacket> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void handle(IPayloadContext context, Player player) {
        var level = player.level;
        var entity = player.level.getEntity(entityId);

        if (entity instanceof TameableDragonEntity dragon) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVector = player.getLookAngle();
            Vec3 targetPos = eyePos.add(lookVector.scale(10));

            // Create a larger bounding box in the direction the player is looking
            double searchDistance = 10.0;
            AABB searchBox = player.getBoundingBox().inflate(searchDistance);

            // Find all valid entities within the search box
            var potentialTargets = level.getEntities(
                    player,
                    searchBox,
                    ent -> ent instanceof LivingEntity livingEntity && dragon.canHarmWithBreath(livingEntity));

            // Find the closest entity that's in front of the player
            LivingEntity closestTarget = null;
            double closestDistance = Double.MAX_VALUE;

            for (Entity potentialTarget : potentialTargets) {
                // Check if the entity is in front of the player (dot product > 0)
                Vec3 directionToTarget =
                        potentialTarget.position().subtract(eyePos).normalize();
                double dotProduct = directionToTarget.dot(lookVector);

                // Only consider entities that are in the general direction the player is looking
                if (dotProduct > 0.7) { // Approximately within a 45-degree cone
                    double distance = potentialTarget.distanceToSqr(eyePos);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestTarget = (LivingEntity) potentialTarget;
                    }
                }
            }

            // If we found a valid target, use it
            if (closestTarget != null) {
                dragon.setBreathAttackTarget(closestTarget);
                return;
            }

            // If no entity target was found, check for block hits
            var clipContext =
                    new ClipContext(eyePos, targetPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player);
            var blockHitResult = level.clip(clipContext);

            if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                var blockPos = blockHitResult.getBlockPos();
                var blockState = level.getBlockState(blockPos);

                if (!blockState.isAir()) {
                    dragon.setBreathAttackBlock(blockPos);
                    return;
                }
            }

            // If no entity or block was hit, target the position
            dragon.setBreathAttackPosition(targetPos);
        }
    }
}
