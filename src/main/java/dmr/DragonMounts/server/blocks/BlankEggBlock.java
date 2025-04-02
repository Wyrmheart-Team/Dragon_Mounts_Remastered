package dmr.DragonMounts.server.blocks;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModBlockEntities;
import dmr.DragonMounts.server.blockentities.DMRBlankEggBlockEntity;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

public class BlankEggBlock extends DragonEggBlock implements EntityBlock, SimpleWaterloggedBlock {

	public BlankEggBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(WATERLOGGED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new DMRBlankEggBlockEntity(pPos, pState);
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level _level, BlockState _state, BlockEntityType<T> type) {
		return type != ModBlockEntities.BLANK_EGG_BLOCK_ENTITY.get() ? null : cast(((level, pos, state, be) -> be.tick(level, pos, state)));
	}

	@SuppressWarnings("unchecked")
	private static <F extends BlockEntityTicker<DMRBlankEggBlockEntity>, T> T cast(F from) {
		return (T) from;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState p_220074_1_) {
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public BlockState updateShape(
		BlockState state,
		Direction pDirection,
		BlockState pNeighborState,
		LevelAccessor level,
		BlockPos pCurrentPos,
		BlockPos pNeighborPos
	) {
		if (state.getValue(WATERLOGGED)) level.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

		return super.updateShape(state, pDirection, pNeighborState, level, pCurrentPos, pNeighborPos);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext p_196258_1_) {
		return super
			.getStateForPlacement(p_196258_1_)
			.setValue(WATERLOGGED, p_196258_1_.getLevel().getFluidState(p_196258_1_.getClickedPos()).getType() == Fluids.WATER);
	}

	@Override
	public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource random) {
		if (
			pLevel.getBlockEntity(pPos) instanceof DMRBlankEggBlockEntity e &&
			e.getTargetBreedId() != null &&
			!e.getTargetBreedId().isEmpty()
		) {
			for (int i = 0; i < random.nextIntBetweenInclusive(1, 3); i++) {
				addHatchingParticles(DragonBreedsRegistry.getDragonBreed(e.getTargetBreedId()), pLevel, pPos, random);
			}
		}
	}

	public void addHatchingParticles(IDragonBreed breed, Level level, BlockPos pos, RandomSource random) {
		double px = pos.getX() + random.nextDouble();
		double py = pos.getY() + random.nextDouble();
		double pz = pos.getZ() + random.nextDouble();
		double ox = 0;
		double oy = 0;
		double oz = 0;

		var particle = getHatchingParticles(breed, random);
		if (particle.getType() == ParticleTypes.DUST) {
			py = pos.getY() + (random.nextDouble() - 0.5) + 1;
		} else if (particle.getType() == ParticleTypes.PORTAL) {
			ox = (random.nextDouble() - 0.5) * 2;
			oy = (random.nextDouble() - 0.5) * 2;
			oz = (random.nextDouble() - 0.5) * 2;
		}

		level.addParticle(particle, px, py, pz, ox, oy, oz);
	}

	public static ParticleOptions getHatchingParticles(IDragonBreed breed, RandomSource random) {
		if (breed.getHatchParticles() != null) {
			return breed.getHatchParticles();
		}

		return dustParticleFor(breed, random);
	}

	public static DustParticleOptions dustParticleFor(IDragonBreed breed, RandomSource random) {
		var vec = Vec3.fromRGB24(random.nextDouble() < 0.75 ? breed.getPrimaryColor() : breed.getSecondaryColor());
		return new DustParticleOptions(new Vector3f((float) vec.x, (float) vec.y, (float) vec.z), 1);
	}
}
