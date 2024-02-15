package dmr.DragonMounts.types.abilities.dragon_types.fire_dragon;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.FootprintAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;


public class HotFeetAbility implements FootprintAbility
{
    public static final TagKey<Block> BURNABLES_TAG = BlockTags.create(DragonMountsRemaster.id("hot_feet_burnables"));

    @Override
    public void placeFootprint(DMRDragonEntity dragon, BlockPos pos)
    {
        var level = dragon.level;
        var steppingOn = level.getBlockState(pos);
        if (steppingOn.is(BURNABLES_TAG))
        {
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, dragon.getSoundSource(), 0.1f, 2f);
            ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 1, 0, 0.05);
        }
    }

    @Override
    public String type()
    {
        return "hot_feet";
    }
}
