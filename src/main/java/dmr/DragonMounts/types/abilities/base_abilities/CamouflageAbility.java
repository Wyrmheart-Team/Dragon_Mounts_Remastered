package dmr.DragonMounts.types.abilities.base_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber
public class CamouflageAbility extends Ability {
    private int range = 5;

    public CamouflageAbility(String abilityType) {
        super(abilityType);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);

        if (definition.getProperties().containsKey("range")) {
            this.range = (int) definition.getProperties().get("range");
        } else {
            this.range = 5;
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof TameableDragonEntity dragon) {
            if (dragon.hasAbility(CamouflageAbility.class)) {
                var ability = dragon.getAbility(CamouflageAbility.class);
                if (event.getEntity().distanceTo(dragon) > ability.range) {
                    event.setCanceled(true);
                }
            }
        } else if (event.getNewAboutToBeSetTarget() instanceof Player player) {
            if (player.getVehicle() instanceof TameableDragonEntity dragon) {
                if (dragon.hasAbility(CamouflageAbility.class)) {
                    var ability = dragon.getAbility(CamouflageAbility.class);
                    if (event.getEntity().distanceTo(dragon) > ability.range) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
