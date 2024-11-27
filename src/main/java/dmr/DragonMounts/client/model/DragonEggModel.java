package dmr.DragonMounts.client.model;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.server.blockentities.DMREggBlockEntity;
import dmr.DragonMounts.types.dragonBreeds.DragonHybridBreed;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

/**
 * A dynamic BakedModel which returns quads based on the given breed of the tile entity.
 */
public class DragonEggModel implements IUnbakedGeometry<DragonEggModel> {

	private final ImmutableMap<String, BlockModel> models;

	public DragonEggModel(ImmutableMap<String, BlockModel> models) {
		this.models = models;
	}

	@Override
	public BakedModel bake(
		IGeometryBakingContext context,
		ModelBaker baker,
		Function<Material, TextureAtlasSprite> spriteGetter,
		ModelState modelState,
		ItemOverrides overrides
	) {
		var baked = ImmutableMap.<String, BakedModel>builder();
		for (var entry : models.entrySet()) {
			var unbaked = entry.getValue();
			unbaked.resolveParents(baker::getModel);
			baked.put(entry.getKey(), unbaked.bake(baker, unbaked, spriteGetter, modelState, true));
		}
		return new Baked(baked.build(), overrides);
	}

	private record Data(String breedId) {
		private static final ModelProperty<Data> PROPERTY = new ModelProperty<>();
	}

	public static class Baked implements IDynamicBakedModel {

		public static final Supplier<BakedModel> FALLBACK = Suppliers.memoize(() ->
			Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.DRAGON_EGG.defaultBlockState())
		);

		public final ImmutableMap<String, BakedModel> models;
		private final ItemOverrides overrides;

		public Baked(ImmutableMap<String, BakedModel> models, ItemOverrides overrides) {
			this.models = models;
			this.overrides = new ItemModelResolver(this, overrides);
		}

		@Override
		public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
			var data = extraData.get(Data.PROPERTY);
			if (data != null && models.containsKey(data.breedId)) {
				return models.get(data.breedId()).getQuads(state, side, rand, extraData, renderType);
			}

			return FALLBACK.get().getQuads(state, side, rand, extraData, renderType);
		}

		@Override
		public boolean useAmbientOcclusion() {
			return true;
		}

		@Override
		public boolean isGui3d() {
			return true;
		}

		@Override
		public boolean usesBlockLight() {
			return true;
		}

		@Override
		public boolean isCustomRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleIcon() {
			return FALLBACK.get().getParticleIcon();
		}

		@Override
		public TextureAtlasSprite getParticleIcon(ModelData modelData) {
			var data = modelData.get(Data.PROPERTY);
			if (data != null && models.containsKey(data.breedId)) {
				return models.get(data.breedId()).getParticleIcon(modelData);
			}

			return getParticleIcon();
		}

		@Override
		public ItemOverrides getOverrides() {
			return overrides;
		}

		@Override
		public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
			return FALLBACK.get().applyTransform(transformType, poseStack, applyLeftHandTransform);
		}

		@Override
		public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
			if (level.getBlockEntity(pos) instanceof DMREggBlockEntity e && e.isModelReady()) {
				var breed = e.getBreed() instanceof DragonHybridBreed hybridBreed ? hybridBreed.parent1 : e.getBreed();
				return modelData.derive().with(Data.PROPERTY, new Data(breed.getId())).build();
			}
			return modelData;
		}
	}

	public static class ItemModelResolver extends ItemOverrides {

		private final Baked owner;
		private final ItemOverrides nested;

		public ItemModelResolver(Baked owner, ItemOverrides nested) {
			this.owner = owner;
			this.nested = nested;
		}

		@Override
		public BakedModel resolve(BakedModel original, ItemStack stack, ClientLevel level, LivingEntity entity, int pSeed) {
			var override = nested.resolve(original, stack, level, entity, pSeed);
			if (override != original) return override;

			var breed = stack.getOrDefault(ModComponents.DRAGON_BREED, DragonBreedsRegistry.getDefault().getId());

			if (breed.startsWith("hybrid_")) {
				var breedObject = DragonBreedsRegistry.getDragonBreed(breed);

				if (breedObject instanceof DragonHybridBreed hybridBreed) {
					breed = hybridBreed.parent1.getId();
				}
			}

			var model = owner.models.get(breed);
			if (model != null) return model;

			return original;
		}
	}
}
