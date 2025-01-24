package dmr.DragonMounts.abilities.scripting.wrappers;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

public class LuaProxy {

	public static LuaValue wrap(Object target) {
		if (target == null) return LuaValue.NIL;

		// Avoid re-wrapping if already a LuaValue
		if (target instanceof LuaValue) {
			return (LuaValue) target;
		}

		// Handle basic types directly
		if (target instanceof String) {
			return LuaValue.valueOf((String) target);
		} else if (target instanceof Number) {
			return LuaValue.valueOf(((Number) target).doubleValue());
		} else if (target instanceof Boolean) {
			return LuaValue.valueOf((Boolean) target);
		} else if (target instanceof List<?>) {
			return convertListToLuaTable((List<?>) target);
		} else if (target instanceof Map<?, ?>) {
			return convertMapToLuaTable((Map<?, ?>) target);
		} else if (target.getClass().isArray()) {
			return convertArrayToLuaTable((Object[]) target);
		}

		// Convert Java object to Lua table
		return createLuaTableFromObject(target);
	}

	private static LuaTable createLuaTableFromObject(Object target) {
		LuaTable luaTable = new LuaTable();
		luaTable.set("__java_object", LuaValue.userdataOf(target)); // Store reference to original Java object

		Class<?> clazz = target.getClass();

		for (Method method : clazz.getMethods()) {
			// Ignore inherited methods from Object class
			if (method.getDeclaringClass().equals(Object.class)) {
				continue;
			}

			luaTable.set(method.getName(), new LuaFunctionProxy(target, method));
		}

		return luaTable;
	}

	private static LuaTable convertListToLuaTable(List<?> list) {
		LuaTable luaTable = new LuaTable();
		for (int i = 0; i < list.size(); i++) {
			luaTable.set(i + 1, wrap(list.get(i))); // Lua uses 1-based indexing
		}
		return luaTable;
	}

	private static LuaTable convertMapToLuaTable(Map<?, ?> map) {
		LuaTable luaTable = new LuaTable();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			luaTable.set(wrap(entry.getKey()), wrap(entry.getValue()));
		}
		return luaTable;
	}

	private static LuaTable convertArrayToLuaTable(Object[] array) {
		LuaTable luaTable = new LuaTable();
		for (int i = 0; i < array.length; i++) {
			luaTable.set(i + 1, wrap(array[i]));
		}
		return luaTable;
	}

	// Proxy class to call Java methods from Lua
	private static class LuaFunctionProxy extends VarArgFunction {

		// Proxy class to safely invoke Java methods from Lua
		private final Object target;
		private final Method method;

		public LuaFunctionProxy(Object target, Method method) {
			this.target = target;
			this.method = method;
		}

		@Override
		public Varargs invoke(Varargs args) {
			try {
				Object[] javaArgs = convertLuaArgsToJava(args, method.getParameterTypes());
				Object result = method.invoke(target, javaArgs);

				// Return result if it's already Lua-compatible
				if (result instanceof LuaValue) {
					return (LuaValue) result;
				}
				return wrap(result);
			} catch (Exception e) {
				return LuaValue.error("Error invoking method: " + method.getName() + " - " + e.getMessage());
			}
		}

		@Override
		public Varargs onInvoke(Varargs args) {
			// Directly call the base class implementation to avoid recursion
			return invoke(args);
		}

		private static Object[] convertLuaArgsToJava(Varargs luaArgs, Class<?>[] paramTypes) {
			int argOffset = 1; // LuaJ adds "self" as the first argument, so we skip it.
			Object[] javaArgs = new Object[paramTypes.length];

			for (int i = 0; i < paramTypes.length; i++) {
				LuaValue arg = luaArgs.arg(i + argOffset + 1);

				// Check if argument is a LuaTable representing a Java object
				if (arg.istable() && arg.get("__java_object").isuserdata()) {
					javaArgs[i] = arg.get("__java_object").touserdata(paramTypes[i]);
				} else {
					javaArgs[i] = CoerceLuaToJava.coerce(arg, paramTypes[i]);
				}
			}

			return javaArgs;
		}
	}
}
