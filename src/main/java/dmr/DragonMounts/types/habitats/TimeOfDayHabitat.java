package dmr.DragonMounts.types.habitats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record TimeOfDayHabitat(int points, boolean isDayTime) implements Habitat {
	public static final Codec<TimeOfDayHabitat> CODEC = RecordCodecBuilder.create(func ->
		func
			.group(
				Habitat.withPoints(1, TimeOfDayHabitat::points),
				Codec.BOOL.optionalFieldOf("is_day", true).forGetter(TimeOfDayHabitat::isDayTime)
			)
			.apply(func, TimeOfDayHabitat::new)
	);

	@Override
	public int getHabitatPoints(Level level, BlockPos pos) {
		return isDayTime() ? level.isDay() ? points : 0 : level.isNight() ? points : 0;
	}

	@Override
	public String type() {
		return Habitat.TIME_OF_DAY;
	}
}
