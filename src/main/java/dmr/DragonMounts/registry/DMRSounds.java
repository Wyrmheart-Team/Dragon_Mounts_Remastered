package dmr.DragonMounts.registry;

import dmr.DragonMounts.DragonMountsRemaster;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DMRSounds
{
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, DragonMountsRemaster.MOD_ID);
	public static final Supplier<SoundEvent> DRAGON_WHISTLE_SOUND = SOUNDS.register("entity.dragon.whistle", () -> SoundEvent.createVariableRangeEvent(DragonMountsRemaster.id("entity.dragon.whistle")));
	public static final Supplier<SoundEvent> GHOST_DRAGON_AMBIENT = SOUNDS.register("entity.dragon.ambient.ghost", () -> SoundEvent.createVariableRangeEvent(DragonMountsRemaster.id("entity.dragon.ambient.ghost")));
	public static final Supplier<SoundEvent> DRAGON_DEATH_SOUND = SOUNDS.register("entity.dragon.death", () -> SoundEvent.createVariableRangeEvent(DragonMountsRemaster.id("entity.dragon.death")));
	public static final Supplier<SoundEvent> DRAGON_STEP_SOUND = SOUNDS.register("entity.dragon.step", () -> SoundEvent.createVariableRangeEvent(DragonMountsRemaster.id("entity.dragon.step")));
	public static final Supplier<SoundEvent> DRAGON_AMBIENT_SOUND = SOUNDS.register("entity.dragon.ambient", () -> SoundEvent.createVariableRangeEvent(DragonMountsRemaster.id("entity.dragon.ambient")));
}
