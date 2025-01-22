package dmr.DragonMounts.types.dragonBreeds;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModComponents;
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

	@SerializedName("primary_color")
	private String primary_color;

	@SerializedName("secondary_color")
	private String secondary_color;

	public int getPrimaryColor() {
		return primary_color == null ? 0 : Integer.parseInt(primary_color, 16);
	}

	public int getSecondaryColor() {
		return secondary_color == null ? 0 : Integer.parseInt(secondary_color, 16);
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
	private List<String> abilities = new ArrayList<>();

	@Override
	public List<String> getAbilities() {
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

	@SerializedName("accessories")
	private List<String> modelAccessories = new ArrayList<>();

	@Override
	public List<String> getAccessories() {
		return new ArrayList<>(modelAccessories);
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

	@SerializedName("armor_type")
	private String armorTypeId = "default";

	@Override
	public String getArmorTypeId() {
		return armorTypeId;
	}

	@SerializedName("variants")
	private Variant[] variants;

	@Override
	public List<Variant> getVariants() {
		return variants != null ? List.of(variants) : List.of();
	}

	public static IDragonBreed getDragonType(ItemStack stack) {
		var breedId = stack.get(ModComponents.DRAGON_BREED);
		return DragonBreedsRegistry.getDragonBreed(breedId);
	}

	public static Variant getDragonTypeVariant(ItemStack stack) {
		var breed = getDragonType(stack);
		var variantId = stack.get(ModComponents.DRAGON_VARIANT);
		if (breed != null) {
			return breed.getVariants().stream().filter(variant -> variant.id().equals(variantId)).findFirst().orElse(null);
		}
		return null;
	}

	public static void setDragonType(ItemStack stack, IDragonBreed type) {
		if (type == null) return;
		stack.set(ModComponents.DRAGON_BREED, type.getId());
	}

	public static void setDragonTypeVariant(ItemStack stack, IDragonBreed type, Variant variant) {
		setDragonType(stack, type);
		stack.set(ModComponents.DRAGON_VARIANT, variant.id());
	}

	@Override
	public String toString() {
		return "DragonBreed{" + "id='" + id + '\'' + '}';
	}
}
