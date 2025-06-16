package dmr.DragonMounts.types.abilities.base_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber
public class EtherealHarmonyAbility extends Ability {
    public EtherealHarmonyAbility(String abilityType) {
        super(abilityType);
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity().getType().is(EntityTypeTags.UNDEAD)) {
            if (event.getNewAboutToBeSetTarget() instanceof TameableDragonEntity dragon) {
                if (dragon.hasAbility(EtherealHarmonyAbility.class)) {
                    event.setCanceled(true);
                }
            } else if (event.getNewAboutToBeSetTarget() instanceof Player player) {
                if (player.getVehicle() instanceof TameableDragonEntity dragon) {
                    if (dragon.hasAbility(EtherealHarmonyAbility.class)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
