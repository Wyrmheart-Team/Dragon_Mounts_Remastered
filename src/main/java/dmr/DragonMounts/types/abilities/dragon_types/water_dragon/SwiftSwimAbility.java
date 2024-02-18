package dmr.DragonMounts.types.abilities.dragon_types.water_dragon;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.NearbyAbility;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class SwiftSwimAbility implements NearbyAbility
{
	@Override
	public String type()
	{
		return "swift_swim";
	}
	
	@Override
	public int getRange()
	{
		return 2;
	}
	
	@Override
	public void tick(DMRDragonEntity dragon, Player owner)
	{
		if(!dragon.level.isClientSide) {
			dragon.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 1, true, false, false));
			owner.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 1, true, false, false));
		}
	}
}
