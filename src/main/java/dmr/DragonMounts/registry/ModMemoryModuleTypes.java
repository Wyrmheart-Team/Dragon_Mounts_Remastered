package dmr.DragonMounts.registry;

import com.mojang.serialization.Codec;
import dmr.DragonMounts.DMR;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMemoryModuleTypes {

	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPE = DeferredRegister.create(
		Registries.MEMORY_MODULE_TYPE,
		DMR.MOD_ID
	);
	public static final Supplier<MemoryModuleType<GlobalPos>> WANDER_TARGET = register("wander_target", GlobalPos.CODEC);
	public static final Supplier<MemoryModuleType<Boolean>> IS_SITTING = register("sitting", Codec.BOOL);
	public static final Supplier<MemoryModuleType<Boolean>> IS_TAMED = register("is_tamed", Codec.BOOL);
	public static final Supplier<MemoryModuleType<Boolean>> HAS_BREATH_COOLDOWN = register("has_breath_cooldown", Codec.BOOL);

	private static <U> Supplier<MemoryModuleType<U>> register(String key, Codec<U> codec) {
		return MEMORY_MODULE_TYPE.register(key, () -> new MemoryModuleType<>(Optional.of(codec)));
	}
}
