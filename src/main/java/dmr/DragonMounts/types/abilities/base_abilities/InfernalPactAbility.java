package dmr.DragonMounts.types.abilities.base_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber
public class InfernalPactAbility extends Ability {
    public InfernalPactAbility(String abilityType) {
        super(abilityType);
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof AbstractPiglin
                || event.getEntity() instanceof Ghast
                || event.getEntity() instanceof MagmaCube) {
            if (event.getNewAboutToBeSetTarget() instanceof TameableDragonEntity dragon) {
                if (dragon.hasAbility(InfernalPactAbility.class)) {
                    event.setCanceled(true);
                }
            } else if (event.getNewAboutToBeSetTarget() instanceof Player player) {
                if (player.getVehicle() instanceof TameableDragonEntity dragon) {
                    if (dragon.hasAbility(InfernalPactAbility.class)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
