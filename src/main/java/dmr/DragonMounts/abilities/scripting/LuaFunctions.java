package dmr.DragonMounts.abilities.scripting;

import lombok.Getter;

@Getter
public enum LuaFunctions {
	init("init"),
	close("close"),
	onTick("onTick"),
	onMove("onMove");

	private final String name;

	LuaFunctions(String name) {
		this.name = name;
	}
}
