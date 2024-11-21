package dmr.DragonMounts.types.abilities.dragon_types.ice_dragon;

import dmr.DragonMounts.server.entity.DMRDragonEntity;
import dmr.DragonMounts.types.abilities.types.Ability;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.pathfinder.PathType;

public class FrostWalkerAbility implements Ability
{
	@Override
	public void initialize(DMRDragonEntity dragon)
	{
		dragon.setPathfindingMalus(PathType.WATER, 0);
	}
	
	@Override
	public void close(DMRDragonEntity dragon)
	{
		dragon.setPathfindingMalus(PathType.WATER, PathType.WATER.getMalus());
	}
	
	@Override
	public void onMove(DMRDragonEntity dragon)
	{
		if (!dragon.level.isClientSide() && dragon.isAdult()) {
			HolderLookup.RegistryLookup<Enchantment> registrylookup = dragon.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			var enchant = registrylookup.getOrThrow(Enchantments.FROST_WALKER);
			enchant.value().runLocationChangedEffects((ServerLevel)dragon.level, (int)Math.max(3 * dragon.getScale(), 1), new EnchantedItemInUse(ItemStack.EMPTY, null, null, (i) -> {}), dragon);
		}
	}
	
	@Override
	public void tick(DMRDragonEntity dragon)
	{
		if (!dragon.level.isClientSide() && dragon.isAdult()) {
			HolderLookup.RegistryLookup<Enchantment> registrylookup = dragon.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			var enchant = registrylookup.getOrThrow(Enchantments.FROST_WALKER);
			enchant.value().runLocationChangedEffects((ServerLevel)dragon.level, (int)Math.max(3 * dragon.getScale(), 1), new EnchantedItemInUse(ItemStack.EMPTY, null, null, (i) -> {}), dragon);
		}
	}
	
	@Override
	public String type()
	{
		return "frost_walker";
	}
}
