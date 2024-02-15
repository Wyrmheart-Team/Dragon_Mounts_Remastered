package dmr.DragonMounts.client.model;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import dmr.DragonMounts.DMRConstants.NBTConstants;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.types.dragonBreeds.DragonHybridBreed;
import dmr.DragonMounts.server.blockentities.DragonEggBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A dynamic BakedModel which returns quads based on the given breed of the tile entity.
 */
public class DragonEggModel implements IUnbakedGeometry<DragonEggModel>
{
	private final ImmutableMap<String, BlockModel> models;
	
	public DragonEggModel(ImmutableMap<String, BlockModel> models)
	{
		this.models = models;
	}
	
	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation)
	{
		var baked = ImmutableMap.<String, BakedModel>builder();
		for (var entry : models.entrySet())
		{
			var unbaked = entry.getValue();
			unbaked.resolveParents(baker::getModel);
			baked.put(entry.getKey(), unbaked.bake(baker, unbaked, spriteGetter, modelState, modelLocation, true));
		}
		return new Baked(baked.build(), overrides);
	}
	
	private record Data(String breedId)
	{
		private static final ModelProperty<Data> PROPERTY = new ModelProperty<>();
	}
	
	public static class Baked implements IDynamicBakedModel
	{
		private static final Supplier<BakedModel> FALLBACK = Suppliers.memoize(() -> Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.DRAGON_EGG.defaultBlockState()));
		
		private final ImmutableMap<String, BakedModel> models;
		private final ItemOverrides overrides;
		
		public Baked(ImmutableMap<String, BakedModel> models, ItemOverrides overrides)
		{
			this.models = models;
			this.overrides = new ItemModelResolver(this, overrides);
		}
		
		@Override
		public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType)
		{
			var data = extraData.get(Data.PROPERTY);
			if (data != null && models.containsKey(data.breedId))
				return models.get(data.breedId()).getQuads(state, side, rand, extraData, renderType);
			
			return FALLBACK.get().getQuads(state, side, rand, extraData, renderType);
		}
		
		@Override
		public boolean useAmbientOcclusion()
		{
			return true;
		}
		
		@Override
		public boolean isGui3d()
		{
			return true;
		}
		
		@Override
		public boolean usesBlockLight()
		{
			return true;
		}
		
		@Override
		public boolean isCustomRenderer()
		{
			return false;
		}
		
		@Override
		public TextureAtlasSprite getParticleIcon()
		{
			return FALLBACK.get().getParticleIcon();
		}
		
		@Override
		public TextureAtlasSprite getParticleIcon(@NotNull ModelData modelData)
		{
			var data = modelData.get(Data.PROPERTY);
			if (data != null && models.containsKey(data.breedId))
				return models.get(data.breedId()).getParticleIcon(modelData);
			
			return getParticleIcon();
		}
		
		@Override
		public ItemOverrides getOverrides()
		{
			return overrides;
		}
		
		@Override
		public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform)
		{
			return FALLBACK.get().applyTransform(transformType, poseStack, applyLeftHandTransform);
		}
		
		@Override
		public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData)
		{
			if (level.getBlockEntity(pos) instanceof DragonEggBlockEntity e && e.isModelReady()) {
				var breed = e.getBreed() instanceof DragonHybridBreed hybridBreed ? hybridBreed.parent1 : e.getBreed();
				return modelData.derive().with(Data.PROPERTY, new Data(breed.getId())).build();
			}
			return modelData;
		}
	}
	
	public static class ItemModelResolver extends ItemOverrides
	{
		private final Baked owner;
		private final ItemOverrides nested;
		
		public ItemModelResolver(Baked owner, ItemOverrides nested)
		{
			this.owner = owner;
			this.nested = nested;
		}
		
		@Nullable
		@Override
		public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int pSeed)
		{
			var override = nested.resolve(original, stack, level, entity, pSeed);
			if (override != original) return override;
			
			var tag = stack.getTag();
			if (tag != null)
			{
				var breed = tag.getString(NBTConstants.BREED);
				
				if(breed.startsWith("hybrid_")){
					var breedObject = DragonBreedsRegistry.getDragonBreed(breed);
					
					if(breedObject instanceof DragonHybridBreed hybridBreed){
						breed = hybridBreed.parent1.getId();
					}
				}
				
				var model = owner.models.get(breed);
				if (model != null) return model;
			}
			
			return original;
		}
	}
}
