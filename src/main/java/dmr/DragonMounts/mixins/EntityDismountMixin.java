package dmr.DragonMounts.mixins;

import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.network.packets.DismountDragonPacket;
import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.registry.ModMemoryModuleTypes;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityDismountMixin {

	@Inject(method = "stopRiding", at = @At("HEAD"))
	public void stopRiding(CallbackInfo ci) {
		if (((Entity) (Object) this) instanceof Player player) {
			if (player.getControlledVehicle() instanceof DMRDragonEntity dragon) {
				DragonOwnerCapability cap = player.getData(ModCapabilities.PLAYER_CAPABILITY);
				cap.shouldDismount = false;
				dragon.getBrain().setMemoryWithExpiry(ModMemoryModuleTypes.SHOULD_SIT.get(), true, 40L);

				if (player.level.isClientSide()) {
					PacketDistributor.sendToServer(new DismountDragonPacket(player.getId(), false));
				} else {
					PacketDistributor.sendToPlayer((ServerPlayer) (Object) this, new DismountDragonPacket(player.getId(), false));
				}
			}
		}
	}
}
