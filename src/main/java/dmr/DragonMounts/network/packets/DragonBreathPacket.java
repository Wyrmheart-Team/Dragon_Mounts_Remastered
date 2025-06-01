package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
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
            AABB aabb = player.getBoundingBox().expandTowards(targetPos).inflate(1.0);
            var hitResult = ProjectileUtil.getEntityHitResult(
                    player,
                    eyePos,
                    targetPos,
                    aabb,
                    ent -> ent instanceof LivingEntity livingEntity && dragon.canHarmWithBreath(livingEntity),
                    10);

            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                if (hitResult.getEntity() instanceof LivingEntity livingEntity
                        && dragon.canHarmWithBreath(livingEntity)) {
                    dragon.setBreathAttackTarget(livingEntity);
                    return;
                }
            }

            var clipContext =
                    new ClipContext(lookVector, targetPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player);
            var blockHitResult = level.clip(clipContext);

            if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                var blockPos = blockHitResult.getBlockPos();
                var blockState = level.getBlockState(blockPos);

                if (!blockState.isAir()) {
                    dragon.setBreathAttackBlock(blockPos);
                    return;
                }
            }

            dragon.setBreathAttackPosition(targetPos);
        }
    }
}
