package dmr.DragonMounts.client.model;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.common.config.DMRConfig;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
		
		if(dragon.getControllingPassenger() instanceof Player player) {
			var lookVector = true;
			if(dragon.level.isClientSide){
				if(!cameraFlightCheck(player)){
					lookVector = false;
				}
			}
			
			if(lookVector){
				viewVector = player.getViewVector(1f);
			}else{
				viewVector = dragon.getDeltaMovement().multiply(0.5, 0.5, 0.5);
			}
		}
		
		if(viewVector != null) {
			var pitch = viewVector.y;
			parser.setValue("query.pitch", () -> Mth.clamp(pitch, -1, 1));
		}
	}
	
	@OnlyIn( Dist.CLIENT)
	private boolean cameraFlightCheck(Player player){
		return DMRConfig.CAMERA_FLIGHT.get() && player == Minecraft.getInstance().player;
	}
}
