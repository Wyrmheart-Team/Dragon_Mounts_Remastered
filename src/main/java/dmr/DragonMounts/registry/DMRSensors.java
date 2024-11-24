package dmr.DragonMounts.registry;

import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.server.ai.DragonAttackablesSensor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DMRSensors {
	public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(Registries.SENSOR_TYPE, DragonMountsRemaster.MOD_ID);
	public static final Supplier<SensorType<DragonAttackablesSensor>> DRAGON_ATTACKABLES = register("dragon_attackables", DragonAttackablesSensor::new);
	
	private static <U extends Sensor<?>> Supplier<SensorType<U>> register(String key, Supplier<U> sensorSupplier)
	{
		return SENSORS.register(key, () -> new SensorType<>(sensorSupplier));
	}
}
