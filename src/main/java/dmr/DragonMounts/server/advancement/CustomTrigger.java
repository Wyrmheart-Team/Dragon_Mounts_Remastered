package dmr.DragonMounts.server.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.advancement.CustomTrigger.Instance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class CustomTrigger extends SimpleCriterionTrigger<Instance> {

	private final ResourceLocation triggerID;

	public CustomTrigger(String parString) {
		this(DMR.id(parString));
	}

	public CustomTrigger(ResourceLocation parRL) {
		super();
		triggerID = parRL;
	}

	public void trigger(ServerPlayer parPlayer) {
		this.trigger(parPlayer, Instance::test);
	}

	public Instance getInstance() {
		return new CustomTrigger.Instance(triggerID);
	}

	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public record Instance(ResourceLocation id) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst ->
			inst.group(ResourceLocation.CODEC.fieldOf("id").forGetter(Instance::id)).apply(inst, Instance::new)
		);

		public boolean test() {
			return true;
		}

		@Override
		public Optional<ContextAwarePredicate> player() {
			return Optional.empty();
		}
	}
}
