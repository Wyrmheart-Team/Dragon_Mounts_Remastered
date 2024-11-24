package dmr.DragonMounts.common.capability.types;

import net.minecraft.nbt.CompoundTag;

public interface NBTInterface {
	CompoundTag writeNBT();
	void readNBT(CompoundTag base);
}
