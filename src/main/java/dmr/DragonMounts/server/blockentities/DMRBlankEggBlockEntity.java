package dmr.DragonMounts.server.blockentities;

import dmr.DragonMounts.network.packets.BlankEggSyncPacket;
import dmr.DragonMounts.registry.ModBlockEntities;
import dmr.DragonMounts.registry.ModBlocks;
import dmr.DragonMounts.util.BreedingUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public class DMRBlankEggBlockEntity extends BlockEntity {

    public DMRBlankEggBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BLANK_EGG_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Getter
    @Setter
    private String targetBreedId;

    @Getter
    @Setter
    private int changeTime = 0;

    public float renderProgress = 0;
    public static final int MAX_RENDER_PROGRESS = 60;

    @Override
    protected void saveAdditional(CompoundTag pTag, Provider registries) {
        super.saveAdditional(pTag, registries);

        if (targetBreedId != null) {
            pTag.putString("targetBreedId", targetBreedId);
        }

        pTag.putInt("changeTime", changeTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("targetBreedId")) {
            targetBreedId = tag.getString("targetBreedId");
        }

        changeTime = tag.getInt("changeTime");
    }

    public int tickCount = 0;

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        tickCount++;
        if (tickCount % 20 == 0) {
            var targetBreed = BreedingUtils.getHabitatBreedOutcome(serverLevel, pos);

            if (targetBreed == null) {
                if (targetBreedId != null && !targetBreedId.isEmpty() && changeTime > 0) {
                    changeTime--;
                } else if (targetBreedId != null && !targetBreedId.isEmpty()) {
                    targetBreedId = null;
                    changeTime = 0;
                }
            } else {
                if (targetBreedId == null || targetBreedId.isEmpty()) {
                    targetBreedId = targetBreed.getId();
                    changeTime = 0;
                } else {
                    if (targetBreedId.equals(targetBreed.getId()) && changeTime < MAX_RENDER_PROGRESS) {
                        changeTime++;
                    } else if (!targetBreedId.equals(targetBreed.getId()) && changeTime > 0) {
                        changeTime--;
                    } else if (!targetBreedId.equals(targetBreed.getId()) && changeTime <= 0) {
                        targetBreedId = targetBreed.getId();
                    }
                }
            }

            PacketDistributor.sendToPlayersNear(
                    serverLevel,
                    null,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    64,
                    new BlankEggSyncPacket(pos, targetBreedId == null ? "" : targetBreedId, getChangeTime()));

            if (targetBreedId != null && !targetBreedId.isEmpty()) {
                if (targetBreed != null && targetBreedId.equals(targetBreed.getId())) {
                    if (changeTime >= MAX_RENDER_PROGRESS) {
                        changeTime = 0;
                        targetBreedId = null;
                        level.setBlockAndUpdate(
                                pos, ModBlocks.DRAGON_EGG_BLOCK.get().defaultBlockState());
                        var blockEntity = level.getBlockEntity(pos);

                        if (blockEntity instanceof DMREggBlockEntity eggBlockEntity) {
                            eggBlockEntity.setBreed(targetBreed);
                        }
                    }
                }
            }
        }
    }
}
