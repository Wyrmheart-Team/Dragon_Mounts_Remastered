package dmr.DragonMounts.server.items.dev;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.server.items.DMRDevItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Node;

public class DragonPathHighligther extends DMRDevItem
{
	@Override
	public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected)
	{
		if (pIsSelected && pEntity instanceof Player pPlayer) {
			if (!pLevel.isClientSide && pPlayer.tickCount % 5 == 0) {
				var dragons = pPlayer.level.getNearbyEntities(DMRDragonEntity.class, TargetingConditions.forNonCombat().range(128).ignoreLineOfSight(), pPlayer, pPlayer.getBoundingBox().inflate(32));
				
				var armorStands = pPlayer.level.getEntitiesOfClass(ArmorStand.class, pPlayer.getBoundingBox().inflate(128));
				armorStands.forEach(stand -> {
					if (stand.getCustomName().getString().equals("PathHighlighter")) stand.kill();
				});
				
				for (DMRDragonEntity dragon : dragons) {
					if (!dragon.getNavigation().isDone()) {
						var path = dragon.getNavigation().getPath();
						var nodes = path.getNodeCount();
						
						for (int i = 0; i < dragon.getNavigation().getPath().getNextNodeIndex(); i++) {
							var node = path.getNode(i);
							addArmorStand(pPlayer, node, Blocks.GRAY_WOOL);
						}
						
						for (int i = dragon.getNavigation().getPath().getNextNodeIndex(); i < nodes - 1; i++) {
							var node = path.getNode(i);
							var blockPos = node.asBlockPos();
							var isAir = dragon.level.getBlockState(blockPos.below(2)).entityCanStandOn(dragon.level, blockPos.below(2), dragon);
							addArmorStand(pPlayer, node, isAir ? Blocks.WHITE_WOOL : Blocks.BLUE_WOOL);
						}
						
						var endNode = path.getEndNode();
						
						if (endNode != null) {
							addArmorStand(pPlayer, endNode, Blocks.EMERALD_BLOCK);
						}
					}
				}
			}
		}
	}
	
	private void addArmorStand(Player pPlayer, Node node, ItemLike item)
	{
		ArmorStand armorStand = new ArmorStand(pPlayer.level, node.x, node.y - 1, node.z);
		
		armorStand.setCustomName(Component.literal("PathHighlighter"));
		
		armorStand.setNoGravity(true);
		armorStand.setInvisible(true);
		
		armorStand.setXRot(0);
		armorStand.setYRot(0);
		
		armorStand.setYHeadRot(0);
		armorStand.setYBodyRot(0);
		armorStand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(item));
		pPlayer.level.addFreshEntity(armorStand);
	}
}
