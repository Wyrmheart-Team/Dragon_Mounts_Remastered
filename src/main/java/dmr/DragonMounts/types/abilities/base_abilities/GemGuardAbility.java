package dmr.DragonMounts.types.abilities.base_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@EventBusSubscriber
public class GemGuardAbility extends Ability {
    private double deflectChance = 0.25;

    public GemGuardAbility(String abilityType) {
        super(abilityType);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        if (definition.getProperties().containsKey("deflect_chance")) {
            this.deflectChance = (double) definition.getProperties().get("deflect_chance");
        }
    }

    @SubscribeEvent
    public static void projectileEvent(ProjectileImpactEvent event) {
        var hitResult = event.getRayTraceResult();

        if (hitResult instanceof EntityHitResult result && result.getType() == Type.ENTITY) {
            var target = result.getEntity();

            if (target instanceof TameableDragonEntity dragon) {
                if (dragon.hasAbility(GemGuardAbility.class)) {
                    var ability = dragon.getAbility(GemGuardAbility.class);
                    if (dragon.getRandom().nextDouble() < ability.deflectChance) {
                        event.setCanceled(true);
                    }
                }
            } else if (target instanceof Player player) {
                if (player.getVehicle() instanceof TameableDragonEntity dragon) {
                    if (dragon.hasAbility(GemGuardAbility.class)) {
                        var ability = dragon.getAbility(GemGuardAbility.class);
                        if (dragon.getRandom().nextDouble() < ability.deflectChance) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }
}
