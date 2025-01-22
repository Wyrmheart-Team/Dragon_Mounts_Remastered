package dmr.DragonMounts.registry;

import dmr.DragonMounts.abilities.DragonAbility;
import dmr.DragonMounts.abilities.scripting.LuaFunctions;
import dmr.DragonMounts.abilities.scripting.ScriptInstance;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DragonAbilityRegistry {

	public static final HashMap<String, DragonAbility> ABILITIES = new HashMap<>();
	public static final HashMap<String, ScriptInstance> SCRIPTS = new HashMap<>();

	public static DragonAbility getDragonAbility(String name) {
		return ABILITIES.getOrDefault(name, null);
	}

	public static boolean hasDragonAbility(String name) {
		return ABILITIES.containsKey(name);
	}

	public static boolean hasScript(String name) {
		return SCRIPTS.containsKey(name);
	}

	public static List<DragonAbility> getDragonAbilities() {
		return new ArrayList<>(ABILITIES.values());
	}

	public static void callScript(String name, LuaFunctions function, DMRDragonEntity dragon, Object... args) {
		var script = SCRIPTS.get(name);
		var ability = ABILITIES.get(name);

		if (script == null || ability == null) {
			return;
		}
		script.execute(ability, dragon, function.getName(), args);
	}
}
