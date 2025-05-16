package dmr.DragonMounts.types.abilities.dragon_types.end_dragon;

import dmr.DragonMounts.registry.DragonAbilities;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import java.util.List;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.EnderManAngerEvent;

@EventBusSubscriber
public class EnderCloakAbility implements Ability {

    @Override
    public String type() {
        return "ender_cloak";
    }

    @SubscribeEvent
    public static void endermanAngerEvent(EnderManAngerEvent event) {
        var player = event.getPlayer();
        var level = player.level;

        if (player.getVehicle() instanceof TameableDragonEntity dragon) {
            if (dragon.getBreed().getAbilities().contains(DragonAbilities.ENDER_CLOAK)) {
                event.setCanceled(true);
                return;
            }
        }

        List<TameableDragonEntity> dragonEntities = level.getEntitiesOfClass(
                TameableDragonEntity.class, event.getEntity().getBoundingBox().inflate(32));
        dragonEntities = dragonEntities.stream()
                .filter(s -> s.getOwnerUUID() == player.getUUID())
                .toList();

        for (TameableDragonEntity dragon : dragonEntities) {
            if (dragon.getBreed().getAbilities().contains(DragonAbilities.ENDER_CLOAK)) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
