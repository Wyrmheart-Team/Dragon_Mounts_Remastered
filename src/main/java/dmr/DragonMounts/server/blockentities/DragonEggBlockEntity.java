package dmr.DragonMounts.server.blockentities;

import dmr.DragonMounts.DMRConstants.NBTConstants;
import dmr.DragonMounts.common.config.DMRConfig;
import dmr.DragonMounts.registry.*;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import dmr.DragonMounts.util.PlayerStateUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

import static dmr.DragonMounts.server.blocks.DragonMountsEggBlock.HATCHING;

public class DragonEggBlockEntity extends BlockEntity {
	private @Getter
	@Setter String breedId;
	
	private @Getter
	@Setter int hatchTime = DMRConfig.HATCH_TIME_CONFIG.get();
	
	private @Getter
	@Setter String owner;
	
	private Component customName;
	
	public DragonEggBlockEntity(BlockPos pPos, BlockState pBlockState)
	{
		super(DMRBlockEntities.DRAGON_EGG_BLOCK_ENTITY.get(), pPos, pBlockState);
	}
	
	public IDragonBreed getBreed()
	{
		return DragonBreedsRegistry.getDragonBreed(getBreedId());
	}
	
	public void setBreed(IDragonBreed breed)
	{
		setBreedId(breed.getId());
	}
	
	@Override
	protected void saveAdditional(CompoundTag pTag, Provider registries)
	{
		super.saveAdditional(pTag, registries);
		
		if (getBreedId() != null) pTag.putString(NBTConstants.BREED, getBreedId());
		
		pTag.putInt("hatchTime", getHatchTime()); pTag.putString("owner", getOwner());
		
		if (getCustomName() != null) pTag.putString("name", Component.Serializer.toJson(customName, registries));
	}
	
	@Override
	protected void loadAdditional(CompoundTag tag, Provider registries)
	{
		super.loadAdditional(tag, registries);
		setBreedId(tag.getString(NBTConstants.BREED));
		setHatchTime(tag.getInt("hatchTime")); setOwner(tag.getString("owner"));
		
		var name = tag.getString("name");
		if (!name.isBlank()) setCustomName(Component.Serializer.fromJson(name, registries));
	}
	
	@Override
	protected void applyImplicitComponents(DataComponentInput componentInput)
	{
		super.applyImplicitComponents(componentInput); setBreedId(componentInput.get(DMRComponents.DRAGON_BREED));
		setHatchTime(componentInput.getOrDefault(DMRComponents.EGG_HATCH_TIME, DMRConfig.HATCH_TIME_CONFIG.get())); setOwner(componentInput.get(DMRComponents.EGG_OWNER));
	}
	
	@Override
	protected void collectImplicitComponents(Builder components)
	{
		super.collectImplicitComponents(components); components.set(DMRComponents.DRAGON_BREED, getBreedId()); components.set(DMRComponents.EGG_HATCH_TIME, getHatchTime());
		components.set(DMRComponents.EGG_OWNER, getOwner());
	}
	
	public Component getCustomName()
	{
		return customName;
	}
	
	public void setCustomName(Component name)
	{
		this.customName = name;
	}
	
	public int tickCount = 0;
	
	public void tick(Level pLevel, BlockPos pPos, BlockState pState)
	{
		tickCount++;
		int maxHatchTime = getBreed() == null ? 1 : getBreed().getHatchTime();
		int growthStage = (maxHatchTime / 3);
		
		if (tickCount % 20 == 0) {
			if (pState.getValue(HATCHING)) {
				if (hatchTime <= 0) {
					if (!pLevel.isClientSide) hatch((ServerLevel)pLevel, pPos);
				} else {
					hatchTime -= 1;
					
					var stage = Mth.clamp((maxHatchTime - hatchTime) / growthStage, 0, 3);
					
					if (stage == 3) {
						level.playSound(null, pPos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.85f, 0.95f + level.getRandom().nextFloat() * 0.2f);
					}
				}
			}
		}
	}
	
	@SuppressWarnings( "ConstantConditions" )
	public void hatch(ServerLevel level, BlockPos pos)
	{
		var data = (DragonEggBlockEntity)level.getBlockEntity(pos);
		var baby = DMREntities.DRAGON_ENTITY.get().create(level); var ownerId = data.getOwner();
		
		level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 1.2f, 0.95f + level.getRandom().nextFloat() * 0.2f);
		level.removeBlock(pos, false); // remove block AFTER data is cached
		
		baby.setBreed(data.getBreed());
		baby.setBaby(true);
		baby.setPos(pos.getX(), pos.getY(), pos.getZ());
		
		if (data.getCustomName() != null) baby.setCustomName(data.getCustomName());
		
		level.addFreshEntity(baby);
		
		if (ownerId != null && !ownerId.isBlank()) {
			UUID owner = UUID.fromString(data.getOwner()); var player = level.getPlayerByUUID(owner); if (player instanceof ServerPlayer serverPlayer) {
				var state = PlayerStateUtils.getHandler(player); state.dragonsHatched++;
				
				
				if (state.dragonsHatched >= 1) {
					DMRCriterionTriggers.HATCH_DRAGON_EGG.get().trigger(serverPlayer);
				}
				
				if (state.dragonsHatched >= 5) {
					DMRCriterionTriggers.HATCH_5_DRAGON_EGGS.get().trigger(serverPlayer);
				}
				
				if (state.dragonsHatched >= 10) {
					DMRCriterionTriggers.HATCH_10_DRAGON_EGGS.get().trigger(serverPlayer);
				}
				
				if (state.dragonsHatched == 100) {
					DMRCriterionTriggers.HATCH_100_DRAGON_EGGS.get().trigger(serverPlayer);
				}
				
				if (data.getBreed().isHybrid()) {
					DMRCriterionTriggers.IS_HYBRID_HATCH_TRIGGER.get().trigger(serverPlayer);
				} else {
					DMRCriterionTriggers.HATCH_TRIGGER.get().trigger(serverPlayer, data.getBreedId());
				}
			}
		}
	}
	
	
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	@Override
	public CompoundTag getUpdateTag(Provider registries)
	{
		var tag = super.getUpdateTag(registries);
		saveAdditional(tag, registries);
		return tag;
	}
	
	public boolean isModelReady()
	{
		return getLevel() != null && getBreedId() != null;
	}
}
