package dmr.DragonMounts.server.ai.behaviours;

import com.mojang.datafixers.util.Pair;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import joptsimple.internal.Strings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.Behavior.Status;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.GateBehavior.OrderPolicy;
import net.minecraft.world.entity.ai.behavior.GateBehavior.RunningPolicy;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

@SuppressWarnings("unchecked")
public class BehaviorWrapper<E extends LivingEntity> implements BehaviorControl<E> {

	private final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
	private final Predicate<E> shouldStart;
	private GateBehavior.OrderPolicy orderPolicy = GateBehavior.OrderPolicy.SHUFFLED;
	private GateBehavior.RunningPolicy runningPolicy = GateBehavior.RunningPolicy.RUN_ONE;
	public final ShufflingList<BehaviorControl<? super E>> behaviors = new ShufflingList<>();

	private Behavior.Status status = Behavior.Status.STOPPED;

	@SafeVarargs
	public BehaviorWrapper(
		Map<MemoryModuleType<?>, MemoryStatus> entryCondition,
		Predicate<E> shouldStart,
		GateBehavior.OrderPolicy orderPolicy,
		GateBehavior.RunningPolicy runningPolicy,
		Pair<BehaviorControl<? super E>, Integer>... behaviors
	) {
		this.shouldStart = shouldStart;
		this.orderPolicy = orderPolicy;
		this.runningPolicy = runningPolicy;
		this.entryCondition = entryCondition;
		Arrays.stream(behaviors).forEach(behavior -> this.behaviors.add(behavior.getFirst(), behavior.getSecond()));
	}

	@SafeVarargs
	public BehaviorWrapper(
		Map<MemoryModuleType<?>, MemoryStatus> entryCondition,
		Predicate<E> shouldStart,
		Pair<BehaviorControl<? super E>, Integer>... behaviors
	) {
		this.shouldStart = shouldStart;
		this.entryCondition = entryCondition;
		Arrays.stream(behaviors).forEach(behavior -> this.behaviors.add(behavior.getFirst(), behavior.getSecond()));
	}

	@SafeVarargs
	public BehaviorWrapper(Predicate<E> shouldStart, Pair<BehaviorControl<? super E>, Integer>... behaviors) {
		this(Map.of(), shouldStart, OrderPolicy.SHUFFLED, RunningPolicy.RUN_ONE, behaviors);
	}

	@SafeVarargs
	public BehaviorWrapper(Predicate<E> shouldStart, BehaviorControl<? super E>... behaviors) {
		this(Map.of(), shouldStart, OrderPolicy.SHUFFLED, RunningPolicy.RUN_ONE, convertToOrderedPairs(behaviors));
	}

	@SafeVarargs
	public BehaviorWrapper(Predicate<E> shouldStart, Function<DMRDragonEntity, Goal>... behaviors) {
		this(
			Map.of(),
			shouldStart,
			OrderPolicy.SHUFFLED,
			RunningPolicy.RUN_ONE,
			convertToOrderedPairs(Arrays.stream(behaviors).map(GoalWrapper::new).toArray(BehaviorControl[]::new))
		);
	}

	@SafeVarargs
	public BehaviorWrapper(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, BehaviorControl<? super E>... behaviors) {
		this(entryCondition, null, convertToPairs(behaviors));
	}

	@SafeVarargs
	public BehaviorWrapper(
		Predicate<E> shouldStart,
		Map<MemoryModuleType<?>, MemoryStatus> entryCondition,
		BehaviorControl<? super E>... behaviors
	) {
		this(entryCondition, shouldStart, convertToPairs(behaviors));
	}

	@SafeVarargs
	public BehaviorWrapper(
		Predicate<E> shouldStart,
		GateBehavior.OrderPolicy orderPolicy,
		GateBehavior.RunningPolicy runningPolicy,
		BehaviorControl<? super E>... behaviors
	) {
		this(null, shouldStart, orderPolicy, runningPolicy, convertToPairs(behaviors));
	}

	public BehaviorWrapper(Predicate<E> shouldStart) {
		this(null, shouldStart);
	}

	@SafeVarargs
	public BehaviorWrapper(BehaviorControl<? super E>... behaviors) {
		this(null, null, OrderPolicy.SHUFFLED, RunningPolicy.RUN_ONE, convertToPairs(behaviors));
	}

	private static <E extends LivingEntity> Pair<BehaviorControl<? super E>, Integer>[] convertToPairs(
		BehaviorControl<? super E>... behaviors
	) {
		return Stream.of(behaviors).<Pair<BehaviorControl<? super E>, Integer>>map(behavior -> Pair.of(behavior, 1)).toArray(Pair[]::new);
	}

	private static <E extends LivingEntity> Pair<BehaviorControl<? super E>, Integer>[] convertToOrderedPairs(
		BehaviorControl<? super E>... behaviors
	) {
		return IntStream.range(0, behaviors.length)
			.<Pair<BehaviorControl<? super E>, Integer>>mapToObj(i -> Pair.of(behaviors[i], i + 1))
			.toArray(Pair[]::new);
	}

	@Override
	public Status getStatus() {
		return status;
	}

	private boolean hasRequiredMemories(E entity) {
		for (Entry<MemoryModuleType<?>, MemoryStatus> entry : this.entryCondition.entrySet()) {
			MemoryModuleType<?> memorymoduletype = entry.getKey();
			MemoryStatus memorystatus = entry.getValue();
			if (!entity.getBrain().checkMemory(memorymoduletype, memorystatus)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean tryStart(ServerLevel level, E entity, long gameTime) {
		if (
			(this.entryCondition == null || this.hasRequiredMemories(entity)) && (this.shouldStart == null || this.shouldStart.test(entity))
		) {
			this.status = Behavior.Status.RUNNING;
			this.orderPolicy.apply(this.behaviors);
			this.runningPolicy.apply(this.behaviors.stream(), level, entity, gameTime);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void tickOrStop(ServerLevel level, E entity, long gameTime) {
		this.behaviors.stream()
			.filter(p_258342_ -> p_258342_.getStatus() == Behavior.Status.RUNNING)
			.forEach(p_258336_ -> p_258336_.tickOrStop(level, entity, gameTime));
		if (this.behaviors.stream().noneMatch(p_258344_ -> p_258344_.getStatus() == Behavior.Status.RUNNING)) {
			this.doStop(level, entity, gameTime);
		}
	}

	@Override
	public void doStop(ServerLevel level, E entity, long gameTime) {
		this.status = Behavior.Status.STOPPED;
		this.behaviors.stream()
			.filter(p_258337_ -> p_258337_.getStatus() == Behavior.Status.RUNNING)
			.forEach(p_258341_ -> p_258341_.doStop(level, entity, gameTime));
	}

	@Override
	public String debugString() {
		var set =
			this.behaviors.stream()
				.filter(p_258343_ -> p_258343_.getStatus() == Behavior.Status.RUNNING)
				.map(BehaviorControl::debugString)
				.toList();
		return "> " + Strings.join(set, ", ");
	}
}
