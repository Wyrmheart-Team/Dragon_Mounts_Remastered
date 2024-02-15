package dmr.DragonMounts.client.model;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.model.GeoModel;


public class DragonModel extends GeoModel<DMRDragonEntity>
{
	
	@Override
	public ResourceLocation getModelResource(DMRDragonEntity object)
	{
		if(object != null && object.getBreed() != null && object.getBreed().getDragonModelLocation() != null)
			return object.getBreed().getDragonModelLocation();
		
		return DragonMountsRemaster.id("geo/dragon.geo.json");
	}
	
	@Override
	public ResourceLocation getTextureResource(DMRDragonEntity object)
	{
		var breed = object.getBreed();
		var breedResourceLocation = breed.getResourceLocation();
		return DragonMountsRemaster.id("textures/entity/dragon/" + breedResourceLocation.getPath() + "/body.png");
	}
	
	@Override
	public ResourceLocation getAnimationResource(DMRDragonEntity animatable)
	{
		if(animatable != null && animatable.getBreed() != null && animatable.getBreed().getDragonAnimationLocation() != null)
			return animatable.getBreed().getDragonAnimationLocation();
		
		return DragonMountsRemaster.id("animations/dragon.animation.json");
	}
	
	@Override
	public void applyMolangQueries(DMRDragonEntity dragon, double seekTime)
	{
		super.applyMolangQueries(dragon, seekTime);
		MolangParser parser = MolangParser.INSTANCE;

		parser.setValue("query.head_pitch", () -> dragon.getXRot() * 1);
		parser.setValue("query.head_yaw", () -> (dragon.yBodyRot - dragon.yHeadRot) * -1);

		var viewVector = dragon.getViewVector(1f);
		
		if(dragon.getControllingPassenger() instanceof Player player){
			viewVector = player.getViewVector(1f);
		}
		
		if(viewVector != null) {
			var pitch = viewVector.y;
			parser.setValue("query.pitch", () -> pitch);
		}
		
	}
}
