package dmr.DragonMounts.registry;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.advancement.CustomTrigger;
import dmr.DragonMounts.server.advancement.HatchCountTrigger;
import dmr.DragonMounts.server.advancement.HatchTrigger;
import java.util.function.Supplier;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCriterionTriggers {

    public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, DMR.MOD_ID);

    public static final Supplier<CustomTrigger> TAME_DRAGON = register("tame_dragon");

    public static final Supplier<CustomTrigger> DEFEAT_WITH_DRAGON = register("defeat_with_dragon");

    public static final Supplier<HatchTrigger> HATCH_TRIGGER =
            CRITERION_TRIGGERS.register("hatch_dragon", HatchTrigger::new);
    public static final Supplier<HatchCountTrigger> HATCH_COUNT_TRIGGER =
            CRITERION_TRIGGERS.register("hatch_count", HatchCountTrigger::new);

    public static Supplier<CustomTrigger> register(String name) {
        return CRITERION_TRIGGERS.register(name, () -> new CustomTrigger(name));
    }
}
