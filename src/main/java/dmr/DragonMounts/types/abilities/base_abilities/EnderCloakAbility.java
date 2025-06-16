package dmr.DragonMounts.types.abilities.base_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import java.util.List;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.EnderManAngerEvent;

@EventBusSubscriber
public class EnderCloakAbility extends Ability {

    private static double protectionRadius = 32.0;

    public EnderCloakAbility(String abilityType) {
        super(abilityType);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        if (definition.getProperties().containsKey("protection_radius")) {
            protectionRadius = (double) definition.getProperties().get("protection_radius");
        }
    }

    @SubscribeEvent
    public static void endermanAngerEvent(EnderManAngerEvent event) {
        var player = event.getPlayer();
        var level = player.level;

        if (player.getVehicle() instanceof TameableDragonEntity dragon) {
            if (dragon.hasAbility(EnderCloakAbility.class)) {
                event.setCanceled(true);
                return;
            }
        }

        List<TameableDragonEntity> dragonEntities = level.getEntitiesOfClass(
                TameableDragonEntity.class, event.getEntity().getBoundingBox().inflate(protectionRadius));
        dragonEntities = dragonEntities.stream()
                .filter(s -> s.getOwnerUUID() == player.getUUID())
                .toList();

        for (TameableDragonEntity dragon : dragonEntities) {
            if (dragon.hasAbility(EnderCloakAbility.class)) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
