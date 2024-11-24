package dmr.DragonMounts.server.items.dev;

import dmr.DragonMounts.server.blockentities.DMREggBlockEntity;
import dmr.DragonMounts.server.items.DMRDevItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

public class InstantHatchItem extends DMRDevItem {

	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		if (!pContext.getLevel().isClientSide) {
			if (pContext.getLevel().getBlockEntity(pContext.getClickedPos()) instanceof DMREggBlockEntity entity) {
				entity.hatch((ServerLevel) entity.getLevel(), entity.getBlockPos());
				return InteractionResult.SUCCESS;
			}
		}

		return super.useOn(pContext);
	}
}
