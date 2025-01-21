package dmr.DragonMounts.types.dragonBreeds;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.abilities.Ability;
import dmr.DragonMounts.registry.DragonAbilityRegistry;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.habitats.Habitat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
		@SerializedName("max") int maxAmount
	) {}

	record Variant(
		@SerializedName("id") String id,
		@SerializedName("texture") ResourceLocation skinTexture,
		@SerializedName("saddle_texture") ResourceLocation saddleTexture,
		@SerializedName("glow_texture") ResourceLocation glowTexture,
		@SerializedName("egg_texture") ResourceLocation eggTexture,
		@SerializedName("primary_color") String primaryColor,
		@SerializedName("secondary_color") String secondaryColor,
		@SerializedName("size_modifier") float sizeModifier
	) {
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

	default void initialize(DMRDragonEntity dragon) {
		applyAttributes(dragon);

		for (Ability a : getCodeAbilities()) {
			a.initialize(dragon);
		}

		for (String ability : getAbilities()) {
			DragonAbilityRegistry.callScript(ability, "init", dragon);
		}

		if (getImmunities().contains("drown")) {
			dragon.setPathfindingMalus(PathType.WATER, 0.0F);
		}
	}

	default void close(DMRDragonEntity dragon) {
		dragon.getAttributes().assignAllValues(new AttributeMap(DMRDragonEntity.createAttributes().build())); // restore default attributes

		for (Ability a : getCodeAbilities()) {
			a.close(dragon);
		}

		for (String ability : getAbilities()) {
			DragonAbilityRegistry.callScript(ability, "close", dragon);
		}
	}

	default void tick(DMRDragonEntity dragon) {
		for (Ability a : getCodeAbilities()) {
			a.tick(dragon);
		}

		for (String ability : getAbilities()) {
			DragonAbilityRegistry.callScript(ability, "onTick", dragon);
		}
	}

	default void onMove(DMRDragonEntity dragon) {
		for (Ability a : getCodeAbilities()) {
			a.onMove(dragon);
		}

		for (String ability : getAbilities()) {
			DragonAbilityRegistry.callScript(ability, "onMove", dragon);
		}
	}

	default void applyAttributes(DMRDragonEntity dragon) {
		float healthPercentile = dragon.getHealth() / dragon.getMaxHealth();

		getAttributes()
			.forEach((att, value) -> {
				Optional<Reference<Attribute>> attr = BuiltInRegistries.ATTRIBUTE.getHolder(att);
				if (attr.isPresent()) {
					AttributeInstance inst = dragon.getAttribute(attr.get());
					if (inst != null) {
						inst.setBaseValue(value);
					}
				}
			});

		for (String ability : getAbilities()) {
			if (DragonAbilityRegistry.hasDragonAbility(ability)) {
				DragonAbilityRegistry.getDragonAbility(ability)
					.getAttributes()
					.forEach((att, value) -> {
						Optional<Reference<Attribute>> attr = BuiltInRegistries.ATTRIBUTE.getHolder(
							Objects.requireNonNull(att.getBaseId())
						);
						if (attr.isPresent()) {
							AttributeInstance inst = dragon.getAttribute(attr.get());
							if (inst != null) {
								inst.addPermanentModifier(
									new AttributeModifier(
										ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, ability),
										value,
										Operation.ADD_VALUE
									)
								);
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

	String getArmorTypeId();

	int getPrimaryColor();
	int getSecondaryColor();

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
	List<String> getAbilities();
	List<Ability> getCodeAbilities();

	List<Item> getTamingItems();
	List<Item> getBreedingItems();

	ParticleOptions getHatchParticles();

	List<String> getAccessories();

	List<LootTableEntry> getLootTable();

	List<Variant> getVariants();
}
