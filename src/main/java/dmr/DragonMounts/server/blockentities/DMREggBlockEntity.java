package dmr.DragonMounts.server.blockentities;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.ModConstants.NBTConstants;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.registry.ModCriterionTriggers;
import dmr.DragonMounts.registry.block.ModBlockEntities;
import dmr.DragonMounts.registry.datapack.DragonBreedsRegistry;
import dmr.DragonMounts.registry.entity.ModEntities;
import dmr.DragonMounts.types.DragonTier;
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

    private int tierLevel = 0;

    public DragonTier getTier() {
        return DragonTier.fromLevel(tierLevel);
    }

    public void setTier(DragonTier tier) {
        this.tierLevel = tier.getLevel();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, Provider registries) {
        super.saveAdditional(pTag, registries);

        if (getBreedId() != null) pTag.putString(NBTConstants.BREED, getBreedId());

        pTag.putInt("hatchTime", getHatchTime());
        pTag.putString("owner", getOwner() == null ? "" : getOwner());
        pTag.putInt("tierLevel", tierLevel);
        
        if(getDragonOutcomeTag() != null) {
            pTag.put("eggOutcome", getDragonOutcomeTag().copy());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);
        setBreedId(tag.getString(NBTConstants.BREED));
        setHatchTime(tag.getInt("hatchTime"));
        setOwner(tag.getString("owner"));
        tierLevel = tag.getInt("tierLevel");
        
        if(tag.contains("eggOutcome")) {
            dragonOutcomeTag = tag.getCompound("eggOutcome");
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        setBreedId(componentInput.get(ModComponents.DRAGON_BREED));
        setHatchTime(
                componentInput.getOrDefault(ModComponents.EGG_HATCH_TIME, ServerConfig.HATCH_TIME_CONFIG.intValue()));
        setOwner(componentInput.get(ModComponents.EGG_OWNER));
        setDragonOutcomeTag(componentInput.get(ModComponents.EGG_OUTCOME));

        if (ServerConfig.ENABLE_DRAGON_TIERS) {
            setTier(DragonTier.fromLevel(tierLevel));
        }
    }

    @Override
    protected void collectImplicitComponents(Builder components) {
        super.collectImplicitComponents(components);
        components.set(ModComponents.DRAGON_BREED, getBreedId());
        components.set(ModComponents.EGG_HATCH_TIME, getHatchTime());
        components.set(ModComponents.EGG_OWNER, getOwner());
        components.set(ModComponents.EGG_OUTCOME, getDragonOutcomeTag());

        if (ServerConfig.ENABLE_DRAGON_TIERS) {
            components.set(ModComponents.DRAGON_TIER, tierLevel);
        }
    }

    public int tickCount = 0;

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        tickCount++;
        int maxHatchTime = getBreed() == null ? 1 : getBreed().getHatchTime();
        if (ServerConfig.ENABLE_DRAGON_TIERS) {
            maxHatchTime = (int) (maxHatchTime * (1 + (getTier().getLevel() * 0.1)));
        }

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

        if(dragonOutcomeTag != null && !dragonOutcomeTag.isEmpty()) {
            try {
                baby.readAdditionalSaveData(dragonOutcomeTag);
            }catch (Exception e) {
                DMR.LOGGER.error("There was an error trying to parse the dragon outcome tag", e);
            }
        }
        
        baby.setBreed(data.getBreed());
        baby.setVariant(data.getVariantId());

        var age = -Math.abs(data.getBreed().getGrowthTime());
        if (ServerConfig.ENABLE_DRAGON_TIERS) {
            baby.setTier(data.getTier());
            age = (int) -Math.abs(
                    data.getBreed().getGrowthTime() * (1 + (data.getTier().getLevel() * 0.1)));
        }

        baby.setBaby(true);
        baby.setAge(age);
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
