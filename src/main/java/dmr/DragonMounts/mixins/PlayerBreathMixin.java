package dmr.DragonMounts.mixins;

import dmr.DragonMounts.registry.DragonAbilities;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin( LivingEntity.class )
public abstract class PlayerBreathMixin
{
	@Inject( method = "canBreatheUnderwater", at = @At( "HEAD" ), cancellable = true )
	public void canBreatheUnderwater(CallbackInfoReturnable<Boolean> ci)
	{
		if (((LivingEntity)(Object)this) instanceof Player player) {
			if (player.isPassenger() && player.getVehicle() instanceof DMRDragonEntity dragon) {
				if (!dragon.canDrownInFluidType(Fluids.WATER.getFluidType())) {
					if (dragon.getBreed().getAbilities().contains(DragonAbilities.AQUATIC_GRACE_ABILITY)) {
						ci.setReturnValue(true);
					}
				}
			}
		}
	}
}
