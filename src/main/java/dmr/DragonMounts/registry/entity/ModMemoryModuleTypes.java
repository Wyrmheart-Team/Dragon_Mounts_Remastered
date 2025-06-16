package dmr.DragonMounts.registry.entity;

import com.mojang.serialization.Codec;
import dmr.DragonMounts.DMR;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMemoryModuleTypes {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPE =
            DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, DMR.MOD_ID);
    public static final Supplier<MemoryModuleType<Boolean>> SHOULD_SIT = register("should_sit", Codec.BOOL);
    public static final Supplier<MemoryModuleType<Boolean>> SHOULD_WANDER = register("should_wander", Codec.BOOL);
    public static final Supplier<MemoryModuleType<Boolean>> HAS_BREATH_COOLDOWN =
            register("has_breath_cooldown", Codec.BOOL);
    public static final Supplier<MemoryModuleType<Integer>> IDLE_TICKS = register("idle_ticks", Codec.INT);

    private static <U> Supplier<MemoryModuleType<U>> register(String key, Codec<U> codec) {
        return MEMORY_MODULE_TYPE.register(key, () -> new MemoryModuleType<>(Optional.of(codec)));
    }
}
