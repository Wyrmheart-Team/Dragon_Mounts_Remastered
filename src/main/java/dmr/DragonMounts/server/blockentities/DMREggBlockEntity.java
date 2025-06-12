package dmr.DragonMounts.server.blockentities;

import dmr.DragonMounts.ModConstants.NBTConstants;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.*;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.util.PlayerStateUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

import static dmr.DragonMounts.server.blocks.DMREggBlock.HATCHING;
@Getter
@Setter
public class DMREggBlockEntity extends BlockEntity {
    
    private String breedId;
    private String variantId = "";
    private CompoundTag dragonOutcomeTag;
    private int hatchTime = ServerConfig.HATCH_TIME_CONFIG.intValue();
    private String owner;
    
    public DMREggBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.DRAGON_EGG_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public DragonBreed getBreed() {
        return DragonBreedsRegistry.getDragonBreed(getBreedId());
    }

    public void setBreed(DragonBreed breed) {
        setBreedId(breed.getId());
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, Provider registries) {
        super.saveAdditional(pTag, registries);

        if (getBreedId() != null) pTag.putString(NBTConstants.BREED, getBreedId());

        pTag.putInt("hatchTime", getHatchTime());
        pTag.putString("owner", getOwner() == null ? "" : getOwner());
        pTag.put("eggOutcome", getDragonOutcomeTag());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);
        setBreedId(tag.getString(NBTConstants.BREED));
        setHatchTime(tag.getInt("hatchTime"));
        setOwner(tag.getString("owner"));
        setDragonOutcomeTag(tag.getCompound("eggOutcome"));
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        setBreedId(componentInput.get(ModComponents.DRAGON_BREED));
        setHatchTime(
                componentInput.getOrDefault(ModComponents.EGG_HATCH_TIME, ServerConfig.HATCH_TIME_CONFIG.intValue()));
        setOwner(componentInput.get(ModComponents.EGG_OWNER));
        setDragonOutcomeTag(componentInput.get(ModComponents.EGG_OUTCOME));
    }

    @Override
    protected void collectImplicitComponents(Builder components) {
        super.collectImplicitComponents(components);
        components.set(ModComponents.DRAGON_BREED, getBreedId());
        components.set(ModComponents.EGG_HATCH_TIME, getHatchTime());
        components.set(ModComponents.EGG_OWNER, getOwner());
        components.set(ModComponents.EGG_OUTCOME, getDragonOutcomeTag());
    }

    public int tickCount = 0;

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        tickCount++;
        int maxHatchTime = getBreed() == null ? 1 : getBreed().getHatchTime();
        int growthStage = (maxHatchTime / 3);

        if (tickCount % 20 == 0) {
            if (pState.getValue(HATCHING)) {
                if (hatchTime <= 0) {
                    if (!pLevel.isClientSide) hatch((ServerLevel) pLevel, pPos);
                } else {
                    hatchTime -= 1;

                    var stage = Mth.clamp((maxHatchTime - hatchTime) / growthStage, 0, 3);

                    if (stage == 3) {
                        level.playSound(
                                null,
                                pPos,
                                SoundEvents.TURTLE_EGG_CRACK,
                                SoundSource.BLOCKS,
                                0.85f,
                                0.95f + level.getRandom().nextFloat() * 0.2f);
                    }
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void hatch(ServerLevel level, BlockPos pos) {
        var data = (DMREggBlockEntity) level.getBlockEntity(pos);
        var baby = ModEntities.DRAGON_ENTITY.get().create(level);
        var ownerId = data.getOwner();

        level.playSound(
                null,
                pos,
                SoundEvents.TURTLE_EGG_HATCH,
                SoundSource.BLOCKS,
                1.2f,
                0.95f + level.getRandom().nextFloat() * 0.2f);

        baby.readAdditionalSaveData(dragonOutcomeTag);
        baby.setBreed(data.getBreed());
        baby.setVariant(data.getVariantId());

        baby.setBaby(true);
        baby.setAge(-Math.abs(data.getBreed().getGrowthTime()));
        baby.setPos(pos.getX(), pos.getY(), pos.getZ());
        baby.setHatched(true);
        
        if (!level.addFreshEntity(baby)) return;

        if (ownerId != null && !ownerId.isBlank()) {
            UUID owner = UUID.fromString(data.getOwner());
            var player = level.getPlayerByUUID(owner);
            if (player instanceof ServerPlayer serverPlayer) {
                var state = PlayerStateUtils.getHandler(player);
                state.dragonsHatched++;

                ModCriterionTriggers.HATCH_COUNT_TRIGGER.get().trigger(serverPlayer, state.dragonsHatched);
                ModCriterionTriggers.HATCH_TRIGGER.get().trigger(serverPlayer, data.getBreedId());
            }
        }

        level.removeBlock(pos, false);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(Provider registries) {
        var tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }
}
