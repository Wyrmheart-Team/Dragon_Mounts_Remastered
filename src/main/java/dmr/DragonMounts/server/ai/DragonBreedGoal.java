package dmr.DragonMounts.server.ai;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;

import java.util.List;

public class DragonBreedGoal extends BreedGoal
{
	private final DMRDragonEntity dragon;
	
	public DragonBreedGoal(DMRDragonEntity animal)
	{
		super(animal, 1);
		this.dragon = animal;
	}
	
	@Override
	public boolean canUse()
	{
		if (!dragon.isAdult()) return false;
        if (!dragon.isInLove()) {return false;} else return (partner = getNearbyMate()) != null;
	}
	
	public DMRDragonEntity getNearbyMate()
	{
		List<DMRDragonEntity> list = level.getEntitiesOfClass(DMRDragonEntity.class, dragon.getBoundingBox().inflate(16d));
		double dist = Double.MAX_VALUE;
		DMRDragonEntity closest = null;
		
		for (DMRDragonEntity entity : list) {
			if (dragon.canMate(entity) && dragon.distanceToSqr(entity) < dist) {
				closest = entity;
				dist = dragon.distanceToSqr(entity);
			}
		}
		
		return closest;
	}
	
	@Override
	protected void breed()
	{
		// Respect Mod compatibility
		if (NeoForge.EVENT_BUS.post(new BabyEntitySpawnEvent(animal, partner, null)).isCanceled()) {
			// Reset the "inLove" state for the animals
			animal.setAge(6000);
			partner.setAge(6000);
			return;
		}
		
		animal.resetLove();
		partner.resetLove();
		dragon.spawnChildFromBreeding((ServerLevel)level, partner);
		level.broadcastEntityEvent(this.animal, (byte)18);
		if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) level.addFreshEntity(new ExperienceOrb(level, animal.getX(), animal.getY(), animal.getZ(), animal.getRandom().nextInt(7) + 1));
	}
}