package dmr.DragonMounts.types.abilities.dragon_types.forest_dragon;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.types.NearbyAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

public class NatureBlessingAbility implements NearbyAbility {

    @Override
    public String type() {
        return "nature_blessing";
    }

    @Override
    public void tick(TameableDragonEntity dragon, Player owner) {
        if (!dragon.level.isClientSide) {
            var level = dragon.level;
            var basePos = dragon.blockPosition();
            var blocks = BlockPos.betweenClosedStream(basePos.offset(2, 2, 2), basePos.offset(-2, -2, -2))
                    .map(level::getBlockState)
                    .filter(state -> state.is(Blocks.GRASS_BLOCK)
                            || state.is(BlockTags.FLOWERS)
                            || state.is(BlockTags.SAPLINGS));

            if (blocks.findAny().isPresent()) {
                dragon.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));
                owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));
            }
        }
    }
}
