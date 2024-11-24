package dmr.DragonMounts.types.dragonBreeds;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.types.abilities.types.Ability;
import dmr.DragonMounts.types.habitats.Habitat;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DragonHybridBreed implements IDragonBreed {
	@SerializedName( "parent1" ) public IDragonBreed parent1;
	
	@SerializedName( "parent1" ) public IDragonBreed parent2;
	
	public DragonHybridBreed(IDragonBreed parent1, IDragonBreed parent2)
	{
		this.parent1 = parent1;
		this.parent2 = parent2;
	}
	
	@Override
	public Component getName()
	{
		return Component.translatable("dragon_breed.hybrid", parent1.getName().getString(), parent2.getName().getString());
	}
	
	@Override
	public ResourceLocation getResourceLocation()
	{
		return parent1.getResourceLocation() != null ? parent1.getResourceLocation() : parent2.getResourceLocation();
	}
	
	@Override
	public ResourceLocation getDragonModelLocation()
	{
		return parent1.getDragonModelLocation() != null ? parent1.getDragonModelLocation() : parent2.getDragonModelLocation();
	}
	
	@Override
	public ResourceLocation getDragonAnimationLocation()
	{
		return parent1.getDragonAnimationLocation() != null ? parent1.getDragonAnimationLocation() : parent2.getDragonAnimationLocation();
	}
	
	@Override
	public int getPrimaryColor()
	{
		return parent1.getPrimaryColor();
	}
	
	@Override
	public int getSecondaryColor()
	{
		return parent2.getPrimaryColor();
	}
	
	@Override
	public String getId()
	{
		return "hybrid_" + parent1.getId() + "_" + parent2.getId();
	}
	
	@Override
	public void setId(String id) {}
	
	@Override
	public ResourceLocation getDeathLootTable()
	{
		return parent1.getDeathLootTable() != null ? parent1.getDeathLootTable() : parent2.getDeathLootTable();
	}
	
	@Override
	public SoundEvent getAmbientSound()
	{
		return parent1.getAmbientSound() != null ? parent1.getAmbientSound() : parent2.getAmbientSound();
	}
	
	@Override
	public int getHatchTime()
	{
		return (parent1.getHatchTime() + parent2.getHatchTime()) / 2;
	}
	
	@Override
	public int getGrowthTime()
	{
		return (parent1.getGrowthTime() + parent2.getGrowthTime()) / 2;
	}
	
	@Override
	public float getVerticalRidingOffset()
	{
		return parent1 != null ? parent1.getVerticalRidingOffset() : parent2.getVerticalRidingOffset();
	}
	
	@Override
	public float getSizeModifier()
	{
		return (parent1.getSizeModifier() + parent2.getSizeModifier()) / 2;
	}
	
	@Override
	public List<String> getImmunities()
	{
		List<String> immunities = new ArrayList<>();
		if (parent1.getImmunities() != null) {
			if (parent1.getImmunities().size() > 1) {
				immunities.addAll(parent1.getImmunities().subList(0, parent1.getImmunities().size() / 2));
			} else {
				immunities.addAll(parent1.getImmunities());
			}
		}
		if (parent2.getImmunities() != null) {
			if (parent2.getImmunities().size() > 1) {
				immunities.addAll(parent2.getImmunities().subList(parent2.getImmunities().size() / 2, parent2.getImmunities().size()));
			} else {
				immunities.addAll(parent2.getImmunities());
			}
		}
		return immunities;
	}
	
	@Override
	public Map<ResourceLocation, Double> getAttributes()
	{
		Map<ResourceLocation, Double> attributes = new HashMap<>();
		if (parent1.getAttributes() != null) attributes.putAll(parent1.getAttributes());
		if (parent2.getAttributes() != null) attributes.putAll(parent2.getAttributes());
		return attributes;
	}
	
	@Override
	public List<Habitat> getHabitats()
	{
		return List.of(); //Habitats wont get used for anything on a hybrid as they are only used for breeding outcomes
	}
	
	@Override
	public List<Ability> getAbilities()
	{
		List<Ability> abilities = new ArrayList<>();
		if (parent1.getAbilities() != null) {
			if (parent1.getAbilities().size() > 1) {
				abilities.addAll(parent1.getAbilities().subList(0, parent1.getAbilities().size() / 2));
			} else {
				abilities.addAll(parent1.getAbilities());
			}
		}
		if (parent2.getAbilities() != null) {
			if (parent2.getAbilities().size() > 1) {
				abilities.addAll(parent2.getAbilities().subList(parent2.getAbilities().size() / 2, parent2.getAbilities().size()));
			} else {
				abilities.addAll(parent2.getAbilities());
			}
		}
		return abilities;
	}
	
	@Override
	public List<Item> getTamingItems()
	{
		List<Item> tamingItems = new ArrayList<>();
		if (parent1.getTamingItems() != null) tamingItems.addAll(parent1.getTamingItems());
		if (parent2.getTamingItems() != null) tamingItems.addAll(parent2.getTamingItems());
		return tamingItems;
	}
	
	@Override
	public List<Item> getBreedingItems()
	{
		List<Item> breedingItems = new ArrayList<>();
		if (parent1.getBreedingItems() != null) breedingItems.addAll(parent1.getBreedingItems());
		if (parent2.getBreedingItems() != null) breedingItems.addAll(parent2.getBreedingItems());
		return breedingItems;
	}
	
	@Override
	public ParticleOptions getHatchParticles()
	{
		return parent2.getHatchParticles() != null ? parent2.getHatchParticles() : parent1.getHatchParticles();
	}
	
	@Override
	public List<String> getAccessories()
	{
		List<String> modelProperties = new ArrayList<>();
		if (parent1.getAccessories() != null) {
			if (parent1.getAccessories().size() > 1) {
				modelProperties.addAll(parent1.getAccessories().subList(0, parent1.getAccessories().size() / 2));
			} else {
				modelProperties.addAll(parent1.getAccessories());
			}
		}
		if (parent2.getAccessories() != null) {
			if (parent2.getAccessories().size() > 1) {
				modelProperties.addAll(parent2.getAccessories().subList(parent2.getAccessories().size() / 2, parent2.getAccessories().size()));
			} else {
				modelProperties.addAll(parent2.getAccessories());
			}
		}
		return modelProperties;
	}
	
	@Override
	public List<LootTableEntry> getLootTable()
	{
		List<LootTableEntry> list = new ArrayList<>();
		
		if (parent1.getLootTable() != null) {
			list.addAll(parent1.getLootTable().subList(0, parent1.getLootTable().size() / 2));
		}
		
		if (parent2.getLootTable() != null) {
			list.addAll(parent2.getLootTable().subList(parent2.getLootTable().size() / 2, parent2.getLootTable().size()));
		}
		
		return list;
	}
}
