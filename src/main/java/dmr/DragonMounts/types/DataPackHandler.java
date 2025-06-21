package dmr.DragonMounts.types;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.registry.*;
import dmr.DragonMounts.registry.datapack.*;
import dmr.DragonMounts.server.events.LootTableInject;
import dmr.DragonMounts.types.abilities.DragonAbility;
import dmr.DragonMounts.types.abilities.DragonAbilityTag;
import dmr.DragonMounts.types.armor.DragonArmor;
import dmr.DragonMounts.types.breath.DragonBreathType;
import dmr.DragonMounts.types.dragonBreeds.DragonBreed;
import dmr.DragonMounts.util.SchemaValidator;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = DMR.MOD_ID)
public class DataPackHandler {
    @SubscribeEvent
    public static void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) DataPackHandler::loadData);
    }

    public static void loadData(ResourceManager resourceManager) {
        loadData(resourceManager, "breeds", DragonBreed.class, DragonBreedsRegistry::setBreeds);
        loadData(resourceManager, "armor", DragonArmor.class, DragonArmorRegistry::setArmors);
        loadData(resourceManager, "breath_types", DragonBreathType.class, DragonBreathRegistry::setBreathTypes);
        loadData(resourceManager, "abilities", DragonAbility.class, DragonAbilityRegistry::setAbilityDefinitions);
        loadData(resourceManager, "ability_tags", DragonAbilityTag.class, DragonAbilityTagRegistry::setAbilityTags);
    }

    private static <T extends DatapackEntry> void loadData(
            ResourceManager resourceManager, String path, Class<T> dataClass, Consumer<List<T>> consumer) {
        var items = resourceManager.listResources(path, s -> true);
        items.putAll(resourceManager.listResources("dmr/" + path, s -> true)); // Legacy path
        List<T> ls = new ArrayList<>();

        for (Entry<ResourceLocation, Resource> entry : items.entrySet()) {
            var id = entry.getKey();
            var resource = entry.getValue();

            try (InputStream stream = resource.open()) {
                var json = lenientMapper.readTree(stream.readAllBytes());

                if (SchemaValidator.validate(getSchemaName(dataClass), json.toPrettyString(), id.getPath())) {
                    var item = DMR.getGson().fromJson(json.toPrettyString(), dataClass);

                    item.setId(id.getPath()
                            .substring(id.getPath().lastIndexOf("/") + 1)
                            .replace(".json", ""));
                    item.setFileSource(id);

                    ls.add(item);
                }
            } catch (Exception e) {
                DMR.LOGGER.error("Failed to load data for {}", id, e);
            }
        }

        consumer.accept(ls);
    }

    static ObjectMapper lenientMapper = new ObjectMapper();

    static {
        lenientMapper.enable(
                JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(),
                JsonReadFeature.ALLOW_MISSING_VALUES.mappedFeature(),
                JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature(),
                JsonReadFeature.ALLOW_YAML_COMMENTS.mappedFeature(),
                JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature(),
                JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature(),
                JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(),
                JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature());
    }

    @SubscribeEvent
    public static void levelLoad(OnDatapackSyncEvent event) {
        var player = event.getRelevantPlayers().findFirst().orElse(null);
        if (player == null) return;

        var level = player.level;
        var server = level.getServer();

        if (server == null) return;

        LootTableInject.injectLootTables(server);
        ModAdvancements.init(server);

        if (!ServerConfig.ENABLE_BLANK_EGG) {
            server.getRecipeManager()
                    .replaceRecipes(server.getRecipeManager().getRecipes().stream()
                            .filter(recipe -> !recipe.id().equals(DMR.id("blank_egg")))
                            .toList());
        }
    }

    @SubscribeEvent
    public static void dataPackData(OnDatapackSyncEvent event) {
        //        event.getRelevantPlayers()
        //                .forEach(player -> PacketDistributor.sendToPlayer(player, new SyncDataPackPacket()));
        //
    }

    private static <T> String getSchemaName(Class<T> clas) {
        String schemaName = null;

        // Determine which schema to use based on the class
        if (clas == DragonAbility.class) {
            schemaName = "ability_schema";
        } else if (clas == DragonBreed.class) {
            schemaName = "breed_schema";
        } else if (clas == DragonBreathType.class) {
            schemaName = "breath_type_schema";
        } else if (clas == DragonArmor.class) {
            schemaName = "armor_schema";
        } else if (clas == DragonAbilityTag.class) {
            schemaName = "ability_tag_schema";
        }
        return schemaName;
    }
}
