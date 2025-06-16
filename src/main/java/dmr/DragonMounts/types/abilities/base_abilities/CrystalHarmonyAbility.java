package dmr.DragonMounts.types.abilities.base_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import dmr.DragonMounts.types.abilities.DragonAbility;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber
public class CrystalHarmonyAbility extends Ability {
    private double protection_chance = 0.1;

    public CrystalHarmonyAbility(String abilityType) {
        super(abilityType);
    }

    @Override
    public void initializeDefinition(DragonAbility definition) {
        super.initializeDefinition(definition);
        if (definition.getProperties().containsKey("protection_chance")) {
            this.protection_chance = (double) definition.getProperties().get("protection_chance");
        }
    }

    @SubscribeEvent
    public static void entityHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof TameableDragonEntity dragon) {
            if (dragon.hasAbility(CrystalHarmonyAbility.class)) {
                var ability = dragon.getAbility(CrystalHarmonyAbility.class);
                if (dragon.getRandom().nextDouble() <= ability.protection_chance) {
                    event.setNewDamage(0);
                }
            }
        } else if (event.getEntity() instanceof Player player) {
            if (player.getVehicle() instanceof TameableDragonEntity dragon) {
                if (dragon.hasAbility(CrystalHarmonyAbility.class)) {
                    var ability = dragon.getAbility(CrystalHarmonyAbility.class);
                    if (dragon.getRandom().nextDouble() <= ability.protection_chance) {
                        event.setNewDamage(0);
                    }
                }
            }
        }
    }
}
