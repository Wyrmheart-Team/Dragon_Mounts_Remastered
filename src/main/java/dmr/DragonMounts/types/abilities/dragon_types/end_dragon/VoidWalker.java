package dmr.DragonMounts.types.abilities.dragon_types.end_dragon;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.NearbyAbility;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class VoidWalker implements NearbyAbility
{
	@Override
	public String type()
	{
		return "void_walker";
	}
	
	@Override
	public void tick(DMRDragonEntity dragon, Player owner)
	{
		owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20*20, 0, true, false, true));
	}
}
