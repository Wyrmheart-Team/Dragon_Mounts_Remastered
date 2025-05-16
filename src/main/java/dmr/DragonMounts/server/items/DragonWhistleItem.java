package dmr.DragonMounts.server.items;

import dmr.DragonMounts.client.gui.CommandMenu.CommandMenuScreen;
import dmr.DragonMounts.client.handlers.CommandOverlayHandler;
import dmr.DragonMounts.common.capability.DragonOwnerCapability;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.network.packets.CompleteDataSync;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.registry.ModCapabilities;
import dmr.DragonMounts.registry.ModItems;
import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.util.PlayerStateUtils;
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
import net.neoforged.neoforge.network.PacketDistributor;

@Getter
public class DragonWhistleItem extends Item {

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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(
            ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        var player = Minecraft.getInstance().player;
        assert player != null;
        var state = PlayerStateUtils.getHandler(player);
        var nbt = state.dragonNBTs.get(color.getId());

        if (nbt == null) {
            return;
        }

        var breed = nbt.getString("breed");
        var dragonBreed = DragonBreedsRegistry.getDragonBreed(breed);

        if (dragonBreed != null) {
            var name = Component.translatable("dmr.dragon_breed." + breed).getString();

            if (nbt.contains("CustomName")) {
                name = nbt.getString("CustomName").replace("\"", "") + " (" + name + ")";
            }

            tooltipComponents.add(Component.translatable("dmr.dragon_summon.tooltip.1", name)
                    .withStyle(ChatFormatting.GRAY)
                    .withStyle(ChatFormatting.ITALIC));
        }

        if (state.respawnDelays.containsKey(color.getId()) && state.respawnDelays.get(color.getId()) > 0) {
            tooltipComponents.add(
                    Component.translatable("dmr.dragon_summon.tooltip.2", state.respawnDelays.get(color.getId()) / 20)
                            .withStyle(ChatFormatting.ITALIC)
                            .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pPlayer.isShiftKeyDown()) {
            var state = PlayerStateUtils.getHandler(pPlayer);
            var nbt = state.dragonNBTs.get(color.getId());
            if (nbt == null) {
                if (!pPlayer.level.isClientSide) {
                    pPlayer.displayClientMessage(
                            Component.translatable("dmr.dragon_call.nodragon").withStyle(ChatFormatting.RED), true);
                }
                return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
            }
            if (pPlayer.level.isClientSide) {
                CommandOverlayHandler.resetTimer();
                CommandMenuScreen.activate();
            }
            return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (pPlayer.level.isClientSide) return InteractionResult.PASS;

        if (pInteractionTarget instanceof TameableDragonEntity dragon) {
            if (dragon.isTame() && dragon.isAdult() && dragon.isOwnedBy(pPlayer)) {
                DragonOwnerCapability cap = pPlayer.getData(ModCapabilities.PLAYER_CAPABILITY);
                if (cap.dragonInstances.containsKey(color.getId())) {
                    var dragonInstance = cap.dragonInstances.get(color.getId());
                    // Only unlink if the player is sneaking
                    if (!pPlayer.isShiftKeyDown()) {
                        return InteractionResult.PASS;
                    }

                    if (!dragonInstance.getUUID().equals(dragon.getDragonUUID())) {
                        pPlayer.displayClientMessage(Component.translatable("dmr.dragon_call.unlink_first"), true);
                    } else {
                        cap.dragonInstances.remove(color.getId());
                        cap.dragonNBTs.remove(color.getId());
                        cap.respawnDelays.remove(color.getId());
                        pPlayer.displayClientMessage(Component.translatable("dmr.dragon_call.unlink_success"), true);
                        PacketDistributor.sendToPlayer((ServerPlayer) pPlayer, new CompleteDataSync(pPlayer));
                    }
                    return InteractionResult.SUCCESS;
                } else {
                    DragonWhistleHandler.setDragon(pPlayer, dragon, color.getId());
                    PacketDistributor.sendToPlayer((ServerPlayer) pPlayer, new CompleteDataSync(pPlayer));
                    pPlayer.displayClientMessage(
                            Component.translatable(
                                    "dmr.dragon_call.link_success",
                                    dragon.getDisplayName().getString()),
                            true);
                }

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
