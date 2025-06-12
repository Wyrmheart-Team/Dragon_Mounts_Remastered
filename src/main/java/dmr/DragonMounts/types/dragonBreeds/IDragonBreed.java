package dmr.DragonMounts.types.dragonBreeds;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import dmr.DragonMounts.types.breath.DragonBreathType;
import dmr.DragonMounts.types.habitats.Habitat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.pathfinder.PathType;

public interface IDragonBreed {
    record LootTableEntry(
            @SerializedName("table") ResourceLocation table,
            @SerializedName("chance") float chance,
            @SerializedName("min") int minAmount,
            @SerializedName("max") int maxAmount) {}

    record Variant(
            @SerializedName("id") String id,
            @SerializedName("texture") ResourceLocation skinTexture,
            @SerializedName("saddle_texture") ResourceLocation saddleTexture,
            @SerializedName("glow_texture") ResourceLocation glowTexture,
            @SerializedName("egg_texture") ResourceLocation eggTexture,
            @SerializedName("breath_type") DragonBreathType breathType,
            @SerializedName("primary_color") String primaryColor,
            @SerializedName("secondary_color") String secondaryColor,
            @SerializedName("size_modifier") float sizeModifier) {
        public int getPrimaryColor() {
            return primaryColor == null ? 0 : Integer.parseInt(primaryColor, 16);
        }

        public int getSecondaryColor() {
            return secondaryColor == null ? 0 : Integer.parseInt(secondaryColor, 16);
        }
    }

    default boolean isHybrid() {
        return this instanceof DragonHybridBreed;
    }

    default void initialize(TameableDragonEntity dragon) {
        applyAttributes(dragon);
        for (Ability a : getAbilities()) {
            a.initialize(dragon);
        }

        if (getImmunities().contains("drown")) {
            dragon.setPathfindingMalus(PathType.WATER, 0.0F);
        }
    }

    default void close(TameableDragonEntity dragon) {
        dragon.getAttributes()
                .assignAllValues(
                        new AttributeMap(TameableDragonEntity.createAttributes().build())); // restore
        // default
        // attributes
        for (Ability a : getAbilities()) {
            a.close(dragon);
        }
    }

    default void tick(TameableDragonEntity dragon) {
        for (Ability a : getAbilities()) {
            a.tick(dragon);
        }
    }

    default void onMove(TameableDragonEntity dragon) {
        for (Ability a : getAbilities()) {
            a.onMove(dragon);
        }
    }

    default void applyAttributes(TameableDragonEntity dragon) {
        float healthPercentile = dragon.getHealth() / dragon.getMaxHealth();

        getAttributes().forEach((att, value) -> {
            Optional<Reference<Attribute>> attr = BuiltInRegistries.ATTRIBUTE.getHolder(att);
            if (attr.isPresent()) {
                AttributeInstance inst = dragon.getAttribute(attr.get());
                if (inst != null) {
                    inst.setBaseValue(value);
                }
            }
        });

        for (Ability ability : getAbilities()) {
            if (ability.getAttributes() != null) {
                ability.getAttributes().forEach((att, value) -> {
                    Optional<Reference<Attribute>> attr = BuiltInRegistries.ATTRIBUTE.getHolder(att);
                    if (attr.isPresent()) {
                        AttributeInstance inst = dragon.getAttribute(attr.get());
                        if (inst != null) {
                            inst.addPermanentModifier(new AttributeModifier(
                                    ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, ability.type()),
                                    value,
                                    Operation.ADD_VALUE));
                        }
                    }
                });
            }
        }

        dragon.setHealth(dragon.getMaxHealth() * healthPercentile); // in case we have less than max health
    }

    Component getName();

    ResourceLocation getResourceLocation();

    ResourceLocation getDragonModelLocation();

    ResourceLocation getDragonAnimationLocation();

    ResourceLocation getInventoryTexture();

    String getArmorTypeId();

    int getPrimaryColor();

    int getSecondaryColor();

    DragonBreathType getBreathType();

    String getId();

    void setId(String id);

    ResourceLocation getDeathLootTable();

    SoundEvent getAmbientSound();

    int getHatchTime();

    int getGrowthTime();

    float getSizeModifier();

    List<String> getImmunities();

    Map<ResourceLocation, Double> getAttributes();

    List<Habitat> getHabitats();

    List<Ability> getAbilities();

    List<Item> getTamingItems();

    List<Item> getBreedingItems();

    ParticleOptions getHatchParticles();

    List<String> getAccessories();

    List<LootTableEntry> getLootTable();

    List<Variant> getVariants();
}
