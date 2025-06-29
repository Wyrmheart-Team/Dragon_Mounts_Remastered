package dmr.DragonMounts.types.abilities.dragon_types.forest_dragon;

import dmr.DragonMounts.registry.DragonAbilities;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber
public class CamouflageAbility implements Ability {

    @Override
    public String type() {
        return "camouflage";
    }

    private static final int range = 5;

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof TameableDragonEntity dragon) {
            if (dragon.getBreed().getAbilities().contains(DragonAbilities.CAMOUFLAGE_ABILITY)) {
                if (event.getEntity().distanceTo(dragon) > range) {
                    event.setCanceled(true);
                }
            }
        } else if (event.getNewAboutToBeSetTarget() instanceof Player player) {
            if (player.getVehicle() instanceof TameableDragonEntity dragon) {
                if (dragon.getBreed().getAbilities().contains(DragonAbilities.CAMOUFLAGE_ABILITY)) {
                    if (event.getEntity().distanceTo(dragon) > range) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
