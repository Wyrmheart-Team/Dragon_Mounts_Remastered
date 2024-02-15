package dmr.DragonMounts.types.abilities.dragon_types.water_dragon;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.NearbyAbility;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class AquaticGraceAbility implements NearbyAbility
{
	@Override
	public String type()
	{
		return "aquatic_grace";
	}
	
	@Override
	public int getRange()
	{
		return 0;
	}
	
	@Override
	public void tick(DMRDragonEntity dragon, Player owner)
	{
		if(dragon.isInWater()) {
			owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20, 0, true, false, false));
		}
	}
}
