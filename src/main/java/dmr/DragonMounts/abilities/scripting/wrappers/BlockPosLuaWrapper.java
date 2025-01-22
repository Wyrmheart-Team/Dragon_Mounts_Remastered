package dmr.DragonMounts.abilities.scripting.wrappers;

import net.minecraft.core.BlockPos;

public class BlockPosLuaWrapper {

	private final BlockPos pos;
	public int x, y, z;

	public BlockPosLuaWrapper(BlockPos pos) {
		this.pos = pos;
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
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
