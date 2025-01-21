package dmr.DragonMounts.abilities.scripting.wrappers;

import net.minecraft.core.BlockPos;

public class BlockPosLuaWrapper {

	private final BlockPos pos;

	public BlockPosLuaWrapper(BlockPos pos) {
		this.pos = pos;
	}

	public int getX() {
		return pos.getX();
	}

	public int getY() {
		return pos.getY();
	}

	public int getZ() {
		return pos.getZ();
	}
}
