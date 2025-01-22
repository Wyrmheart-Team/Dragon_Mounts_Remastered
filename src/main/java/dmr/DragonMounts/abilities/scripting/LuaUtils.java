package dmr.DragonMounts.abilities.scripting;

import java.util.ArrayList;
import java.util.List;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class LuaUtils {

	static void copyAllGlobals(Globals globals, LuaTable environment) {
		LuaValue key = LuaValue.NIL;
		while (true) {
			Varargs n = globals.next(key);
			key = n.arg1();
			if (key.isnil()) break;

			LuaValue value = n.arg(2);
			environment.set(key, value);
		}
	}

	static LuaValue sanitizeValue(Object input) {
		return switch (input) {
			case String s -> LuaValue.valueOf(escapeLuaString(s));
			case Integer i -> LuaValue.valueOf(i);
			case Double v -> LuaValue.valueOf(v);
			case Boolean b -> LuaValue.valueOf(b);
			case null -> LuaValue.NIL;
			default -> throw new IllegalArgumentException("Unsupported value type: " + input.getClass().getSimpleName());
		};
	}

	static List<String> getGlobalsKeys(LuaValue globals) {
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

	private static String escapeLuaString(String input) {
		return input
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("'", "\\'")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\0", "\\0");
	}
}
