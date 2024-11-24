package dmr.DragonMounts.registry;

import com.google.common.collect.ImmutableMap;
import dmr.DragonMounts.DragonMountsRemaster;
import dmr.DragonMounts.server.advancement.HatchTrigger;
import dmr.DragonMounts.server.items.DragonEggItemBlock;
import dmr.DragonMounts.server.items.DragonSpawnEgg;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import net.minecraft.advancements.*;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class DMRAdvancements {
	public static void init(ServerLevel level)
	{
		ServerAdvancementManager manager = level.getServer().getAdvancements();
		
		HashMap<ResourceLocation, AdvancementHolder> map = new HashMap<>(); Consumer<AdvancementHolder> register = (AdvancementHolder holder) -> {
		map.put(holder.id(), holder); manager.tree().addAll(List.of(holder));
	};
		
		AdvancementHolder advancementholder = Advancement.Builder.advancement().display(DMRBlocks.DRAGON_EGG_BLOCK.get(),
				Component.translatable("dmr.advancements.root.title"),
				Component.translatable("dmr.advancements.root.description"),
				ResourceLocation.withDefaultNamespace("textures/block/amethyst_block.png"),
				AdvancementType.TASK,
				false,
				false,
				false
		).addCriterion("anything", PlayerTrigger.TriggerInstance.tick()).build(DragonMountsRemaster.id("advancements/main"));
		
		
		register.accept(advancementholder);
		
		AdvancementHolder findAnEgg = Builder.advancement().parent(advancementholder).display(Items.EGG,
				Component.translatable("dmr.advancements.find_egg.title"),
				Component.translatable("dmr.advancements.find_egg.description"),
				null,
				AdvancementType.TASK,
				false,
				false,
				false
		).addCriterion("obtained_dragon_egg", InventoryChangeTrigger.TriggerInstance.hasItems(DMRItems.DRAGON_EGG_BLOCK_ITEM.get())).build(DragonMountsRemaster.id("advancements/find_egg"));
		
		register.accept(findAnEgg);
		
		var hatch1 = Builder.advancement().parent(advancementholder).display(DMRItems.DRAGON_EGG_BLOCK_ITEM.get(),
						Component.translatable("dmr.advancements.hatch_egg_1.title"),
						Component.translatable("dmr.advancements.hatch_egg_1.description"),
						null,
						AdvancementType.TASK,
						true,
						true,
						false
				).addCriterion("hatch_dragon_egg", new Criterion<>(DMRCriterionTriggers.HATCH_DRAGON_EGG.get(), DMRCriterionTriggers.HATCH_DRAGON_EGG.get().getInstance()))
				.build(DragonMountsRemaster.id("advancements/hatch_egg_1"));
		
		var hatch5 = Builder.advancement().parent(hatch1).display(DMRItems.DRAGON_EGG_BLOCK_ITEM.get(),
						Component.translatable("dmr.advancements.hatch_egg_5.title"),
						Component.translatable("dmr.advancements.hatch_egg_5.description"),
						null,
						AdvancementType.TASK,
						true,
						true,
						false
				).addCriterion("hatch_5_dragon_eggs", new Criterion<>(DMRCriterionTriggers.HATCH_5_DRAGON_EGGS.get(), DMRCriterionTriggers.HATCH_5_DRAGON_EGGS.get().getInstance()))
				.build(DragonMountsRemaster.id("advancements/hatch_egg_5"));
		
		var hatch10 = Builder.advancement().parent(hatch5).display(DMRItems.DRAGON_EGG_BLOCK_ITEM.get(),
						Component.translatable("dmr.advancements.hatch_egg_10.title"),
						Component.translatable("dmr.advancements.hatch_egg_10.description"),
						null,
						AdvancementType.TASK,
						true,
						true,
						false
				).addCriterion("hatch_10_dragon_eggs", new Criterion<>(DMRCriterionTriggers.HATCH_10_DRAGON_EGGS.get(), DMRCriterionTriggers.HATCH_10_DRAGON_EGGS.get().getInstance()))
				.build(DragonMountsRemaster.id("advancements/hatch_egg_10"));
		
		var hatch100 = Builder.advancement().parent(hatch10).display(DMRItems.DRAGON_EGG_BLOCK_ITEM.get(),
						Component.translatable("dmr.advancements.hatch_egg_100.title"),
						Component.translatable("dmr.advancements.hatch_egg_100.description"),
						null,
						AdvancementType.CHALLENGE,
						true,
						true,
						false
				).addCriterion("hatch_100_dragon_eggs", new Criterion<>(DMRCriterionTriggers.HATCH_100_DRAGON_EGGS.get(), DMRCriterionTriggers.HATCH_100_DRAGON_EGGS.get().getInstance()))
				.build(DragonMountsRemaster.id("advancements/hatch_egg_100"));
		
		register.accept(hatch1); register.accept(hatch5); register.accept(hatch10); register.accept(hatch100);
		
		
		List<ItemPredicate> hybridStacks = new ArrayList<>();
		
		for (IDragonBreed breed : DragonBreedsRegistry.getDragonBreeds()) {
			if (!breed.isHybrid() || breed.getLootTable().isEmpty()) {
				continue;
			}
			
			var predicate = ItemPredicate.Builder.item().of(DMRItems.DRAGON_EGG_BLOCK_ITEM.get())
					.hasComponents(DataComponentPredicate.builder().expect(DMRComponents.DRAGON_BREED.get(), breed.getId()).build()).build();
			
			hybridStacks.add(predicate);
		}
		
		var findHybrid = Builder.advancement().parent(advancementholder).display(DragonEggItemBlock.getDragonEggStack(DragonBreedsRegistry.getDragonBreed("amethyst")),
				Component.translatable("dmr.advancements.find_hybrid_egg.title"),
				Component.translatable("dmr.advancements.find_hybrid_egg.description"),
				null,
				AdvancementType.TASK,
				true,
				true,
				false
		).addCriterion("obtained_hybrid_egg", TriggerInstance.hasItems(hybridStacks.toArray(new ItemPredicate[0]))).build(DragonMountsRemaster.id("advancements/find_hybrid"));
		
		register.accept(findHybrid);
		
		var hatchHybrid = Builder.advancement().parent(findHybrid)
				.display(DragonSpawnEgg.create(DragonBreedsRegistry.getHybridBreed(DragonBreedsRegistry.getDragonBreed("amethyst"), DragonBreedsRegistry.getDragonBreed("fire"))),
						Component.translatable("dmr.advancements.hatch_hybrid.title"),
						Component.translatable("dmr.advancements.hatch_hybrid.description"),
						null,
						AdvancementType.CHALLENGE,
						true,
						true,
						false
				).addCriterion("hatch_dragon_egg", new Criterion<>(DMRCriterionTriggers.IS_HYBRID_HATCH_TRIGGER.get(), DMRCriterionTriggers.IS_HYBRID_HATCH_TRIGGER.get().getInstance()))
				.build(DragonMountsRemaster.id("advancements/hatch_hybrid"));
		
		
		register.accept(hatchHybrid);
		
		var tame = Builder.advancement().parent(advancementholder).display(Items.TROPICAL_FISH,
						Component.translatable("dmr.advancements.tame_dragon_1.title"),
						Component.translatable("dmr.advancements.tame_dragon_1.description"),
						null,
						AdvancementType.CHALLENGE,
						true,
						true,
						false
				).addCriterion("tame_dragon", new Criterion<>(DMRCriterionTriggers.TAME_DRAGON.get(), DMRCriterionTriggers.TAME_DRAGON.get().getInstance()))
				.build(DragonMountsRemaster.id("advancements/tame_dragon_1"));
		
		register.accept(tame);
		
		var defeatWithDragon = Builder.advancement().parent(advancementholder).display(Items.IRON_SWORD,
						Component.translatable("dmr.advancements.defeat_with_dragon.title"),
						Component.translatable("dmr.advancements.defeat_with_dragon.description"),
						null,
						AdvancementType.CHALLENGE,
						true,
						true,
						false
				).addCriterion("defeat_with_dragon", new Criterion<>(DMRCriterionTriggers.DEFEAT_WITH_DRAGON.get(), DMRCriterionTriggers.DEFEAT_WITH_DRAGON.get().getInstance()))
				.build(DragonMountsRemaster.id("advancements/defeat_with_dragon"));
		
		register.accept(defeatWithDragon);
		
		
		for (IDragonBreed breed : DragonBreedsRegistry.getDragonBreeds()) {
			if (breed.isHybrid() || breed.getLootTable().isEmpty()) {
				continue;
			}
			
			
			var egg = Builder.advancement().parent(findAnEgg).display(DragonEggItemBlock.getDragonEggStack(breed),
					Component.translatable("dmr.advancements.dragon_egg.title", I18n.get("block.dmr.dragon_egg." + breed.getId())),
					Component.translatable("dmr.advancements.dragon_egg.description", I18n.get("block.dmr.dragon_egg." + breed.getId())),
					null,
					AdvancementType.TASK,
					true,
					true,
					true
			).addCriterion("obtained_dragon_egg",
					TriggerInstance.hasItems(ItemPredicate.Builder.item().of(DMRItems.DRAGON_EGG_BLOCK_ITEM.get())
							.hasComponents(DataComponentPredicate.builder().expect(DMRComponents.DRAGON_BREED.get(), breed.getId()).build()).build())
			).build(DragonMountsRemaster.id("advancements/dragon_egg_" + breed.getId()));
			
			var hatch = Builder.advancement().parent(egg).display(DragonSpawnEgg.create(breed),
							Component.translatable("dmr.advancements.hatch_egg.title", I18n.get("dmr.dragon_breed." + breed.getId())),
							Component.translatable("dmr.advancements.hatch_egg.description", I18n.get("dmr.dragon_breed." + breed.getId())),
							null,
							AdvancementType.CHALLENGE,
							true,
							true,
							false
					).addCriterion("hatch_dragon_egg", new Criterion<>(DMRCriterionTriggers.HATCH_TRIGGER.get(), HatchTrigger.HatchTriggerInstance.test(breed.getId())))
					.build(DragonMountsRemaster.id("advancements/hatch_egg_" + breed.getId()));
			
			register.accept(egg); register.accept(hatch);
		}
		
		
		manager.advancements = ImmutableMap.<ResourceLocation, AdvancementHolder>builder().putAll(level.getServer().getAdvancements().advancements).putAll(map).build();
		
		for (AdvancementNode advancementnode : manager.tree().roots()) {
			if (advancementnode.holder().id().getNamespace().equals(DragonMountsRemaster.MOD_ID)) {
				if (advancementnode.holder().value().display().isPresent()) {
					TreeNodePosition.run(advancementnode);
				}
			}
		}
		
		level.getServer().getPlayerList().reloadResources();
	}
}
