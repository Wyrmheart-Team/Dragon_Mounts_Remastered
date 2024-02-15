package dmr.DragonMounts.server.blocks;

import dmr.DragonMounts.DMRConstants.NBTConstants;
import dmr.DragonMounts.registry.DMRBlockEntities;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.server.items.dev.InstantHatchItem;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import dmr.DragonMounts.server.blockentities.DragonEggBlockEntity;
import dmr.DragonMounts.server.items.DragonEggItemBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;


import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class DragonMountsEggBlock extends DragonEggBlock implements EntityBlock, SimpleWaterloggedBlock
{
	public static final IntegerProperty HATCH_STAGE = IntegerProperty.create("hatch_stage", 0, 3);
	public static final BooleanProperty HATCHING = BooleanProperty.create("hatching");
	
	public DragonMountsEggBlock(Properties pProperties)
	{
		super(pProperties);
		registerDefaultState(defaultBlockState()
				                     .setValue(HATCH_STAGE, 0)
				                     .setValue(HATCHING, false)
				                     .setValue(WATERLOGGED, false));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder)
	{
		pBuilder.add(HATCH_STAGE, HATCHING, WATERLOGGED);
	}
	
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
	{
		return new DragonEggBlockEntity(pPos, pState);
	}
	
	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack)
	{
		if(pLevel.getBlockEntity(pPos) instanceof DragonEggBlockEntity e){
			if(pStack.hasTag()) {
				if (pStack.getTag().contains(NBTConstants.BREED)) {
					e.setBreedId(pStack.getTag().getString(NBTConstants.BREED));
				}
				
				if (pStack.getTag().contains("hatchTime")) {
					e.setHatchTime(pStack.getTag().getInt("hatchTime"));
				}
				
				if (pStack.getTag().contains("hatching")) {
					if(pStack.getTag().getBoolean("hatching")) {
						pLevel.setBlock(pPos, pState.setValue(HATCHING, true), Block.UPDATE_ALL);
					}
				}
			}
		}
	}
	
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level _level, BlockState _state, BlockEntityType<T> type)
	{
		return type != DMRBlockEntities.DRAGON_EGG_BLOCK_ENTITY.get()? null :
				cast(((level, pos, state, be) -> be.tick(level, pos, state)));
	}
	
	@SuppressWarnings("unchecked")
	private static <F extends BlockEntityTicker<DragonEggBlockEntity>, T> T cast(F from)
	{
		return (T) from;
	}
	
	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player)
	{
		var breedId = level.getBlockEntity(pos) instanceof DragonEggBlockEntity e? e.getBreedId() : null;
		IDragonBreed breed = DragonBreedsRegistry.getDragonBreed(breedId);
		return DragonEggItemBlock.getDragonEggStack(breed);
	}
	
	
	@Override
	public RenderShape getRenderShape(BlockState pState)
	{
		return RenderShape.MODEL;
	}
	
	@Override
	public boolean useShapeForLightOcclusion(BlockState p_220074_1_){
		return true;
	}
	
	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
	{
		if(pPlayer.getItemInHand(pHand).getItem() instanceof InstantHatchItem) return InteractionResult.PASS;
		
		if (!pState.getValue(HATCHING))
		{
			if (!pLevel.isClientSide)
			{
				pLevel.setBlock(pPos, pState.setValue(HATCHING, true), Block.UPDATE_ALL);
				return InteractionResult.CONSUME;
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
	
	@Override
	public void attack(BlockState state, Level level, BlockPos at, Player pPlayer)
	{
		if (level.getBlockEntity(at) instanceof DragonEggBlockEntity e
		    && e.getBreedId().equals("end")
		    && !state.getValue(HATCHING))
			teleport(state, level, at); // retain original dragon egg teleport behavior
	}
	
	
	@Override
	public BlockState updateShape(BlockState state, Direction pDirection, BlockState pNeighborState, LevelAccessor level, BlockPos pCurrentPos, BlockPos pNeighborPos)
	{
		if (state.getValue(WATERLOGGED))
			level.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		
		return super.updateShape(state, pDirection, pNeighborState, level, pCurrentPos, pNeighborPos);
	}
	
	@Override
	public FluidState getFluidState(BlockState state)
	{
		return state.getValue(WATERLOGGED)? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext p_196258_1_){
		return super.getStateForPlacement(p_196258_1_).setValue(WATERLOGGED, p_196258_1_.getLevel().getFluidState(p_196258_1_.getClickedPos()).getType() == Fluids.WATER);
	}
	
	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom)
	{
		// Original logic trashes BlockEntity data. We need it, so do it ourselves.
		if (isFree(pLevel.getBlockState(pPos.below())) && pPos.getY() >= pLevel.getMinBuildHeight())
		{
			CompoundTag tag = null;
			if (pLevel.getBlockEntity(pPos) instanceof DragonEggBlockEntity e)
				tag = e.saveWithoutMetadata();
			
			var entity = FallingBlockEntity.fall(pLevel, pPos, pState); // this deletes the block. We need to cache the data first and then apply it.
			if (tag != null) entity.blockData = tag;
			falling(entity);
		}
	}
	
	public static DragonEggBlockEntity place(ServerLevel level, BlockPos pos, BlockState state, IDragonBreed breed)
	{
		level.setBlock(pos, state, Block.UPDATE_ALL);
		
		// Forcibly add new BlockEntity, so we can set the specific breed.
		var data = ((DragonEggBlockEntity) ((DragonMountsEggBlock) state.getBlock()).newBlockEntity(pos, state));
		data.setBreed(breed);
		data.setHatchTime(breed.getHatchTime() - ((breed.getHatchTime()/3) * state.getValue(HATCH_STAGE)));
		level.setBlockEntity(data);
		level.updateNeighborsAt(pos, state.getBlock());
		return data;
	}
	
	@Override
	public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource random)
	{
		if (pState.getValue(HATCHING) && pLevel.getBlockEntity(pPos) instanceof DragonEggBlockEntity e)
			for (int i = 0; i < random.nextIntBetweenInclusive(4, 7); i++)
				addHatchingParticles(e.getBreed(), pLevel, pPos, random);
	}
	
	public void addHatchingParticles(IDragonBreed breed, Level level, BlockPos pos, RandomSource random)
	{
		double px = pos.getX() + random.nextDouble();
		double py = pos.getY() + random.nextDouble();
		double pz = pos.getZ() + random.nextDouble();
		double ox = 0;
		double oy = 0;
		double oz = 0;
		
		var particle = getHatchingParticles(breed, random);
		if (particle.getType() == ParticleTypes.DUST) py = pos.getY() + (random.nextDouble() - 0.5) + 1;
		else if (particle.getType() == ParticleTypes.PORTAL)
		{
			ox = (random.nextDouble() - 0.5) * 2;
			oy = (random.nextDouble() - 0.5) * 2;
			oz = (random.nextDouble() - 0.5) * 2;
		}
		
		level.addParticle(particle, px, py, pz, ox, oy, oz);
	}
	
	public static ParticleOptions getHatchingParticles(IDragonBreed breed, RandomSource random)
	{
		if(breed.getHatchParticles() != null){
			return breed.getHatchParticles();
		}
		
		return dustParticleFor(breed, random);
	}
	
	public static DustParticleOptions dustParticleFor(IDragonBreed breed, RandomSource random)
	{
		var vec = Vec3.fromRGB24(random.nextDouble() < 0.75? breed.getPrimaryColor() : breed.getSecondaryColor());
		return new DustParticleOptions(new Vector3f((float)vec.x, (float)vec.y, (float)vec.z), 1);
	}
}
