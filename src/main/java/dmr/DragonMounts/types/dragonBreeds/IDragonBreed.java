package dmr.DragonMounts.types.dragonBreeds;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import dmr.DragonMounts.types.habitats.Habitat;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.minecraft.core.Holder.Direct;
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
	@Getter
	class LootTableEntry {

		private ResourceLocation table;
		private float chance;
		private int minAmount;
		private int maxAmount;
	}

	default boolean isHybrid() {
		return this instanceof DragonHybridBreed;
	}

	default void initialize(DMRDragonEntity dragon) {
		applyAttributes(dragon);
		for (Ability a : getAbilities()) {
			a.initialize(dragon);
		}

		if (getImmunities().contains("drown")) {
			dragon.setPathfindingMalus(PathType.WATER, 0.0F);
		}
	}

	default void close(DMRDragonEntity dragon) {
		dragon.getAttributes().assignAllValues(new AttributeMap(DMRDragonEntity.createAttributes().build())); // restore default attributes
		for (Ability a : getAbilities()) {
			a.close(dragon);
		}
	}

	default void tick(DMRDragonEntity dragon) {
		for (Ability a : getAbilities()) {
			a.tick(dragon);
		}
	}

	default void onMove(DMRDragonEntity dragon) {
		for (Ability a : getAbilities()) {
			a.onMove(dragon);
		}
	}

	default void applyAttributes(DMRDragonEntity dragon) {
		float healthPercentile = dragon.getHealth() / dragon.getMaxHealth();

		getAttributes()
			.forEach((att, value) -> {
				Attribute attr = BuiltInRegistries.ATTRIBUTE.get(att);
				if (attr != null) {
					AttributeInstance inst = dragon.getAttribute(new Direct<>(attr));
					if (inst != null) inst.setBaseValue(value);
				}
			});

		for (Ability ability : getAbilities()) {
			if (ability.getAttributes() != null) {
				ability
					.getAttributes()
					.forEach((att, value) -> {
						Attribute attr = BuiltInRegistries.ATTRIBUTE.get(att);
						if (attr != null) {
							AttributeInstance inst = dragon.getAttribute(new Direct<>(attr));
							if (inst != null) {
								inst.addPermanentModifier(
									new AttributeModifier(
										ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID, ability.type()),
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

	int getPrimaryColor();
	int getSecondaryColor();

	String getId();
	void setId(String id);

	ResourceLocation getDeathLootTable();
	SoundEvent getAmbientSound();

	int getHatchTime();
	int getGrowthTime();
	float getSizeModifier();
	float getVerticalRidingOffset();

	List<String> getImmunities();
	Map<ResourceLocation, Double> getAttributes();
	List<Habitat> getHabitats();
	List<Ability> getAbilities();

	List<Item> getTamingItems();
	List<Item> getBreedingItems();

	ParticleOptions getHatchParticles();

	List<String> getAccessories();

	List<LootTableEntry> getLootTable();
}
