package dmr.DragonMounts.registry;

import com.google.common.collect.ImmutableMap;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.server.advancement.HatchTrigger;
import dmr.DragonMounts.server.items.DragonEggItemBlock;
import dmr.DragonMounts.server.items.DragonSpawnEgg;
import dmr.DragonMounts.types.dragonBreeds.IDragonBreed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.advancements.*;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;

public class ModAdvancements {

    public static void init(ServerLevel level) {
        ServerAdvancementManager manager = level.getServer().getAdvancements();

        HashMap<ResourceLocation, AdvancementHolder> map = new HashMap<>();
        Consumer<AdvancementHolder> register = (AdvancementHolder holder) -> {
            map.put(holder.id(), holder);
            manager.tree().addAll(List.of(holder));
        };

        var origMap = new HashMap<>(manager.advancements);
        var main = manager.get(DMR.id("main"));

        List<ItemPredicate> hybridStacks = new ArrayList<>();

        for (IDragonBreed breed : DragonBreedsRegistry.getDragonBreeds()) {
            if (!breed.isHybrid() || breed.getLootTable().isEmpty()) {
                continue;
            }

            var predicate = ItemPredicate.Builder.item()
                    .of(ModItems.DRAGON_EGG_BLOCK_ITEM.get())
                    .hasComponents(DataComponentPredicate.builder()
                            .expect(ModComponents.DRAGON_BREED.get(), breed.getId())
                            .build())
                    .build();

            hybridStacks.add(predicate);
        }

        var findHybrid = Builder.advancement()
                .parent(main)
                .display(
                        DragonEggItemBlock.getDragonEggStack(DragonBreedsRegistry.getDragonBreed("amethyst")),
                        Component.translatable("dmr.advancements.find_hybrid_egg.title"),
                        Component.translatable("dmr.advancements.find_hybrid_egg.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false)
                .addCriterion(
                        "obtained_hybrid_egg", TriggerInstance.hasItems(hybridStacks.toArray(new ItemPredicate[0])))
                .build(DMR.id("find_hybrid"));

        origMap.put(DMR.id("find_hybrid"), findHybrid);

        for (IDragonBreed breed : DragonBreedsRegistry.getDragonBreeds()) {
            if (breed.isHybrid() || breed.getLootTable().isEmpty()) {
                continue;
            }

            var egg = Builder.advancement()
                    .parent(manager.get(DMR.id("find_egg")))
                    .display(
                            DragonEggItemBlock.getDragonEggStack(breed),
                            Component.translatable(
                                    "dmr.advancements.dragon_egg.title",
                                    Component.translatable("block.dmr.dragon_egg." + breed.getId())),
                            Component.translatable(
                                    "dmr.advancements.dragon_egg.description",
                                    Component.translatable("block.dmr.dragon_egg." + breed.getId())),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            true)
                    .addCriterion(
                            "obtained_dragon_egg",
                            TriggerInstance.hasItems(ItemPredicate.Builder.item()
                                    .of(ModItems.DRAGON_EGG_BLOCK_ITEM.get())
                                    .hasComponents(DataComponentPredicate.builder()
                                            .expect(ModComponents.DRAGON_BREED.get(), breed.getId())
                                            .build())
                                    .build()))
                    .build(DMR.id("dragon_egg_" + breed.getId()));

            var hatch = Builder.advancement()
                    .parent(egg)
                    .display(
                            DragonSpawnEgg.create(breed),
                            Component.translatable(
                                    "dmr.advancements.hatch_egg.title",
                                    Component.translatable("dmr.dragon_breed." + breed.getId())),
                            Component.translatable(
                                    "dmr.advancements.hatch_egg.description",
                                    Component.translatable("dmr.dragon_breed." + breed.getId())),
                            null,
                            AdvancementType.CHALLENGE,
                            true,
                            true,
                            false)
                    .addCriterion(
                            "hatch_dragon_egg",
                            new Criterion<>(
                                    ModCriterionTriggers.HATCH_TRIGGER.get(),
                                    HatchTrigger.HatchTriggerInstance.test(breed.getId())))
                    .build(DMR.id("hatch_egg_" + breed.getId()));

            register.accept(egg);
            register.accept(hatch);
        }

        var tempMap = new HashMap<>(origMap);
        map.keySet().forEach(tempMap::remove);
        tempMap.putAll(map);

        manager.advancements = ImmutableMap.<ResourceLocation, AdvancementHolder>builder()
                .putAll(tempMap)
                .build();

        for (AdvancementNode advancementnode : manager.tree().roots()) {
            if (advancementnode.holder().id().getNamespace().equals(DMR.MOD_ID)) {
                if (advancementnode.holder().value().display().isPresent()) {
                    TreeNodePosition.run(advancementnode);
                }
            }
        }
        for (PlayerAdvancements playeradvancements :
                level.getServer().getPlayerList().advancements.values()) {
            playeradvancements.reload(
                    level.getServer().getPlayerList().getServer().getAdvancements());
        }
    }
}
