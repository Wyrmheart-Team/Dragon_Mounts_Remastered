package dmr.DragonMounts.types.dragonBreeds;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.types.abilities.types.Ability;
import dmr.DragonMounts.types.habitats.Habitat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DragonBreed implements IDragonBreed {

	private String id;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@SerializedName("ambient_sound")
	private SoundEvent ambientSound;

	@Override
	public SoundEvent getAmbientSound() {
		return ambientSound;
	}

	@SerializedName("death_loot")
	private ResourceLocation deathLoot;

	@Override
	public ResourceLocation getDeathLootTable() {
		return deathLoot;
	}

	@SerializedName("hatch_time")
	private int hatchTime = -1;

	@Override
	public int getHatchTime() {
		if (hatchTime <= 0) {
			return ServerConfig.HATCH_TIME_CONFIG.get();
		} else return hatchTime;
	}

	@SerializedName("growth_time")
	private int growthTime = -1;

	@Override
	public int getGrowthTime() {
		if (growthTime <= 0) {
			return ServerConfig.GROWTH_TIME_CONFIG.get() * 20;
		} else return growthTime;
	}

	@SerializedName("size_modifier")
	private float sizeModifier = -1;

	@Override
	public float getSizeModifier() {
		if (sizeModifier <= 0) {
			return ServerConfig.SIZE_MODIFIER.get().floatValue();
		} else {
			return sizeModifier;
		}
	}

	@SerializedName("riding_offset")
	private float riding_offset = 0;

	@Override
	public float getVerticalRidingOffset() {
		return riding_offset;
	}

	@SerializedName("primary_color")
	private String primary_color;

	@SerializedName("secondary_color")
	private String secondary_color;

	public int getPrimaryColor() {
		return Integer.parseInt(primary_color, 16);
	}

	public int getSecondaryColor() {
		return Integer.parseInt(secondary_color, 16);
	}

	@SerializedName("immunities")
	private List<String> immunities = new ArrayList<>();

	@Override
	public List<String> getImmunities() {
		return immunities;
	}

	@SerializedName("attributes")
	private Map<ResourceLocation, Double> attributes = new HashMap<>();

	@Override
	public Map<ResourceLocation, Double> getAttributes() {
		return attributes;
	}

	@SerializedName("habitats")
	private List<Habitat> habitats = new ArrayList<>();

	@Override
	public List<Habitat> getHabitats() {
		return habitats;
	}

	@SerializedName("abilities")
	private List<Ability> abilities = new ArrayList<>();

	@Override
	public List<Ability> getAbilities() {
		return abilities;
	}

	@SerializedName("taming_items")
	private List<Item> tamingItems = new ArrayList<>();

	@Override
	public List<Item> getTamingItems() {
		return tamingItems;
	}

	@SerializedName("breeding_items")
	private List<Item> breedingItems = new ArrayList<>();

	@Override
	public List<Item> getBreedingItems() {
		return breedingItems;
	}

	@SerializedName("hatch_particles")
	private ParticleOptions hatchParticles;

	@Override
	public ParticleOptions getHatchParticles() {
		return hatchParticles;
	}

	//This is to keep backwards compatibility with DML
	@SerializedName("model_properties")
	private Map<String, Boolean> oldModelProperties = new HashMap<>();

	@SerializedName("accessories")
	private List<String> modelAccessories = new ArrayList<>();

	@Override
	public List<String> getAccessories() {
		ArrayList<String> list = new ArrayList<>();
		list.addAll(modelAccessories);
		list.addAll(oldModelProperties.keySet());
		return list;
	}

	@SerializedName("loot_tables")
	private List<LootTableEntry> lootTable = new ArrayList<>();

	@Override
	public List<LootTableEntry> getLootTable() {
		return lootTable;
	}

	@Override
	public Component getName() {
		return Component.translatable(DMR.MOD_ID + ".dragon_breed." + getId());
	}

	public ResourceLocation getResourceLocation() {
		return DMR.id(getId());
	}

	@SerializedName("model_location")
	private ResourceLocation modelLocation;

	@Override
	public ResourceLocation getDragonModelLocation() {
		return modelLocation;
	}

	@SerializedName("animation_location")
	private ResourceLocation animationLocation;

	@Override
	public ResourceLocation getDragonAnimationLocation() {
		return animationLocation;
	}

	public static IDragonBreed getDragonType(ItemStack stack) {
		var breedId = stack.get(ModComponents.DRAGON_BREED);
		return DragonBreedsRegistry.getDragonBreed(breedId);
	}

	public static void setDragonType(ItemStack stack, IDragonBreed type) {
		if (type == null) return;
		stack.set(ModComponents.DRAGON_BREED, type.getId());
	}
}
