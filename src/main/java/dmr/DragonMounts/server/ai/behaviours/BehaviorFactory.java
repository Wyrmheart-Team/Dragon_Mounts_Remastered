package dmr.DragonMounts.server.ai.behaviours;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GateBehavior.OrderPolicy;
import net.minecraft.world.entity.ai.behavior.GateBehavior.RunningPolicy;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/**
 * Factory methods for common behavior wrapper patterns.
 * Simplifies the creation of behavior wrappers with various configurations.
 */
public class BehaviorFactory {
    /**
     * Creates a simple behavior with a condition
     * @param condition The condition to check
     * @param behaviors The behaviors to run
     * @return The configured behavior wrapper
     */
    @SafeVarargs
    public static <E extends LivingEntity> BehaviorWrapper<E> withCondition(
            Predicate<E> condition, BehaviorControl<? super E>... behaviors) {
        return new BehaviorWrapper<>(condition, behaviors);
    }

    /**
     * Creates a behavior with memory requirements
     * @param memoryRequirements The required memory states
     * @param behaviors The behaviors to run
     * @return The configured behavior wrapper
     */
    @SafeVarargs
    public static <E extends LivingEntity> BehaviorWrapper<E> withMemoryRequirements(
            Map<MemoryModuleType<?>, MemoryStatus> memoryRequirements, BehaviorControl<? super E>... behaviors) {
        return new BehaviorWrapper<>(memoryRequirements, behaviors);
    }

    /**
     * Creates a behavior with both condition and memory requirements
     * @param condition The condition to check
     * @param memoryRequirements The required memory states
     * @param behaviors The behaviors to run
     * @return The configured behavior wrapper
     */
    @SafeVarargs
    public static <E extends LivingEntity> BehaviorWrapper<E> withConditionAndMemory(
            Predicate<E> condition,
            Map<MemoryModuleType<?>, MemoryStatus> memoryRequirements,
            BehaviorControl<? super E>... behaviors) {
        return new BehaviorWrapper<>(condition, memoryRequirements, behaviors);
    }

    /**
     * Creates a behavior with custom execution policies
     * @param condition The condition to check
     * @param orderPolicy How behaviors should be ordered
     * @param runningPolicy How behaviors should be executed
     * @param behaviors The behaviors to run
     * @return The configured behavior wrapper
     */
    @SafeVarargs
    public static <E extends LivingEntity> BehaviorWrapper<E> withPolicies(
            Predicate<E> condition,
            OrderPolicy orderPolicy,
            RunningPolicy runningPolicy,
            BehaviorControl<? super E>... behaviors) {
        return new BehaviorWrapper<>(condition, orderPolicy, runningPolicy, behaviors);
    }

    /**
     * Creates a behavior wrapper for goal-based behaviors
     * @param condition The condition to check
     * @param goalFactories The goal factories to create goals
     * @return The configured behavior wrapper
     */
    @SafeVarargs
    public static <E extends LivingEntity> BehaviorWrapper<E> withGoals(
            Predicate<E> condition, Function<TameableDragonEntity, Goal>... goalFactories) {
        return new BehaviorWrapper<>(condition, goalFactories);
    }
}
