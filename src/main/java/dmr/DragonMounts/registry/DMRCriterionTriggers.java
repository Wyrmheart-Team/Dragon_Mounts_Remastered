package dmr.DragonMounts.registry;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.server.advancement.CustomTrigger;
import dmr.DragonMounts.server.advancement.HatchTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DMRCriterionTriggers {
	public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, DragonMountsRemaster.MOD_ID);
	
	public static final Supplier<CustomTrigger> HATCH_DRAGON_EGG = register("hatch_dragon_egg");
	public static final Supplier<CustomTrigger> HATCH_5_DRAGON_EGGS = register("hatch_5_dragon_eggs");
	public static final Supplier<CustomTrigger> HATCH_10_DRAGON_EGGS = register("hatch_10_dragon_eggs");
	public static final Supplier<CustomTrigger> HATCH_100_DRAGON_EGGS = register("hatch_100_dragon_eggs");
	
	public static final Supplier<CustomTrigger> TAME_DRAGON = register("tame_dragon");
	
	public static final Supplier<CustomTrigger> DEFEAT_WITH_DRAGON = register("defeat_with_dragon");
	
	public static final Supplier<HatchTrigger> HATCH_TRIGGER = CRITERION_TRIGGERS.register("hatch_dragon", HatchTrigger::new);
	public static final Supplier<CustomTrigger> IS_HYBRID_HATCH_TRIGGER = register("is_hybrid_hatch");
	
	public static Supplier<CustomTrigger> register(String name) {
		return CRITERION_TRIGGERS.register(name, () -> new CustomTrigger(name));
	}
}
