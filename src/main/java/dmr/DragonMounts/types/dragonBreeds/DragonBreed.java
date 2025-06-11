package dmr.DragonMounts.types.dragonBreeds;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.DragonBreathRegistry;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.types.abilities.types.Ability;
import dmr.DragonMounts.types.breath.DragonBreathType;
import dmr.DragonMounts.types.habitats.Habitat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a dragon breed with specific attributes, abilities, and characteristics.
 * This class implements the IDragonBreed interface and provides serialization support
 * for loading breed data from JSON files.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(of = {"id"} )
@Getter
public class DragonBreed implements IDragonBreed {
    private <T> T getValueOrDefault(T value, T defaultValue) {
        return value != null && (!(value instanceof Number) || ((Number)value).doubleValue() > 0) ? value : defaultValue;
    }
    
    @Setter
    private String id;
    
    @SerializedName("ambient_sound")
    private SoundEvent ambientSound;
    
    @SerializedName("death_loot")
    private ResourceLocation deathLootTable;
    
    @SerializedName("hatch_time")
    private int hatchTime = -1;
    
    @Override
    public int getHatchTime() {
        return getValueOrDefault(hatchTime, ServerConfig.HATCH_TIME_CONFIG.intValue());
    }

    @SerializedName("growth_time")
    private int growthTime = -1;
    
    @Override
    public int getGrowthTime() {
        return getValueOrDefault(growthTime, (int)(ServerConfig.GROWTH_TIME_CONFIG * 20));
    }

    @SerializedName("size_modifier")
    private float sizeModifier = -1;

    @Override
    public float getSizeModifier() {
        return getValueOrDefault(sizeModifier, (float)ServerConfig.SIZE_MODIFIER);
    }

    @SerializedName("primary_color")
    private String primaryColor;

    @SerializedName("secondary_color")
    private String secondaryColor;

    public int getPrimaryColor() {
        return primaryColor == null ? 0 : Integer.parseInt(primaryColor, 16);
    }

    public int getSecondaryColor() {
        return secondaryColor == null ? 0 : Integer.parseInt(secondaryColor, 16);
    }
    
    @SerializedName("inventory_texture")
    private ResourceLocation inventoryTexture;
    private final ResourceLocation defaultInventoryTexture = ResourceLocation.parse("textures/block/stone.png");
    
    @Override
    public ResourceLocation getInventoryTexture() {
        return inventoryTexture != null ? inventoryTexture : defaultInventoryTexture;
    }
    
    @SerializedName("breath_type")
    private String breathType;

    @Override
    public DragonBreathType getBreathType() {
        return DragonBreathRegistry.getBreathType(breathType);
    }

    @SerializedName("immunities")
    private List<String> immunities = new ArrayList<>();

    @SerializedName("attributes")
    private Map<ResourceLocation, Double> attributes = new HashMap<>();
    
    @SerializedName("habitats")
    private List<Habitat> habitats = new ArrayList<>();
    
    @SerializedName("abilities")
    private List<Ability> abilities = new ArrayList<>();
    
    @SerializedName("taming_items")
    private List<Item> tamingItems = new ArrayList<>();

    @SerializedName("breeding_items")
    private List<Item> breedingItems = new ArrayList<>();

    @SerializedName("hatch_particles")
    private ParticleOptions hatchParticles;
    
    @SerializedName("accessories")
    private List<String> accessories = new ArrayList<>();

    @SerializedName("loot_tables")
    private List<LootTableEntry> lootTable = new ArrayList<>();
    
    @Override
    public Component getName() {
        return Component.translatable(DMR.MOD_ID + ".dragon_breed." + getId());
    }

    public ResourceLocation getResourceLocation() {
        return DMR.id(getId());
    }

    @SerializedName("model_location")
    private ResourceLocation dragonModelLocation;
    
    @SerializedName("animation_location")
    private ResourceLocation dragonAnimationLocation;
    
    @SerializedName("armor_type")
    private String armorTypeId = "default";
    
    @SerializedName("variants")
    private Variant[] variants;
    private List<Variant> cachedVariants = null;
    
    @Override
    public List<Variant> getVariants() {
        if (cachedVariants == null) {
            cachedVariants = variants != null ? List.of(variants) : List.of();
        }
        return List.copyOf(cachedVariants);
    }

    public static IDragonBreed getDragonType(ItemStack stack) {
        var breedId = stack.get(ModComponents.DRAGON_BREED);
        return DragonBreedsRegistry.getDragonBreed(breedId);
    }

    public static Variant getDragonTypeVariant(ItemStack stack) {
        var breed = getDragonType(stack);
        var variantId = stack.get(ModComponents.DRAGON_VARIANT);
        if (breed != null) {
            return breed.getVariants().stream()
                    .filter(variant -> variant.id().equals(variantId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public static void setDragonType(ItemStack stack, IDragonBreed type) {
        if (stack == null || type == null) return;
        stack.set(ModComponents.DRAGON_BREED, type.getId());
    }

    public static void setDragonTypeVariant(ItemStack stack, IDragonBreed type, Variant variant) {
        setDragonType(stack, type);
        stack.set(ModComponents.DRAGON_VARIANT, variant.id());
    }
}
