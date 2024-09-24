package dmr.DragonMounts.client.model;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import dmr.DragonMounts.DMRConstants.NBTConstants;
import dmr.DragonMounts.DragonMountsRemaster;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class DragonArmorItemModel
{
	public static class DragonArmorLoader implements IGeometryLoader<DragonArmorGeometry>{
		
		@Override
		public DragonArmorGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializer) throws JsonParseException
		{
			var models = ImmutableMap.<String, BlockModel>builder();
			var dir = "models/item/dragon_armor";
			var length = "models/".length();
			var suffixLength = ".json".length();
			for (var entry : Minecraft.getInstance().getResourceManager().listResources(dir, f -> f.getPath().endsWith(".json")).entrySet())
			{
				var rl = entry.getKey();
				var path = rl.getPath();
				path = path.substring(length, path.length() - suffixLength);
				var id = String.format("%s", path.substring("item/dragon_armor/".length(), path.length() - "_dragon_armor".length()));
				
				try (var reader = entry.getValue().openAsReader())
				{
					models.put(id, BlockModel.fromStream(reader));
				}
				catch (IOException e)
				{
					throw new JsonParseException(e);
				}
			}
			
			return new DragonArmorGeometry(models.build());
		}
	}
	
	static class DragonArmorGeometry implements IUnbakedGeometry<DragonArmorGeometry>{
		private final ImmutableMap<String, BlockModel> models;
		
		public DragonArmorGeometry(ImmutableMap<String, BlockModel> models)
		{
			this.models = models;
		}
		
		@Override
		public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides)
		{
			var baked = ImmutableMap.<String, BakedModel>builder();
			for (var entry : models.entrySet())
			{
				var unbaked = entry.getValue();
				unbaked.resolveParents(baker::getModel);
				baked.put(entry.getKey(), unbaked.bake(baker, unbaked, spriteGetter, modelState, true));
			}
			return new Baked(baked.build(), overrides);
		}
		
	}
	
	private record Data(String armorId)
	{
		private static final ModelProperty<Data> PROPERTY = new ModelProperty<>();
	}
	
	public static class Baked implements IDynamicBakedModel
	{
		private static final Supplier<BakedModel> FALLBACK = Suppliers.memoize(() -> Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(Items.LEATHER_HORSE_ARMOR));
		
		private final ImmutableMap<String, BakedModel> models;
		private final ItemOverrides overrides;
		
		public Baked(ImmutableMap<String, BakedModel> models, ItemOverrides overrides)
		{
			this.models = models;
			this.overrides = new ItemModelResolver(this, overrides);
		}
		
		@Override
		public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType renderType)
		{
			var data = extraData.get(Data.PROPERTY);
			if (data != null && models.containsKey(data.armorId))
				return models.get(data.armorId()).getQuads(state, side, rand, extraData, renderType);
			
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
		public TextureAtlasSprite getParticleIcon(ModelData modelData)
		{
			var data = modelData.get(Data.PROPERTY);
			if (data != null && models.containsKey(data.armorId))
				return models.get(data.armorId()).getParticleIcon(modelData);
			
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
		public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData)
		{
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
		
		
		@Override
		public BakedModel resolve(BakedModel original, ItemStack stack, ClientLevel level, LivingEntity entity, int pSeed)
		{
			var override = nested.resolve(original, stack, level, entity, pSeed);
			if (override != original) return override;
			
			if(stack.has(DataComponents.CUSTOM_DATA)){
				var tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
				var armor = tag.getString(NBTConstants.ARMOR);
				var model = owner.models.get(armor);
				if (model != null) return model;
			}
			
			return original;
		}
	}
	
	@EventBusSubscriber( modid = DragonMountsRemaster.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
	public static class ClientEvents
	{
		@SubscribeEvent
		public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event)
		{
			event.register(ResourceLocation.fromNamespaceAndPath(DragonMountsRemaster.MOD_ID, "dragon_armor"), new DragonArmorLoader());
		}
	}
}
