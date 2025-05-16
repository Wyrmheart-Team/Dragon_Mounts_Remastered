package dmr.DragonMounts.types.abilities.dragon_types.nether_dragon;

import dmr.DragonMounts.registry.DragonAbilities;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber
public class InfernalPactAbility implements Ability {

    @Override
    public String type() {
        return "infernal_pact";
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof AbstractPiglin
                || event.getEntity() instanceof Ghast
                || event.getEntity() instanceof MagmaCube) {
            if (event.getNewAboutToBeSetTarget() instanceof TameableDragonEntity dragon) {
                if (dragon.getBreed().getAbilities().contains(DragonAbilities.INFERNAL_PACT_ABILITY)) {
                    event.setCanceled(true);
                }
            } else if (event.getNewAboutToBeSetTarget() instanceof Player player) {
                if (player.getVehicle() instanceof TameableDragonEntity dragon) {
                    if (dragon.getBreed().getAbilities().contains(DragonAbilities.INFERNAL_PACT_ABILITY)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
