package dmr.DragonMounts.server.blockentities;

import dmr.DragonMounts.DMRConstants.NBTConstants;
import dmr.DragonMounts.common.config.DMRConfig;
import dmr.DragonMounts.registry.DMRBlockEntities;
import dmr.DragonMounts.registry.DMREntities;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static dmr.DragonMounts.server.blocks.DragonMountsEggBlock.HATCHING;
import static dmr.DragonMounts.server.blocks.DragonMountsEggBlock.HATCH_STAGE;

public class DragonEggBlockEntity extends BlockEntity
{
	private @Getter
	@Setter String breedId;
	private @Getter
	@Setter int hatchTime = DMRConfig.HATCH_TIME_CONFIG.get();
	
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
		
		pTag.putInt("hatchTime", getHatchTime());
		
		if (getCustomName() != null) pTag.putString("name", Component.Serializer.toJson(customName, registries));
	}
	
	@Override
	protected void loadAdditional(CompoundTag tag, Provider registries)
	{
		super.loadAdditional(tag, registries);
		setBreedId(tag.getString(NBTConstants.BREED));
		setHatchTime(tag.getInt("hatchTime"));
		
		var name = tag.getString("name");
		if (!name.isBlank()) setCustomName(Component.Serializer.fromJson(name, registries));
	}
	
	
	public Component getCustomName()
	{
		return customName;
	}
	
	public void setCustomName(Component name)
	{
		this.customName = name;
	}
	
	int tickCount = 0;
	
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
					
					if (stage != pState.getValue(HATCH_STAGE)) pLevel.setBlockAndUpdate(pPos, pState.setValue(HATCH_STAGE, stage));
					
					if (pState.getValue(HATCH_STAGE) == 3) {
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
		var baby = DMREntities.DRAGON_ENTITY.get().create(level);
		
		level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 1.2f, 0.95f + level.getRandom().nextFloat() * 0.2f);
		level.removeBlock(pos, false); // remove block AFTER data is cached
		
		baby.setBreed(data.getBreed());
		baby.setBaby(true);
		baby.setPos(pos.getX(), pos.getY(), pos.getZ());
		
		if (data.getCustomName() != null) baby.setCustomName(data.getCustomName());
		
		level.addFreshEntity(baby);
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
