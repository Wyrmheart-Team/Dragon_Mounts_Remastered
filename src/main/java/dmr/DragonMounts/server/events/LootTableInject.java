package dmr.DragonMounts.server.events;

import dmr.DragonMounts.server.items.DragonEggItemBlock;
import dmr.DragonMounts.registry.DragonBreedsRegistry;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed.LootTableEntry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;


public class LootTableInject
{
	// Hacky method to add egg loot tables without having to run /reload
	// This is due to NeoForge running the LootTableLoadEvent event before the data pack is loaded
	public static void firstLoadInject(LevelAccessor level)
	{
		var server = level.getServer();
		
		if (server != null) {
			var lootData = server.getLootData();
			for (IDragonBreed breed : DragonBreedsRegistry.getDragonBreeds()) {
				if (breed.isHybrid()) continue;
				
				for (LootTableEntry entry : breed.getLootTable()) {
					var optionalTable = lootData.getElementOptional(LootDataType.TABLE, entry.getTable());
					if(optionalTable.isEmpty()) continue;
					
					var table = optionalTable.get();
					
					LootPool lootPool = injectLoot(breed, entry);
					
					if (lootPool.getName() != null) {
						table.removePool(lootPool.getName());
						table.addPool(lootPool);
					}
				}
			}
		}
	}
	
	public static LootPool injectLoot(IDragonBreed breed, LootTableEntry entry)
	{
		var stack = DragonEggItemBlock.getDragonEggStack(breed);
		var lootItemBuilder = LootItem.lootTableItem(stack.getItem()
		).apply(SetNbtFunction.setTag(stack.getTag()));
		
		var lootPoolBuilder = LootPool.lootPool()
				.when(LootItemRandomChanceCondition.randomChance(entry.getChance()))
				.add(lootItemBuilder).name(breed.getId() + "-egg");
		
		return lootPoolBuilder.build();
	}
	
	@SubscribeEvent
	public static void onLootLoad(LootTableLoadEvent evt)
	{
		for (IDragonBreed breed : DragonBreedsRegistry.getDragonBreeds()) {
			if (breed.isHybrid()) continue;
			
			for (LootTableEntry entry : breed.getLootTable()) {
				if (evt != null && evt.getName() != null) {
					if (evt.getName().equals(entry.getTable())) {
						evt.getTable().addPool(injectLoot(breed, entry));
					}
				}
			}
		}
	}
}
