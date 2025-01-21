package dmr.DragonMounts.types.abilities.types;

import dmr.DragonMounts.abilities.Ability;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.core.BlockPos;

public interface FootprintAbility extends Ability {
	@Override
	default void onMove(DMRDragonEntity dragon) {
		if (dragon.level.isClientSide || !dragon.isAdult() || !dragon.onGround()) return;

		var chance = getFootprintChance(dragon);
		if (chance == 0) return;

		for (int i = 0; i < 4; i++) {
			// place only if randomly selected
			if (dragon.getRandom().nextFloat() > chance) {
				continue;
			}

			// get footprint position
			int bx = (int) (dragon.getX() + ((i % 2) * 2 - 1) * 0.25f);
			int by = (int) dragon.getY();
			int bz = (int) (dragon.getZ() + (((i / 2f) % 2) * 2 - 1) * 0.25f);
			var pos = new BlockPos(bx, by, bz);

			placeFootprint(dragon, pos);
		}
	}

	default float getFootprintChance(DMRDragonEntity dragon) {
		return 0.05f;
	}

	void placeFootprint(DMRDragonEntity dragon, BlockPos pos);
}
