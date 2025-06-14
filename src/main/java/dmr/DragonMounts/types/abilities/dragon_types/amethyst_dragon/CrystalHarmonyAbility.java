package dmr.DragonMounts.types.abilities.dragon_types.amethyst_dragon;

import dmr.DragonMounts.registry.DragonAbilities;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber
public class CrystalHarmonyAbility implements Ability {

    @Override
    public String type() {
        return "crystal_harmony";
    }

    private static final double protection_chance = 0.1;

    @SubscribeEvent
    public static void entityHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof TameableDragonEntity dragon) {
            if (dragon.getBreed().getAbilities().contains(DragonAbilities.CRYSTAL_HARMONY)) {
                if (dragon.getRandom().nextDouble() <= protection_chance) {
                    event.setNewDamage(0);
                }
            }
        } else if (event.getEntity() instanceof Player player) {
            if (player.getVehicle() instanceof TameableDragonEntity dragon) {
                if (dragon.getBreed().getAbilities().contains(DragonAbilities.CRYSTAL_HARMONY)) {
                    if (dragon.getRandom().nextDouble() <= protection_chance) {
                        event.setNewDamage(0);
                    }
                }
            }
        }
    }
}
