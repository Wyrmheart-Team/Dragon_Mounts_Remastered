package dmr.DragonMounts.mixins;

import dmr.DragonMounts.config.ServerConfig;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndDragonFight.class)
public class ReplenishDragonEggMixin {

    /**
     * Return the invert of whats wanted due to the target inverting the returned
     * value.
     */
    @Redirect(
            method = "setDragonKilled(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;)V",
            at =
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;previouslyKilled:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean dragonmounts_replenishDragonEgg(EndDragonFight instance) {
        return !(!instance.hasPreviouslyKilledDragon() || ServerConfig.REPLENISH_EGGS.get());
    }
}
