package dmr.DragonMounts.registry;

import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.types.dragonBreeds.DragonHybridBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class DragonBreedsRegistry
{
	private static final HashMap<String, IDragonBreed> DRAGON_TYPES = new HashMap<>();
	
	public static void register(IDragonBreed breed)
	{
		DRAGON_TYPES.put(breed.getId(), breed);
	}
	
	public static void registerHybrids()
	{
		//Clear all current hybrids
		var list = DRAGON_TYPES.entrySet().stream().filter(ent -> ent.getValue() instanceof DragonHybridBreed).map(Entry::getKey);
		list.forEach(DRAGON_TYPES::remove);
		var breeds = getDragonBreeds();
		
		//Register all hybrids based on current breeds
		for (IDragonBreed dragonBreed1 : breeds) {
			for (IDragonBreed dragonBreed2 : breeds) {
				if (dragonBreed1 == dragonBreed2) continue;
				registerHybrid(dragonBreed1, dragonBreed2);
			}
		}
	}
	
	public static void registerHybrid(IDragonBreed parent1, IDragonBreed parent2)
	{
		register(new DragonHybridBreed(parent1, parent2));
	}
	
	public static void setBreeds(List<IDragonBreed> breeds)
	{
		DRAGON_TYPES.clear();
		for (IDragonBreed breed : breeds) {
			register(breed);
		}
	}
	
	public static IDragonBreed getDragonBreed(String name)
	{
		var val = DRAGON_TYPES.getOrDefault(name, null);
		return val == null ? getDefault() : val;
	}
	
	public static IDragonBreed getHybridBreed(IDragonBreed breed1, IDragonBreed breed2)
	{
		return getDragonBreed("hybrid_" + breed1.getId() + "_" + breed2.getId());
	}
	
	public static boolean hasDragonBreed(String name)
	{
		return DRAGON_TYPES.containsKey(name);
	}
	
	public static List<IDragonBreed> getDragonBreeds()
	{
		return new ArrayList<>(DRAGON_TYPES.values());
	}
	
	public static IDragonBreed getFirst()
	{
		return getDragonBreeds().stream().findFirst().orElse(new DragonBreed());
	}
	
	public static IDragonBreed getDefault()
	{
		return hasDragonBreed("end") ? getDragonBreed("end") : getFirst();
	}
}
