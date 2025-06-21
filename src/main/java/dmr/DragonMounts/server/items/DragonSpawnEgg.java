package dmr.DragonMounts.server.items;

import dmr.DragonMounts.ModConstants;
import dmr.DragonMounts.ModConstants.NBTConstants;
import dmr.DragonMounts.registry.ModComponents;
import dmr.DragonMounts.registry.datapack.DragonBreedsRegistry;
import dmr.DragonMounts.registry.entity.ModEntities;
import dmr.DragonMounts.registry.item.ModItems;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.types.dragonBreeds.DragonVariant;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

public class DragonSpawnEgg extends DeferredSpawnEggItem {

    public DragonSpawnEgg() {
        super(ModEntities.DRAGON_ENTITY, 0, 0, new Item.Properties());
    }

    public static final String DATA_ITEM_NAME = "ItemName";
    public static final String DATA_PRIM_COLOR = "PrimaryColor";
    public static final String DATA_SEC_COLOR = "SecondaryColor";

    public static ItemStack create(DragonBreed breed) {
        return create(breed, null);
    }

    public static ItemStack create(DragonBreed breed, DragonVariant variant) {
        var id = breed.getId();

        ItemStack stack = new ItemStack(ModItems.DRAGON_SPAWN_EGG.get());

        CompoundTag entityTag = new CompoundTag();

        if (variant != null) {
            entityTag.putString(NBTConstants.VARIANT, variant.id());
        }

        entityTag.putString(NBTConstants.BREED, id);

        entityTag.putString("id", "dmr:dragon");

        var itemDataTag = new CompoundTag();

        if (variant != null) {
            itemDataTag.putString(
                    DATA_ITEM_NAME,
                    String.join(
                            ".",
                            ModItems.DRAGON_SPAWN_EGG.get().getDescriptionId(),
                            id + ModConstants.VARIANT_DIVIDER + variant.id()));
            itemDataTag.putInt(DATA_PRIM_COLOR, variant.getPrimaryColor());
            itemDataTag.putInt(DATA_SEC_COLOR, variant.getSecondaryColor());
        } else {
            itemDataTag.putString(
                    DATA_ITEM_NAME,
                    String.join(".", ModItems.DRAGON_SPAWN_EGG.get().getDescriptionId(), id));
            itemDataTag.putInt(DATA_PRIM_COLOR, breed.getPrimaryColor());
            itemDataTag.putInt(DATA_SEC_COLOR, breed.getSecondaryColor());
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(itemDataTag));
        stack.set(DataComponents.ENTITY_DATA, CustomData.of(entityTag));

        stack.set(ModComponents.DRAGON_BREED, id);
        stack.set(ModComponents.DRAGON_VARIANT, variant != null ? variant.id() : null);

        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        var customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        var tag = customData.copyTag();
        if (tag.contains(DATA_ITEM_NAME)) {
            return Component.translatable(tag.getString(DATA_ITEM_NAME));
        }

        return super.getName(stack);
    }

    public static int getColor(ItemStack stack, int tintIndex) {
        var customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();

        if (customData == CustomData.EMPTY || !tag.contains(DATA_PRIM_COLOR) || !tag.contains(DATA_SEC_COLOR)) {
            var breed = DragonBreedsRegistry.getDragonBreed(stack.get(ModComponents.DRAGON_BREED));
            return tintIndex == 0 ? breed.getPrimaryColor() : breed.getSecondaryColor();
        }

        return tintIndex == 0 ? tag.getInt(DATA_PRIM_COLOR) : tag.getInt(DATA_SEC_COLOR);
    }
}
