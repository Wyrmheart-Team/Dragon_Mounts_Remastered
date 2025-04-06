package dmr.DragonMounts.network.packets;

import static dmr.DragonMounts.server.entity.AbstractDMRDragonEntity.AgroState.*;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.network.IMessage;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.server.items.DragonWhistleItem;
import dmr.DragonMounts.util.PlayerStateUtils;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DragonCommandPacket(int command) implements IMessage<DragonCommandPacket> {
	public DragonCommandPacket(Command command) {
		this(command.id);
	}

	public enum Command {
		SIT(0),
		FOLLOW(1),
		WANDER(2),
		WHISTLE(3),
		PASSIVE(4),
		NEUTRAL(5),
		AGGRESSIVE(6);

		public final int id;

		Command(int id) {
			this.id = id;
		}
	}

	public static final StreamCodec<FriendlyByteBuf, DragonCommandPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT,
		DragonCommandPacket::command,
		DragonCommandPacket::new
	);

	@Override
	public StreamCodec<? super RegistryFriendlyByteBuf, DragonCommandPacket> streamCodec() {
		return STREAM_CODEC;
	}

	public static final Type<DragonCommandPacket> TYPE = new Type<>(DMR.id("dragon_command"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public DragonCommandPacket decode(FriendlyByteBuf buffer) {
		return new DragonCommandPacket(buffer.readInt());
	}

	@Override
	public void handle(IPayloadContext context, Player player) {}

	@Override
	public void handleServer(IPayloadContext context, ServerPlayer player) {
		var heldItem = player.getMainHandItem();
		var level = (ServerLevel) player.level;

		if (heldItem.isEmpty() || !(heldItem.getItem() instanceof DragonWhistleItem whistleItem)) {
			return;
		}

		var index = whistleItem.getColor().getId();
		var state = PlayerStateUtils.getHandler(player);
		var instance = state.dragonInstances.get(index);
		if (instance == null) {
			return;
		}
		var dragonEntity = level.getEntity(instance.getEntityId());

		if (dragonEntity instanceof DMRDragonEntity dragon) {
			switch (Command.values()[command]) {
				case SIT -> {
					dragon.setWanderTarget(Optional.empty());
					dragon.setOrderedToSit(true);
					player.displayClientMessage(Component.translatable("dmr.command_mode.sit.text"), true);
				}
				case FOLLOW -> {
					dragon.setOrderedToSit(false);
					dragon.setWanderTarget(Optional.empty());
					player.displayClientMessage(Component.translatable("dmr.command_mode.follow.text"), true);
				}
				case WANDER -> {
					dragon.setOrderedToSit(false);
					dragon.setWanderTarget(Optional.of(GlobalPos.of(level.dimension(), player.blockPosition())));
					player.displayClientMessage(Component.translatable("dmr.command_mode.wander.text"), true);
				}
				case WHISTLE -> {
					DragonWhistleHandler.summonDragon(player);
					player.displayClientMessage(Component.translatable("dmr.command_mode.whistle.text"), true);
				}
				case PASSIVE -> {
					dragon.setAgroState(PASSIVE);
					player.displayClientMessage(Component.translatable("dmr.command_mode.passive.text"), true);
				}
				case NEUTRAL -> {
					dragon.setAgroState(NEUTRAL);
					player.displayClientMessage(Component.translatable("dmr.command_mode.neutral.text"), true);
				}
				case AGGRESSIVE -> {
					dragon.setAgroState(AGGRESSIVE);
					player.displayClientMessage(Component.translatable("dmr.command_mode.aggressive.text"), true);
				}
			}
		}
	}
}
