package dmr.DragonMounts.types.abilities.dragon_types.fire_dragon;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.NearbyAbility;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class FireProofAbility implements NearbyAbility
{
	@Override
	public String type()
	{
		return "fire_proof";
	}
	
	@Override
	public void tick(DMRDragonEntity dragon, Player owner)
	{
		if (!dragon.level.isClientSide) {
			owner.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false, true));
		}
	}
}
