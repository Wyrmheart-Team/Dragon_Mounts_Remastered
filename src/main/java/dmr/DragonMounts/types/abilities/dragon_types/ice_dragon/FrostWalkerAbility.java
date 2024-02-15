package dmr.DragonMounts.types.abilities.dragon_types.ice_dragon;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class FrostWalkerAbility implements Ability
{
    @Override
    public void initialize(DMRDragonEntity dragon)
    {
        dragon.setPathfindingMalus(BlockPathTypes.WATER, 0);
    }

    @Override
    public void close(DMRDragonEntity dragon)
    {
        dragon.setPathfindingMalus(BlockPathTypes.WATER, BlockPathTypes.WATER.getMalus());
    }

    @Override
    public void onMove(DMRDragonEntity dragon)
    {
        if (!dragon.level.isClientSide() && dragon.isAdult())
            FrostWalkerEnchantment.onEntityMoved(dragon, dragon.level, dragon.blockPosition(), (int) Math.max(3 * dragon.getScale(), 1));
    }
    
    @Override
    public void tick(DMRDragonEntity dragon)
    {
        if (!dragon.level.isClientSide() && dragon.isAdult())
            FrostWalkerEnchantment.onEntityMoved(dragon, dragon.level, dragon.blockPosition(), (int) Math.max(3 * dragon.getScale(), 1));
    }
    
    @Override
    public String type()
    {
        return "frost_walker";
    }
}
