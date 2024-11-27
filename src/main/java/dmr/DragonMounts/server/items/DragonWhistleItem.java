package dmr.DragonMounts.server.items;

import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.network.packets.CompleteDataSync;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.server.entity.DMRDragonEntity;
import java.util.List;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;

public class DragonWhistleItem extends Item {

	@Getter
	private final DyeColor color;

	public DragonWhistleItem(Properties pProperties, DyeColor color) {
		super(pProperties);
		this.color = color;
	}

	public static ItemStack getWhistleItem(DyeColor color) {
		return getWhistleItem(color, 1);
	}

	public static ItemStack getWhistleItem(DyeColor color, int count) {
		return new ItemStack(ModItems.DRAGON_WHISTLES.get(color.getId()).get(), count);
	}

	@Override
	public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
		super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);

		if (pEntity instanceof Player player) {
			var state = player.getData(ModCapabilities.PLAYER_CAPABILITY);
			if (state.respawnDelays.containsKey(color.getId())) {
				if (!player.getCooldowns().isOnCooldown(this)) {
					player.getCooldowns().addCooldown(this, state.respawnDelays.get(color.getId()));
				}
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

		if (context.level() != null && context.level().isClientSide) {
			if (FMLLoader.getDist() == Dist.CLIENT) {
				clientSideTooltip(stack, tooltipComponents);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void clientSideTooltip(ItemStack pStack, List<Component> pTooltipComponents) {
		var player = Minecraft.getInstance().player;
		var state = player.getData(ModCapabilities.PLAYER_CAPABILITY);
		if (state.dragonUUIDs.containsKey(color.getId())) {
			var id = state.dragonUUIDs.get(color.getId());
			var dragon = DragonWhistleHandler.findDragon(player, id);
			var nbt = state.dragonNBTs.get(color.getId());

			if (nbt != null) {
				var breed = nbt.getString("breed");
				var dragonBreed = DragonBreedsRegistry.getDragonBreed(breed);

				if (dragon != null) {
					var breedName = Component.translatable("dmr.dragon_breed." + breed).getString();
					var name = dragon.getDisplayName().getString();

					if (!name.equals(breedName)) {
						name = name + " (" + breedName + ")";
					}

					pTooltipComponents.add(
						Component.translatable("dmr.dragon_summon.tooltip.1", name)
							.withStyle(ChatFormatting.GRAY)
							.withStyle(ChatFormatting.ITALIC)
					);
				} else {
					if (dragonBreed != null) {
						var name = Component.translatable("dmr.dragon_breed." + breed).getString();

						if (nbt.contains("CustomName")) {
							name = nbt.getString("CustomName").replace("\"", "") + " (" + name + ")";
						}

						pTooltipComponents.add(
							Component.translatable("dmr.dragon_summon.tooltip.1", name)
								.withStyle(ChatFormatting.GRAY)
								.withStyle(ChatFormatting.ITALIC)
						);
					}
				}

				if (state.respawnDelays.containsKey(color.getId()) && state.respawnDelays.get(color.getId()) > 0) {
					pTooltipComponents.add(
						Component.translatable("dmr.dragon_summon.tooltip.2", state.respawnDelays.get(color.getId()) / 20)
							.withStyle(ChatFormatting.ITALIC)
							.withStyle(ChatFormatting.RED)
					);
				}
			}
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
		if (!pPlayer.isShiftKeyDown()) {
			DragonWhistleHandler.summonDragon(pPlayer);
			return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
		}

		return super.use(pLevel, pPlayer, pUsedHand);
	}

	@Override
	public InteractionResult interactLivingEntity(
		ItemStack pStack,
		Player pPlayer,
		LivingEntity pInteractionTarget,
		InteractionHand pUsedHand
	) {
		if (pPlayer.level.isClientSide) return InteractionResult.PASS;

		if (!pPlayer.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}

		if (pInteractionTarget instanceof DMRDragonEntity dragon) {
			if (dragon.isTame() && dragon.isOwnedBy(pPlayer)) {
				DragonOwnerCapability cap = pPlayer.getData(ModCapabilities.PLAYER_CAPABILITY);
				if (cap.dragonUUIDs.containsKey(color.getId())) {
					if (!cap.dragonUUIDs.get(color.getId()).equals(dragon.getDragonUUID())) {
						pPlayer.displayClientMessage(Component.translatable("dmr.dragon_call.unlink_first"), true);
						return InteractionResult.SUCCESS;
					} else {
						cap.dragonUUIDs.remove(color.getId());
						cap.summonInstances.remove(color.getId());
						cap.dragonNBTs.remove(color.getId());
						cap.respawnDelays.remove(color.getId());
						pPlayer.displayClientMessage(Component.translatable("dmr.dragon_call.unlink_success"), true);
						PacketDistributor.sendToPlayer((ServerPlayer) pPlayer, new CompleteDataSync(pPlayer));
						return InteractionResult.SUCCESS;
					}
				} else {
					DragonWhistleHandler.setDragon(pPlayer, dragon, color.getId());
					PacketDistributor.sendToPlayer((ServerPlayer) pPlayer, new CompleteDataSync(pPlayer));
					pPlayer.displayClientMessage(
						Component.translatable("dmr.dragon_call.link_success", dragon.getDisplayName().getString()),
						true
					);
				}

				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}
}
