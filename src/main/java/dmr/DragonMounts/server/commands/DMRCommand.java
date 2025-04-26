package dmr.DragonMounts.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dmr.DragonMounts.network.packets.CompleteDataSync;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModEntities;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.server.worlddata.DragonWorldData.DragonHistory;
import dmr.DragonMounts.server.worlddata.DragonWorldDataManager;
import dmr.DragonMounts.util.PlayerStateUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DMRCommand {

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		var baseCommand = Commands.literal("dmr").requires(source -> source.hasPermission(2));
		var spawnRegular = baseCommand.then(
			Commands.literal("spawn").then(
				Commands.literal("regular").then(
					Commands.argument("breed", StringArgumentType.word())
						.suggests(DMRCommand::getBreeds)
						.executes(context ->
							spawnDragon(
								context.getSource(),
								context.getArgument("breed", String.class),
								context.getSource().getPosition(),
								new CompoundTag()
							)
						)
						.then(
							Commands.argument("pos", Vec3Argument.vec3())
								.executes(context ->
									spawnDragon(
										context.getSource(),
										context.getArgument("breed", String.class),
										Vec3Argument.getVec3(context, "pos"),
										new CompoundTag()
									)
								)
								.then(
									Commands.argument("nbt", StringArgumentType.greedyString()).executes(context ->
										spawnDragon(
											context.getSource(),
											context.getArgument("breed", String.class),
											Vec3Argument.getVec3(context, "pos"),
											CompoundTagArgument.getCompoundTag(context, "nbt")
										)
									)
								)
						)
				)
			)
		);

		var spawnHybrid = baseCommand.then(
			Commands.literal("spawn").then(
				Commands.literal("hybrid").then(
					Commands.argument("parent 1", StringArgumentType.word())
						.suggests(DMRCommand::getBreeds)
						.then(
							Commands.argument("parent 2", StringArgumentType.word())
								.suggests(DMRCommand::getBreeds)
								.executes(context ->
									spawnDragon(
										context.getSource(),
										String.format(
											"hybrid_%s_%s",
											context.getArgument("parent 1", String.class),
											context.getArgument("parent 2", String.class)
										),
										context.getSource().getPosition(),
										new CompoundTag()
									)
								)
								.then(
									Commands.argument("pos", Vec3Argument.vec3())
										.executes(context ->
											spawnDragon(
												context.getSource(),
												String.format(
													"hybrid_%s_%s",
													context.getArgument("parent 1", String.class),
													context.getArgument("parent 2", String.class)
												),
												Vec3Argument.getVec3(context, "pos"),
												new CompoundTag()
											)
										)
										.then(
											Commands.argument("nbt", StringArgumentType.greedyString()).executes(context ->
												spawnDragon(
													context.getSource(),
													String.format(
														"hybrid_%s_%s",
														context.getArgument("parent 1", String.class),
														context.getArgument("parent 2", String.class)
													),
													Vec3Argument.getVec3(context, "pos"),
													CompoundTagArgument.getCompoundTag(context, "nbt")
												)
											)
										)
								)
						)
				)
			)
		);

		var recall = baseCommand.then(
			Commands.literal("recall").then(
				Commands.argument("id", UuidArgument.uuid())
					.suggests((context, builder) -> {
						DragonWorldDataManager.getInstance(context.getSource().getLevel())
							.dragonHistory.values()
							.stream()
							.sorted(Comparator.comparing(DragonHistory::time))
							.forEach(v ->
								builder.suggest(
									v.id().toString(),
									Component.translatable(
										"dmr.commands.dragon_recall.suggest",
										v.dragonName().copy().withStyle(ChatFormatting.AQUA),
										Component.literal(v.playerName()).withStyle(ChatFormatting.GRAY),
										Component.literal(getTimeAgo(v.time())).withStyle(ChatFormatting.GRAY)
									)
								)
							);
						return builder.buildFuture();
					})
					.executes(context ->
						runRecall(context.getSource(), context.getArgument("id", UUID.class), context.getSource().getPosition())
					)
					.then(
						Commands.argument("pos", Vec3Argument.vec3()).executes(context ->
							runRecall(context.getSource(), context.getArgument("id", UUID.class), Vec3Argument.getVec3(context, "pos"))
						)
					)
			)
		);
		
		var clearWhistle = baseCommand.then(
			Commands.literal("clear_whistle").then(
				Commands.argument("color", StringArgumentType.string()).suggests((context, builder) -> {
					for(DyeColor color : DyeColor.values()) {
						builder.suggest(color.getName());
					}
					return builder.buildFuture();
				}).executes(ctx -> runClearWhistle(ctx.getSource(), DyeColor.byName(ctx.getArgument("color", String.class), DyeColor.WHITE)))
			)
		);

		commandDispatcher.register(spawnRegular);
		commandDispatcher.register(spawnHybrid);
		commandDispatcher.register(recall);
		commandDispatcher.register(clearWhistle);
	}

	private static String getTimeAgo(long timestampMs) {
		long diff = System.currentTimeMillis() - timestampMs;
		if (diff < 0) return "";
		if (diff < 60000) return diff / 1000 + "s";
		if (diff < 3600000) return diff / 60000 + "m";
		if (diff < 86400000) return diff / 3600000 + "h";
		return diff / 86400000 + "d";
	}

	private static int spawnDragon(CommandSourceStack source, String breedName, Vec3 position, CompoundTag nbt) {
		// Retrieve breed from the DragonBreedRegistry
		if (!DragonBreedsRegistry.hasDragonBreed(breedName)) {
			source.sendFailure(Component.translatable("dmr.commands.dragon_spawn.invalid_breed", breedName));
			return 0;
		}
		var breed = DragonBreedsRegistry.getDragonBreed(breedName);

		// Spawn the dragon (server-side logic)
		ServerLevel level = source.getLevel();

		var dragonEntity = ModEntities.DRAGON_ENTITY.get().create(level);
		if (dragonEntity instanceof DMRDragonEntity dragon) {
			dragon.load(nbt);
			dragon.setPos(position.x, position.y, position.z);
			dragon.setBreed(breed);
			level.addFreshEntity(dragon);
		}

		source.sendSuccess(() -> Component.translatable("dmr.commands.dragon_spawn.success", breed.getName()), true);
		return 1;
	}

	private static int runRecall(CommandSourceStack source, UUID id, Vec3 position) {
		DragonHistory history = DragonWorldDataManager.getDragonHistory(source.getLevel(), id);
		CompoundTag nbt = history != null ? history.compoundTag() : null;
		if (nbt != null) {
			ServerLevel level = source.getLevel();
			Optional<EntityType<?>> type = EntityType.by(nbt);

			if (type.isPresent()) {
				Entity entity = type.get().create(level);
				if (entity instanceof DMRDragonEntity dragon) {
					dragon.load(nbt);
					dragon.setUUID(id);
					dragon.setPos(position.x, position.y, position.z);
					dragon.setHealth(Math.max(1, dragon.getHealth()));
					level.addFreshEntity(dragon);
				}
			}

			source.sendSuccess(() -> Component.translatable("dmr.commands.dragon_recall.success", id.toString()), true);
		} else {
			source.sendFailure(Component.translatable("dmr.commands.dragon_recall.failure", id.toString()));
		}
		return 1;
	}
	
	private static int runClearWhistle(CommandSourceStack source, DyeColor color) {
		var handler = PlayerStateUtils.getHandler(source.getPlayer());
		handler.dragonNBTs.remove(color.getId());
		handler.dragonInstances.remove(color.getId());
		handler.respawnDelays.remove(color.getId());
		PacketDistributor.sendToPlayer(source.getPlayer(), new CompleteDataSync(source.getPlayer()));
		source.sendSuccess(() -> Component.translatable("dmr.commands.clear_whistle.success", color.getName()), true);
		return 1;
	}

	private static CompletableFuture<Suggestions> getBreeds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		DragonBreedsRegistry.getDragonBreeds()
			.stream()
			.filter(breed -> !breed.isHybrid())
			.forEach(breed -> builder.suggest(breed.getId(), breed.getName()));
		return builder.buildFuture();
	}
}
