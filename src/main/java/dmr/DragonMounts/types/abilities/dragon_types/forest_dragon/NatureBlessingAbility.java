package dmr.DragonMounts.types.abilities.dragon_types.forest_dragon;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.NearbyAbility;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;


public class NatureBlessingAbility implements NearbyAbility {
	@Override
	public String type()
	{
		return "nature_blessing";
	}
	
	@Override
	public void tick(DMRDragonEntity dragon, Player owner)
	{
		if (!dragon.level.isClientSide) {
			var level = dragon.level;
			var block = level.getBlockState(dragon.blockPosition());
			
			if (block.is(Blocks.GRASS_BLOCK) || block.is(BlockTags.FLOWERS) || block.is(BlockTags.SAPLINGS)) {
				dragon.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));
				owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));
			}
		}
	}
}
