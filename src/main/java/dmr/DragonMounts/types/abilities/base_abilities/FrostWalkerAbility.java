package dmr.DragonMounts.types.abilities.base_abilities;

import dmr.DragonMounts.server.entity.TameableDragonEntity;
import dmr.DragonMounts.types.abilities.Ability;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.pathfinder.PathType;

public class FrostWalkerAbility extends Ability {
    public FrostWalkerAbility(String abilityType) {
        super(abilityType);
    }

    @Override
    public void onInitialize(TameableDragonEntity dragon) {
        dragon.setPathfindingMalus(PathType.WATER, 0);
    }

    @Override
    public void onMove(TameableDragonEntity dragon) {
        if (!dragon.level.isClientSide() && dragon.isAdult()) {
            HolderLookup.RegistryLookup<Enchantment> registrylookup =
                    dragon.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var enchant = registrylookup.getOrThrow(Enchantments.FROST_WALKER);
            enchant.value()
                    .runLocationChangedEffects(
                            (ServerLevel) dragon.level,
                            (int) Math.max(3 * dragon.getScale(), 1),
                            new EnchantedItemInUse(ItemStack.EMPTY, null, null, i -> {}),
                            dragon);
        }
    }

    @Override
    public void tick(TameableDragonEntity dragon) {
        if (!dragon.level.isClientSide() && dragon.isAdult()) {
            HolderLookup.RegistryLookup<Enchantment> registrylookup =
                    dragon.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var enchant = registrylookup.getOrThrow(Enchantments.FROST_WALKER);
            enchant.value()
                    .runLocationChangedEffects(
                            (ServerLevel) dragon.level,
                            (int) Math.max(3 * dragon.getScale(), 1),
                            new EnchantedItemInUse(ItemStack.EMPTY, null, null, i -> {}),
                            dragon);
        }
    }
}
