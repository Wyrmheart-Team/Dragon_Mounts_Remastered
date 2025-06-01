package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.network.PacketHelper;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet for synchronizing dragon breath attack targets between client and server.
 * <p>
 * This packet can be used to:
 * <ul>
 *   <li>Target a specific entity with a breath attack</li>
 *   <li>Target a position in the world with a breath attack</li>
 *   <li>Stop an ongoing breath attack</li>
 * </ul>
 */
public class DragonBreathTargetSyncPacket extends AbstractMessage<DragonBreathTargetSyncPacket> {
    private static final StreamCodec<FriendlyByteBuf, DragonBreathTargetSyncPacket> STREAM_CODEC =
            PacketHelper.createCodec(DragonBreathTargetSyncPacket::encode, DragonBreathTargetSyncPacket::decode);

    /**
     * Encodes a packet to a buffer.
     *
     * @param buffer The buffer to encode to
     * @param packet The packet to encode
     */
    public static void encode(FriendlyByteBuf buffer, DragonBreathTargetSyncPacket packet) {
        buffer.writeInt(packet.dragonId);
        buffer.writeByte(packet.targetType);

        if (packet.targetType == 1) {
            buffer.writeInt(packet.entityId);
        } else if (packet.targetType == 2) {
            buffer.writeBlockPos(packet.pos);
        }
    }

    /**
     * Decodes a packet from a buffer.
     *
     * @param buffer The buffer to decode from
     * @return The decoded packet
     */
    public static DragonBreathTargetSyncPacket decode(FriendlyByteBuf buffer) {
        int dragonId = buffer.readInt();
        byte targetType = buffer.readByte();
        int entityId = -1;
        BlockPos pos = BlockPos.ZERO;

        if (targetType == 1) {
            entityId = buffer.readInt();
        } else if (targetType == 2) {
            pos = buffer.readBlockPos();
        }

        return new DragonBreathTargetSyncPacket(dragonId, targetType, entityId, pos);
    }

    @Getter
    private final int dragonId;

    @Getter
    private final byte targetType;

    @Getter
    private final int entityId;

    @Getter
    private final BlockPos pos;

    /**
     * Empty constructor for NetworkHandler.
     */
    DragonBreathTargetSyncPacket() {
        this.dragonId = -1;
        this.targetType = 0;
        this.entityId = -1;
        this.pos = BlockPos.ZERO;
    }

    /**
     * Creates a new packet with the given parameters.
     *
     * @param dragonId The ID of the dragon
     * @param targetType The type of target (0 = stop, 1 = entity, 2 = position)
     * @param entityId The ID of the target entity (if targetType = 1)
     * @param pos The target position (if targetType = 2)
     */
    public DragonBreathTargetSyncPacket(int dragonId, byte targetType, int entityId, BlockPos pos) {
        this.dragonId = dragonId;
        this.targetType = targetType;
        this.entityId = entityId;
        this.pos = pos;
    }

    @Override
    protected String getTypeName() {
        return "dragon_breath_target_sync";
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, DragonBreathTargetSyncPacket> streamCodec() {
        return STREAM_CODEC;
    }

    /**
     * Creates a packet to target an entity with a breath attack.
     *
     * @param dragonId The ID of the dragon
     * @param entityId The ID of the target entity
     * @return A new packet
     */
    public static DragonBreathTargetSyncPacket forEntityTarget(int dragonId, int entityId) {
        return new DragonBreathTargetSyncPacket(dragonId, (byte) 1, entityId, BlockPos.ZERO);
    }

    /**
     * Creates a packet to target a position with a breath attack.
     *
     * @param dragonId The ID of the dragon
     * @param pos The position to target
     * @return A new packet
     */
    public static DragonBreathTargetSyncPacket forPositionTarget(int dragonId, BlockPos pos) {
        return new DragonBreathTargetSyncPacket(dragonId, (byte) 2, -1, pos);
    }

    /**
     * Creates a packet to stop a breath attack.
     *
     * @param dragonId The ID of the dragon
     * @return A new packet
     */
    public static DragonBreathTargetSyncPacket forStopBreath(int dragonId) {
        return new DragonBreathTargetSyncPacket(dragonId, (byte) 0, -1, BlockPos.ZERO);
    }

    @Override
    public void handle(IPayloadContext context, Player player) {
        var level = player.level;
        var entity = level.getEntity(dragonId);

        if (entity instanceof TameableDragonEntity dragon) {
            if (targetType == 0) {
                // Stop breath attack
                dragon.stopBreathAttack();
            } else if (targetType == 1) {
                // Entity target
                Entity targetEntity = level.getEntity(entityId);
                if (targetEntity instanceof LivingEntity livingEntity) {
                    dragon.setBreathAttackTarget(livingEntity);
                }
            } else if (targetType == 2) {
                // Position target
                dragon.setBreathAttackBlock(pos);
            }
        }
    }
}
