package dmr.DragonMounts.abilities.scripting;

import dmr.DragonMounts.abilities.DragonAbility;
import dmr.DragonMounts.abilities.scripting.wrappers.DragonLuaWrapper;
import dmr.DragonMounts.abilities.scripting.wrappers.LuaRandomWrapper;
import dmr.DragonMounts.abilities.scripting.wrappers.PlayerLuaWrapper;
import dmr.DragonMounts.abilities.scripting.wrappers.WorldLuaWrapper;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.world.entity.player.Player;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.HashMap;
import java.util.Random;

public class ScriptInstance {

	private final HashMap<String, LuaValue> func = new HashMap<>();
	private final String path;
	private final LuaValue environment;

	public ScriptInstance(String scriptPath, String scriptContent, String... function) {
		String wrappedScript = "local _ENV = {};\n" + scriptContent + "\nreturn _ENV;";

		LuaGlobals.globals.set("random", CoerceJavaToLua.coerce(new LuaRandomWrapper(new Random())));
		var scriptChunk = LuaGlobals.globals.load(wrappedScript, "script");

		if (scriptChunk.isnil()) {
			throw new RuntimeException("Script file " + scriptPath + " not found");
		}

		environment = scriptChunk.call();
		LuaUtils.copyAllGlobals(LuaGlobals.globals, environment.checktable());

		this.path = scriptPath;

		for (String funcName : function) {
			try {
				var func = environment.get(funcName);

				if (!func.isfunction()) {
					continue;
				}

				this.func.put(funcName, func);
			} catch (Exception e) {
				System.err.println("Error loading function " + funcName + " from script " + scriptPath);
			}
		}
	}

	public void execute(DragonAbility ability, DMRDragonEntity dragon, String function, Object... args) {
		if (!this.func.containsKey(function)) {
			return;
		}

		LuaValue paramTable = LuaValue.tableOf();

		for (var entry : ability.getScriptParameters().entrySet()) {
			paramTable.set(LuaValue.valueOf(entry.getKey()), LuaUtils.sanitizeValue(entry.getValue()));
		}

		var preKeys = LuaUtils.getGlobalsKeys(environment);

		environment.set("params", paramTable);

		if (dragon != null) {
			environment.set("isServer", LuaValue.valueOf(!dragon.level.isClientSide()));
			environment.set("hasOwner", LuaValue.valueOf(dragon.getOwner() != null));
			environment.set("tickCount", LuaValue.valueOf(dragon.tickCount % 1000));

			environment.set("dragon", CoerceJavaToLua.coerce(new DragonLuaWrapper(dragon)));
			environment.set("world", CoerceJavaToLua.coerce(new WorldLuaWrapper(dragon.level)));

			if (dragon.getOwner() != null) {
				environment.set("player", CoerceJavaToLua.coerce(new PlayerLuaWrapper((Player) dragon.getOwner())));
				environment.set("owner", environment.get("player"));
			}
		}

		Runnable clearFunc = () -> {
			var postKeys = LuaUtils.getGlobalsKeys(environment);
			postKeys.removeAll(preKeys);

			for (String key : postKeys) {
				environment.set(key, LuaValue.NIL);
			}
		};

		var func1 = this.func.get(function);

		if (func1 == null || func1.isnil()) {
			System.err.println("Function " + function + " is nil");
			clearFunc.run();
			return;
		}

		LuaGlobals.globals.set("script_name", LuaValue.valueOf(this.path));
		LuaGlobals.globals.set("function_name", LuaValue.valueOf(function));

		try {
			if (args.length == 0) {
				func1.call();
			} else {
				LuaValue[] luaArgs = new LuaValue[args.length];
				for (int i = 0; i < args.length; i++) {
					luaArgs[i] = CoerceJavaToLua.coerce(args[i]);
				}
				func1.invoke(LuaValue.varargsOf(luaArgs));
			}
		} catch (LuaError e) {
			System.err.printf("Lua error executing function %s: %s%n", function, e.getMessage());
		}

		LuaGlobals.globals.set("script_name", LuaValue.NIL);
		LuaGlobals.globals.set("function_name", LuaValue.NIL);

		clearFunc.run();
	}
}
