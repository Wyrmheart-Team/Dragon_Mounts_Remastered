package dmr.DragonMounts.types.abilities.base_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

@EventBusSubscriber
public class VoidWalker extends Ability {

    private static double protectionRadius = 5.0;

    public VoidWalker(String abilityType) {
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
    public static void onFallDamage(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            var level = player.level;

            var dragons = level.getNearbyEntities(
                    TameableDragonEntity.class,
                    TargetingConditions.forNonCombat(),
                    player,
                    player.getBoundingBox().inflate(protectionRadius));

            for (var dragon : dragons) {
                if (dragon.getOwner() != player) continue;
                if (dragon.hasAbility(VoidWalker.class)) {
                    event.setCanceled(true);
                    break;
                }
            }
        }
    }
}
