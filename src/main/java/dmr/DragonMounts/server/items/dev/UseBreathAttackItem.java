package dmr.DragonMounts.server.items.dev;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.server.items.DMRDevItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.UseOnContext;

public class UseBreathAttackItem extends DMRDevItem {

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        var ents = pContext.getLevel()
                .getEntities(
                        pContext.getPlayer(),
                        pContext.getPlayer().getBoundingBox().inflate(50),
                        ent -> ent instanceof TameableDragonEntity);

        for (Entity ent : ents) {
            if (ent instanceof TameableDragonEntity dragon) {
                dragon.setBreathAttackBlock(pContext.getClickedPos());
            }
        }

        return super.useOn(pContext);
    }
}
