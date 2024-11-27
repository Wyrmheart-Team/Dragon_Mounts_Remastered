package dmr.DragonMounts.registry;

import dmr.DragonMounts.DMR;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {

	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, DMR.MOD_ID);
	public static final Supplier<SoundEvent> DRAGON_WHISTLE_SOUND = SOUNDS.register("entity.dragon.whistle", () ->
		SoundEvent.createVariableRangeEvent(DMR.id("entity.dragon.whistle"))
	);
	public static final Supplier<SoundEvent> GHOST_DRAGON_AMBIENT = SOUNDS.register("entity.dragon.ambient.ghost", () ->
		SoundEvent.createVariableRangeEvent(DMR.id("entity.dragon.ambient.ghost"))
	);
	public static final Supplier<SoundEvent> DRAGON_DEATH_SOUND = SOUNDS.register("entity.dragon.death", () ->
		SoundEvent.createVariableRangeEvent(DMR.id("entity.dragon.death"))
	);
	public static final Supplier<SoundEvent> DRAGON_STEP_SOUND = SOUNDS.register("entity.dragon.step", () ->
		SoundEvent.createVariableRangeEvent(DMR.id("entity.dragon.step"))
	);
	public static final Supplier<SoundEvent> DRAGON_AMBIENT_SOUND = SOUNDS.register("entity.dragon.ambient", () ->
		SoundEvent.createVariableRangeEvent(DMR.id("entity.dragon.ambient"))
	);
}
