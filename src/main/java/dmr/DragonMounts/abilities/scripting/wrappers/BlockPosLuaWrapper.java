package dmr.DragonMounts.abilities.scripting.wrappers;

import net.minecraft.core.BlockPos;

public class BlockPosLuaWrapper {

	private final BlockPos pos;

	public BlockPosLuaWrapper(BlockPos pos) {
		this.pos = pos;
	}

	public double x() {
		return pos.getX();
	}

	public double y() {
		return pos.getY();
	}

	public double z() {
		return pos.getZ();
	}
}
