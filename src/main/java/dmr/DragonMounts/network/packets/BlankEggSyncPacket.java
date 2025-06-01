package dmr.DragonMounts.network.packets;

import dmr.DragonMounts.network.AbstractMessage;
import dmr.DragonMounts.server.blockentities.DMRBlankEggBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class BlankEggSyncPacket extends AbstractMessage<BlankEggSyncPacket> {
    private static final StreamCodec<FriendlyByteBuf, BlankEggSyncPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BlankEggSyncPacket::getPos,
            ByteBufCodecs.STRING_UTF8,
            BlankEggSyncPacket::getTargetBreed,
            ByteBufCodecs.INT,
            BlankEggSyncPacket::getChangeTime,
            BlankEggSyncPacket::new);

    @Getter
    private final BlockPos pos;

    @Getter
    private final String targetBreed;

    @Getter
    private final int changeTime;

    /**
     * Empty constructor for NetworkHandler.
     */
    BlankEggSyncPacket() {
        this.pos = BlockPos.ZERO;
        this.targetBreed = "";
        this.changeTime = -1;
    }

    /**
     * Creates a new packet with the given parameters.
     *
     * @param pos The position of the egg
     * @param targetBreed The target breed ID
     * @param changeTime The change time
     */
    public BlankEggSyncPacket(BlockPos pos, String targetBreed, int changeTime) {
        this.pos = pos;
        this.targetBreed = targetBreed;
        this.changeTime = changeTime;
    }

    @Override
    protected String getTypeName() {
        return "blank_egg_sync";
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, BlankEggSyncPacket> streamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public void handle(IPayloadContext supplier, Player player) {
        var level = player.level();
        var blockEntity = level.getBlockEntity(new BlockPos(pos.getX(), pos.getY(), pos.getZ()));

        if (blockEntity instanceof DMRBlankEggBlockEntity eggBlockEntity) {
            eggBlockEntity.setTargetBreedId(targetBreed);
            eggBlockEntity.setChangeTime(changeTime);
        }
    }
}
