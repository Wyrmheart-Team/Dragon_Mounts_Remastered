package dmr.DragonMounts.abilities.scripting;

import com.mojang.logging.LogUtils;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.abilities.DragonAbility;
import dmr.DragonMounts.abilities.scripting.lua.ReadOnlyLuaTable;
import dmr.DragonMounts.abilities.scripting.wrappers.DragonLuaWrapper;
import dmr.DragonMounts.abilities.scripting.wrappers.LuaRandomWrapper;
import dmr.DragonMounts.abilities.scripting.wrappers.PlayerLuaWrapper;
import dmr.DragonMounts.abilities.scripting.wrappers.WorldLuaWrapper;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import net.minecraft.world.entity.player.Player;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

public class ScriptInstance {

	public static Globals globals = JsePlatform.standardGlobals();
	private LuaValue environment;

	static {
		// read only string metatable
		LuaString.s_metatable = new ReadOnlyLuaTable(LuaString.s_metatable);

		// Add a custom print function
		globals.set(
			"print",
			new VarArgFunction() {
				@Override
				public Varargs invoke(Varargs args) {
					var scriptName = globals.get("script_name").tojstring();
					var functionName = globals.get("function_name").tojstring();

					StringBuilder output = new StringBuilder("[" + scriptName + ":" + functionName + "] ");
					for (int i = 1; i <= args.narg(); i++) {
						output.append(args.arg(i).isnil() ? "nil" : args.arg(i).tojstring());
						if (i < args.narg()) {
							output.append("\t");
						}
					}
					if (DMR.DEBUG) {
						String message = output.toString();
						if (message.contains("ERROR")) {
							LogUtils.getLogger().error(message);
						} else {
							LogUtils.getLogger().info(message);
						}
					}

					return NONE;
				}
			}
		);

		globals.load("params = params or {}", "setup").call();
		fetchResource("sandbox.lua", "sandbox");
		fetchResource("math.lua", "math");
	}

	private static void fetchResource(String s, String name) {
		var path = String.format("/data/%s/scripts/%s", DMR.MOD_ID, s);
		try (InputStream inputStream = DMR.class.getResourceAsStream(path)) {
			if (inputStream == null) throw new IOException("Unable to get resource " + path);
			globals.load(new String(inputStream.readAllBytes()), name).call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final HashMap<String, LuaValue> func = new HashMap<>();
	private final String path;

	public ScriptInstance(String scriptPath, String scriptContent, String... function) {
		String wrappedScript = "local _ENV = {};\n" + scriptContent + "\nreturn _ENV;";

		globals.set("random", CoerceJavaToLua.coerce(new LuaRandomWrapper(new Random())));
		var scriptChunk = globals.load(wrappedScript, "script");

		if (scriptChunk.isnil()) {
			throw new RuntimeException("Script file " + scriptPath + " not found");
		}

		environment = scriptChunk.call();
		copyAllGlobals(globals, environment.checktable());

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

	private static LuaValue sanitizeValue(Object input) {
		return switch (input) {
			case String s -> LuaValue.valueOf(escapeLuaString(s));
			case Integer i -> LuaValue.valueOf(i);
			case Double v -> LuaValue.valueOf(v);
			case Boolean b -> LuaValue.valueOf(b);
			case null -> LuaValue.NIL;
			default -> throw new IllegalArgumentException("Unsupported value type: " + input.getClass().getSimpleName());
		};
	}

	private static String escapeLuaString(String input) {
		return input
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("'", "\\'")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\0", "\\0");
	}

	private static List<String> getKeys(LuaValue globals) {
		List<String> keys = new ArrayList<>();
		LuaValue key = LuaValue.NIL;

		while (true) {
			Varargs n = globals.next(key);
			key = n.arg1();
			if (key.isnil()) break;
			keys.add(key.tojstring());
		}

		return keys;
	}

	private static void copyAllGlobals(Globals globals, LuaTable environment) {
		LuaValue key = LuaValue.NIL;
		while (true) {
			Varargs n = globals.next(key);
			key = n.arg1();
			if (key.isnil()) break;

			LuaValue value = n.arg(2);
			environment.set(key, value);
		}
	}

	public Varargs execute(DragonAbility ability, DMRDragonEntity dragon, String function, Object... args) {
		if (!this.func.containsKey(function)) {
			return LuaValue.NIL;
		}

		LuaValue paramTable = LuaValue.tableOf();

		for (var entry : ability.getScriptParameters().entrySet()) {
			paramTable.set(LuaValue.valueOf(entry.getKey()), sanitizeValue(entry.getValue()));
		}

		var preKeys = getKeys(environment);

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

		var val = doExecute(function, args);

		var postKeys = getKeys(environment);
		postKeys.removeAll(preKeys);

		for (String key : postKeys) {
			environment.set(key, LuaValue.NIL);
		}

		return val;
	}

	private Varargs doExecute(String function, Object... args) {
		var func = this.func.get(function);

		if (func == null || func.isnil()) {
			System.err.println("Function " + function + " is nil");
			return LuaValue.NIL;
		}

		try {
			globals.set("script_name", LuaValue.valueOf(this.path));
			globals.set("function_name", LuaValue.valueOf(function));

			if (args.length == 0) {
				return func.call();
			}

			LuaValue[] luaArgs = new LuaValue[args.length];
			for (int i = 0; i < args.length; i++) {
				luaArgs[i] = CoerceJavaToLua.coerce(args[i]);
			}

			return func.invoke(LuaValue.varargsOf(luaArgs));
		} catch (LuaError e) {
			System.err.printf("Lua error executing function %s: %s%n", function, e.getMessage());
			System.err.println(e.getMessageObject());
		}

		globals.set("script_name", LuaValue.NIL);
		globals.set("function_name", LuaValue.NIL);

		return LuaValue.NIL;
	}
}
