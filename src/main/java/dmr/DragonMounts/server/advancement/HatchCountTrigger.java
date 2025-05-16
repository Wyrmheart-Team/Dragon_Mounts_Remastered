package dmr.DragonMounts.server.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dmr.DragonMounts.registry.ModCriterionTriggers;
import dmr.DragonMounts.server.advancement.HatchCountTrigger.HatchCountTriggerInstance;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class HatchCountTrigger extends SimpleCriterionTrigger<HatchCountTriggerInstance> {

    public static Criterion<HatchCountTriggerInstance> instance(ContextAwarePredicate player, int count) {
        return ModCriterionTriggers.HATCH_COUNT_TRIGGER
                .get()
                .createCriterion(new HatchCountTriggerInstance(Optional.of(player), count));
    }

    @Override
    public Codec<HatchCountTriggerInstance> codec() {
        return HatchCountTriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, int count) {
        this.trigger(player, instance -> instance.matches(player, count));
    }

    public record HatchCountTriggerInstance(Optional<ContextAwarePredicate> player, int count)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<HatchCountTriggerInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                        EntityPredicate.ADVANCEMENT_CODEC
                                .optionalFieldOf("player")
                                .forGetter(HatchCountTriggerInstance::player),
                        Codec.INT.fieldOf("count").forGetter(HatchCountTriggerInstance::count))
                .apply(inst, HatchCountTriggerInstance::new));

        public static HatchCountTriggerInstance test(Optional<ContextAwarePredicate> playerPredicate, int count) {
            return new HatchCountTriggerInstance(playerPredicate, count);
        }

        public static HatchCountTriggerInstance test(int count) {
            return test(Optional.empty(), count);
        }

        public boolean matches(ServerPlayer player, int count) {
            return count >= this.count;
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return player;
        }
    }
}
