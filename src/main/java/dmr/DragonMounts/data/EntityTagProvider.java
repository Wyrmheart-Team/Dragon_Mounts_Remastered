package dmr.DragonMounts.data;

import dmr.DragonMounts.DMR;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class EntityTagProvider extends EntityTypeTagsProvider {

	public static final TagKey<EntityType<?>> DRAGON_HUNTING_TARGET = create(DMR.id("dragon_hunting_target"));
	public static final TagKey<EntityType<?>> WILD_DRAGON_HUNTING_TARGET = create(DMR.id("wild_dragon_hunting_target"));

	public static TagKey<EntityType<?>> create(ResourceLocation name) {
		return TagKey.create(Registries.ENTITY_TYPE, name);
	}

	public EntityTagProvider(
		PackOutput output,
		CompletableFuture<Provider> lookupProvider,
		String modId,
		ExistingFileHelper existingFileHelper
	) {
		super(output, lookupProvider, modId, existingFileHelper);
	}

	@Override
	protected void addTags(Provider pProvider) {
		tag(DRAGON_HUNTING_TARGET).addTags(EntityTypeTags.UNDEAD, EntityTypeTags.ARTHROPOD, EntityTypeTags.RAIDERS).remove(EntityType.BEE);
		tag(WILD_DRAGON_HUNTING_TARGET)
			.addTags(EntityTypeTags.UNDEAD, EntityTypeTags.ARTHROPOD, EntityTypeTags.RAIDERS)
			.remove(EntityType.BEE)
			.add(
				EntityType.COW,
				EntityType.SHEEP,
				EntityType.PIG,
				EntityType.CHICKEN,
				EntityType.RABBIT,
				EntityType.POLAR_BEAR,
				EntityType.PUFFERFISH,
				EntityType.COD,
				EntityType.SALMON,
				EntityType.TROPICAL_FISH,
				EntityType.SQUID,
				EntityType.BAT
			);
	}
}
