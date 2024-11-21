package dmr.DragonMounts.registry;

import dmr.DragonMounts.types.armor.DragonArmor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DragonArmorRegistry
{
	private static final HashMap<String, DragonArmor> ARMOR_TYPES = new HashMap<>();
	
	public static void register(DragonArmor armor)
	{
		ARMOR_TYPES.put(armor.getId(), armor);
	}
	
	public static void setArmors(List<DragonArmor> armors)
	{
		ARMOR_TYPES.clear();
		for (DragonArmor armor : armors) {
			register(armor);
		}
	}
	
	public static DragonArmor getDragonArmor(String name)
	{
		var val = ARMOR_TYPES.getOrDefault(name, null);
		return val == null ? getDefault() : val;
	}
	
	public static boolean hasDragonArmor(String name)
	{
		return ARMOR_TYPES.containsKey(name);
	}
	
	public static List<DragonArmor> getDragonArmors()
	{
		return new ArrayList<>(ARMOR_TYPES.values());
	}
	
	public static DragonArmor getFirst()
	{
		return getDragonArmors().stream().findFirst().orElse(new DragonArmor());
	}
	
	public static DragonArmor getDefault()
	{
		return hasDragonArmor("iron") ? getDragonArmor("iron") : getFirst();
	}
}
