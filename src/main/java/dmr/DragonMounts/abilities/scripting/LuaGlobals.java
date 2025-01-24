package dmr.DragonMounts.abilities.scripting;

import com.mojang.logging.LogUtils;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.abilities.scripting.lua.ReadOnlyLuaTable;
import java.io.IOException;
import java.io.InputStream;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LuaGlobals {

	public static Globals globals = JsePlatform.standardGlobals();

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
					String message = output.toString();
					if (message.contains("ERROR")) {
						LogUtils.getLogger().error(message);
					} else {
						LogUtils.getLogger().info(message);
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
}
