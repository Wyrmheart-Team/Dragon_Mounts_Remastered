package dmr.DragonMounts.server.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dmr.DragonMounts.registry.ModCriterionTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.Optional;

public class HatchTrigger extends SimpleCriterionTrigger<HatchTrigger.HatchTriggerInstance> {

	public static Criterion<HatchTriggerInstance> instance(ContextAwarePredicate player, String id) {
		return ModCriterionTriggers.HATCH_TRIGGER.get().createCriterion(new HatchTriggerInstance(Optional.of(player), id));
	}

	@Override
	public Codec<HatchTriggerInstance> codec() {
		return HatchTriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player, String id) {
		this.trigger(player, instance -> instance.matches(player, id));
	}

	public record HatchTriggerInstance(Optional<ContextAwarePredicate> player, String id) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<HatchTriggerInstance> CODEC = RecordCodecBuilder.create(inst ->
			inst
				.group(
					EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(HatchTriggerInstance::player),
					Codec.STRING.fieldOf("id").forGetter(HatchTriggerInstance::id)
				)
				.apply(inst, HatchTriggerInstance::new)
		);

		public static HatchTriggerInstance test(Optional<ContextAwarePredicate> playerPredicate, String id) {
			return new HatchTriggerInstance(playerPredicate, id);
		}

		public static HatchTriggerInstance test(String id) {
			return test(Optional.empty(), id);
		}

		public boolean matches(ServerPlayer player, String id) {
			return Objects.equals(id, this.id);
		}

		@Override
		public Optional<ContextAwarePredicate> player() {
			return player;
		}
	}
}
