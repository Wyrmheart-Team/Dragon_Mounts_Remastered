package dmr.DragonMounts.server.blocks;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import dmr.DragonMounts.ModConstants.NBTConstants;
import dmr.DragonMounts.config.ClientConfig;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModBlockEntities;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.server.blockentities.DMREggBlockEntity;
import dmr.DragonMounts.server.items.DragonEggItemBlock;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed.Variant;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

public class DMREggBlock extends DragonEggBlock implements EntityBlock, SimpleWaterloggedBlock {

	public static final BooleanProperty HATCHING = BooleanProperty.create("hatching");

	public DMREggBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(HATCHING, false).setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(HATCHING, WATERLOGGED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new DMREggBlockEntity(pPos, pState);
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		if (pLevel.getBlockEntity(pPos) instanceof DMREggBlockEntity e) {
			var breedId = pStack.get(ModComponents.DRAGON_BREED);
			var hatchTime = pStack.getOrDefault(ModComponents.EGG_HATCH_TIME, ServerConfig.HATCH_TIME_CONFIG.get());
			var variantId = pStack.get(ModComponents.DRAGON_VARIANT);

			e.setOwner(pPlacer.getUUID().toString());

			if (variantId != null) {
				e.setVariantId(variantId);
			}

			if (breedId != null) {
				e.setBreedId(breedId);
				e.setHatchTime(hatchTime);
			} else {
				if (pStack.has(DataComponents.CUSTOM_DATA)) {
					var customData = pStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

					var tag = customData.copyTag();
					if (tag.contains(NBTConstants.BREED)) {
						e.setBreedId(tag.getString(NBTConstants.BREED));
					}
				}
			}
		}
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level _level, BlockState _state, BlockEntityType<T> type) {
		return type != ModBlockEntities.DRAGON_EGG_BLOCK_ENTITY.get()
			? null
			: cast(((level, pos, state, be) -> be.tick(level, pos, state)));
	}

	@SuppressWarnings("unchecked")
	private static <F extends BlockEntityTicker<DMREggBlockEntity>, T> T cast(F from) {
		return (T) from;
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		var breedId = level.getBlockEntity(pos) instanceof DMREggBlockEntity e ? e.getBreedId() : null;
		var variantId = level.getBlockEntity(pos) instanceof DMREggBlockEntity e ? e.getVariantId() : null;

		IDragonBreed breed = DragonBreedsRegistry.getDragonBreed(breedId);
		var breedVariant = breed.getVariants().stream().filter(variant -> variant.id().equals(variantId)).findFirst().orElse(null);
		return DragonEggItemBlock.getDragonEggStack(breed, breedVariant);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return pState.getValue(HATCHING) && ClientConfig.MOD_CONFIG_SPEC.isLoaded() && ClientConfig.RENDER_HATCHING_EGG.get()
			? RenderShape.INVISIBLE
			: RenderShape.MODEL;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState p_220074_1_) {
		return true;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHit) {
		if (!pState.getValue(HATCHING)) {
			if (!pLevel.isClientSide) {
				pLevel.setBlock(pPos, pState.setValue(HATCHING, true), Block.UPDATE_ALL);
				return InteractionResult.CONSUME;
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public void attack(BlockState state, Level level, BlockPos at, Player pPlayer) {
		if (level.getBlockEntity(at) instanceof DMREggBlockEntity e && Objects.equals(e.getBreedId(), "end") && !state.getValue(HATCHING)) {
			teleport(state, level, at); // retain original dragon egg teleport behavior
		}
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
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		// Original logic trashes BlockEntity data. We need it, so do it ourselves.
		if (isFree(pLevel.getBlockState(pPos.below())) && pPos.getY() >= pLevel.getMinBuildHeight()) {
			CompoundTag tag = null;
			if (pLevel.getBlockEntity(pPos) instanceof DMREggBlockEntity e) tag = e.saveWithoutMetadata(pLevel.registryAccess());

			var entity = FallingBlockEntity.fall(pLevel, pPos, pState); // this deletes the block. We need to cache the data first and then apply it.
			if (tag != null) entity.blockData = tag;
			falling(entity);
		}
	}

	public static DMREggBlockEntity place(ServerLevel level, BlockPos pos, BlockState state, IDragonBreed breed, Variant variant) {
		level.setBlock(pos, state, Block.UPDATE_ALL);
		var data = (DMREggBlockEntity) level.getBlockEntity(pos);

		if (breed == null) {
			throw new IllegalArgumentException("Breed cannot be null");
		}

		data.setBreed(breed);
		data.setVariantId(variant != null ? variant.id() : null);
		data.setHatchTime(breed.getHatchTime());
		return data;
	}

	@Override
	public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource random) {
		if (pState.getValue(HATCHING) && pLevel.getBlockEntity(pPos) instanceof DMREggBlockEntity e) {
			for (int i = 0; i < random.nextIntBetweenInclusive(4, 7); i++) {
				addHatchingParticles(e.getBreed(), pLevel, pPos, random);
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
		spawnHatchingParticle(level, pos, random, px, py, pz, ox, oy, oz, particle);
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

	static void spawnHatchingParticle(
		Level level,
		BlockPos pos,
		RandomSource random,
		double px,
		double py,
		double pz,
		double ox,
		double oy,
		double oz,
		ParticleOptions particle
	) {
		if (particle.getType() == ParticleTypes.DUST) {
			py = pos.getY() + (random.nextDouble() - 0.5) + 1;
		} else if (particle.getType() == ParticleTypes.PORTAL) {
			ox = (random.nextDouble() - 0.5) * 2;
			oy = (random.nextDouble() - 0.5) * 2;
			oz = (random.nextDouble() - 0.5) * 2;
		}

		level.addParticle(particle, px, py, pz, ox, oy, oz);
	}
}
