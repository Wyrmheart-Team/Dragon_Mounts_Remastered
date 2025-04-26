package dmr.DragonMounts.registry;

import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.types.dragonBreeds.DragonHybridBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import dmr.DragonMounts.util.BreedingUtils;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class DragonBreedsRegistry {

	private static final HashMap<String, IDragonBreed> DRAGON_TYPES = new HashMap<>();

	public static void register(IDragonBreed breed) {
		DRAGON_TYPES.put(breed.getId(), breed);
	}

	public static void registerHybrids() {
		//Clear all current hybrids
		var list = DRAGON_TYPES.entrySet().stream().filter(ent -> ent.getValue() instanceof DragonHybridBreed).map(Entry::getKey);
		list.forEach(DRAGON_TYPES::remove);
		var breeds = getDragonBreeds();

		//Register all hybrids based on current breeds
		for (IDragonBreed dragonBreed1 : breeds) {
			for (IDragonBreed dragonBreed2 : breeds) {
				if (dragonBreed1 == dragonBreed2) continue;
				if (dragonBreed1.getDragonModelLocation() != dragonBreed2.getDragonModelLocation()) continue;

				registerHybrid(dragonBreed1, dragonBreed2);
			}
		}
	}

	public static void registerHybrid(IDragonBreed parent1, IDragonBreed parent2) {
		register(new DragonHybridBreed(parent1, parent2));
	}

	public static void setBreeds(List<IDragonBreed> breeds) {
		DRAGON_TYPES.clear();
		for (IDragonBreed breed : breeds) {
			register(breed);
		}
	}

	public static IDragonBreed getDragonBreed(String name) {
		var val = DRAGON_TYPES.getOrDefault(name, null);
		return val == null ? getDefault() : val;
	}

	public static IDragonBreed getHybridBreed(IDragonBreed breed1, IDragonBreed breed2) {
		return getDragonBreed("hybrid_" + breed1.getId() + "_" + breed2.getId());
	}

	public static boolean hasDragonBreed(String name) {
		return DRAGON_TYPES.containsKey(name);
	}

	public static List<IDragonBreed> getDragonBreeds() {
		return new ArrayList<>(DRAGON_TYPES.values());
	}

	public static IDragonBreed getFirst() {
		return getDragonBreeds().stream().findFirst().orElse(new DragonBreed());
	}

	public static IDragonBreed getDefault() {
		return hasDragonBreed("end") ? getDragonBreed("end") : getFirst();
	}

	public static ArrayList<IDragonBreed> getEggOutcomes(DMRDragonEntity dmrDragonEntity, ServerLevel level, DMRDragonEntity mate) {
		var eggOutcomes = new ArrayList<IDragonBreed>();

		eggOutcomes.addAll(getBreeds(dmrDragonEntity));
		eggOutcomes.addAll(getBreeds(mate));

		if (ServerConfig.HABITAT_OFFSPRING.get()) {
			IDragonBreed highestBreed1 = BreedingUtils.getHabitatBreedOutcome(level, dmrDragonEntity.blockPosition());
			IDragonBreed highestBreed2 = BreedingUtils.getHabitatBreedOutcome(level, mate.blockPosition());

			if (highestBreed1 != null) {
				if (!eggOutcomes.contains(highestBreed1)) eggOutcomes.add(highestBreed1);
			}

			if (highestBreed2 != null) {
				if (!eggOutcomes.contains(highestBreed2)) eggOutcomes.add(highestBreed2);
			}
		}

		if (ServerConfig.ALLOW_HYBRIDIZATION.get()) {
			var newList = new ArrayList<IDragonBreed>();

			for (IDragonBreed breed1 : eggOutcomes) {
				for (IDragonBreed breed2 : eggOutcomes) {
					if (breed1 != breed2) {
						var hybrid = getHybridBreed(breed1, breed2);
						if (hybrid != null) {
							newList.add(hybrid);
						}
					}
				}
			}
			eggOutcomes.addAll(newList);
		}

		return eggOutcomes;
	}

	public static List<IDragonBreed> getBreeds(DMRDragonEntity dmrDragonEntity) {
		List<IDragonBreed> breeds = new ArrayList<>();
		if (dmrDragonEntity.getBreed() instanceof DragonHybridBreed hybridBreed) {
			breeds.add(hybridBreed.parent1);
			breeds.add(hybridBreed.parent2);
			return breeds;
		}
		breeds.add(dmrDragonEntity.getBreed());
		return breeds;
	}
}
