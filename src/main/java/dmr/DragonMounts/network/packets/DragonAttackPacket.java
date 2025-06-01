package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.registry.ModCriterionTriggers;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet for triggering a dragon attack.
 */
public class DragonAttackPacket extends AbstractMessage<DragonAttackPacket> {
    private static final StreamCodec<FriendlyByteBuf, DragonAttackPacket> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.INT, DragonAttackPacket::getEntityId, DragonAttackPacket::new);

    @Getter
    private final int entityId;

    /**
     * Empty constructor for NetworkHandler.
     */
    DragonAttackPacket() {
        this.entityId = -1;
    }

    /**
     * Creates a new packet with the given entity ID.
     *
     * @param entityId The ID of the entity
     */
    public DragonAttackPacket(int entityId) {
        this.entityId = entityId;
    }

    @Override
    protected String getTypeName() {
        return "dragon_attack";
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, DragonAttackPacket> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void handle(IPayloadContext context, Player player) {
        var entity = player.level.getEntity(entityId);

        if (entity instanceof TameableDragonEntity dragon) {
            dragon.swing(InteractionHand.MAIN_HAND);
            dragon.triggerAnim("head-controller", "bite");

            var dimensions = dragon.getDimensions(dragon.getPose());
            float degrees = Mth.wrapDegrees(player.yBodyRot);

            double yawRadians = -Math.toRadians(degrees);
            double f4 = Math.sin(yawRadians);
            double f5 = Math.cos(yawRadians);
            Vec3 lookVector = new Vec3(f4 * dimensions.width() * 2, 0, f5 * dimensions.width() * 2);

            var offsetAabb = dragon.getBoundingBox().move(lookVector).inflate(2, 5, 2);

            var entities = dragon.level.getNearbyEntities(
                    LivingEntity.class,
                    TargetingConditions.forCombat().selector(player::canAttack).selector(s -> !s.isAlliedTo(player)),
                    player,
                    offsetAabb);
            var target = entities.stream()
                    .filter(e -> e != dragon && e != player)
                    .findFirst()
                    .orElse(null);

            if (target != null) {
                if (dragon.doHurtTarget(target)) {
                    if (target.isDeadOrDying()) {
                        if (player instanceof ServerPlayer serverPlayer) {
                            ModCriterionTriggers.DEFEAT_WITH_DRAGON.get().trigger(serverPlayer);
                        }
                    }
                }
            }
        }
    }
}
