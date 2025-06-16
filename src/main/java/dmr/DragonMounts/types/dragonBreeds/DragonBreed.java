package dmr.DragonMounts.types.dragonBreeds;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.DragonBreathRegistry;
import dmr.DragonMounts.types.DatapackEntry;
import dmr.DragonMounts.types.LootTableEntry;
import dmr.DragonMounts.types.LootTableProvider;
import dmr.DragonMounts.types.abilities.DragonAbilityEntry;
import dmr.DragonMounts.types.breath.DragonBreathType;
import dmr.DragonMounts.types.habitats.Habitat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

/**
 * Represents a dragon breed with specific attributes, abilities, and characteristics.
 * This class provides serialization support for loading breed data from JSON files.
 */
@Getter
public class DragonBreed extends DatapackEntry implements LootTableProvider {
    private <T> T getValueOrDefault(T value, T defaultValue) {
        return value != null && (!(value instanceof Number) || ((Number) value).doubleValue() > 0)
                ? value
                : defaultValue;
    }

    @SerializedName("ambient_sound")
    private SoundEvent ambientSound;

    @SerializedName("death_loot")
    private ResourceLocation deathLootTable;

    @SerializedName("hatch_time")
    private int hatchTime = -1;

    public int getHatchTime() {
        return getValueOrDefault(hatchTime, ServerConfig.HATCH_TIME_CONFIG.intValue());
    }

    @SerializedName("growth_time")
    private int growthTime = -1;

    public int getGrowthTime() {
        return getValueOrDefault(growthTime, (int) (ServerConfig.GROWTH_TIME_CONFIG * 20));
    }

    @SerializedName("size_modifier")
    private float sizeModifier = -1;

    public float getSizeModifier() {
        return getValueOrDefault(sizeModifier, (float) ServerConfig.SIZE_MODIFIER);
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
    private ResourceLocation inventoryTexture = ResourceLocation.parse("textures/block/stone.png");

    @SerializedName("breath_type")
    private String breathType;

    public DragonBreathType getBreathType() {
        return DragonBreathRegistry.getBreathType(breathType);
    }

    @SerializedName("immunities")
    private List<String> immunities = new ArrayList<>();

    @SerializedName("attributes")
    private Map<ResourceLocation, Double> attributes = new HashMap<>();

    @SerializedName("habitats")
    private List<Habitat> habitats = new ArrayList<>();

    @SerializedName("dragon_abilities")
    private List<DragonAbilityEntry> abilities = new ArrayList<>();

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

    public Component getName() {
        return Component.translatable(DMR.MOD_ID + ".dragon_breed." + getId());
    }

    public ResourceLocation getResourceLocation() {
        return DMR.id(getId());
    }

    @SerializedName("model_location")
    private ResourceLocation dragonModelLocation = DMR.id("geo/dragon.geo.json");

    @SerializedName("animation_location")
    private ResourceLocation dragonAnimationLocation = DMR.id("animations/dragon.animation.json");

    @SerializedName("armor_type")
    private String armorTypeId = "default";

    @SerializedName("variants")
    private List<DragonVariant> variants = new ArrayList<>();
}
