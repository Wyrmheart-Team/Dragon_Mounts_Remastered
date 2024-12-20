package dmr.DragonMounts.registry;

import dmr.DragonMounts.DMR;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModActivityTypes {

	public static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(Registries.ACTIVITY, DMR.MOD_ID);

	public static final Supplier<Activity> WANDER = register("wander");
	public static final Supplier<Activity> SIT = register("sit");

	private static <U> Supplier<Activity> register(String key) {
		return ACTIVITIES.register(key, () -> new Activity(key));
	}
}
