package dmr.DragonMounts.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.resource.SchemaLoader;
import dmr.DragonMounts.DMR;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(modid = DMR.MOD_ID)
public class SchemaValidator {
    // A logical identifier for schemas, used to resolve relative $ref pointers.
    public static final String SCHEMA_BASE_URI = "http://localhost/";

    // Schemas that are used to validate top-level data files.
    private static final List<String> TOP_LEVEL_SCHEMAS = Arrays.asList(
            "ability_schema.json",
            "breed_schema.json",
            "breath_type_schema.json",
            "armor_schema.json",
            "variant_schema.json");

    // All schema files, including dependencies, that must be loaded.
    private static final List<String> ALL_SCHEMAS = Arrays.asList(
            "ability_schema.json",
            "breed_schema.json",
            "breath_type_schema.json",
            "armor_schema.json",
            "variant_schema.json",
            "definitions.json",
            "habitat_schema.json");

    private static final Map<String, JsonSchema> schemaCache = new HashMap<>();
    private static final ArrayList<String> schemas = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static JsonSchemaFactory factory;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) SchemaValidator::loadSchemas);
    }

    public static void loadSchemas(ResourceManager resourceManager) {
        schemaCache.clear();

        Map<String, byte[]> schemaContents = new HashMap<>();
        for (String fileName : ALL_SCHEMAS) {
            try {
                ResourceLocation location = DMR.id("schemas/" + fileName);
                try (InputStream stream = resourceManager.open(location)) {
                    String uri = SCHEMA_BASE_URI + fileName;
                    var content = stream.readAllBytes();

                    var lenientMapper = new ObjectMapper();
                    lenientMapper.enable(
                            JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(),
                            JsonReadFeature.ALLOW_MISSING_VALUES.mappedFeature(),
                            JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature(),
                            JsonReadFeature.ALLOW_YAML_COMMENTS.mappedFeature());
                    JsonNode rootNode = lenientMapper.readTree(content);

                    var prettyPrintMapper = new ObjectMapper();
                    prettyPrintMapper.enable(SerializationFeature.INDENT_OUTPUT);

                    schemaContents.put(uri, prettyPrintMapper.writeValueAsBytes(rootNode));
                    schemas.add(fileName);
                }
            } catch (Exception e) {
                DMR.LOGGER.error("Failed to read local schema file: {}", fileName, e);
            }
        }

        // This loader serves schema content from our pre-loaded map.
        final SchemaLoader mapBasedLoader = uri -> {
            byte[] schemaBytes = schemaContents.get(uri.toString());
            if (schemaBytes == null) {
                return null;
            }
            return () -> new ByteArrayInputStream(schemaBytes);
        };

        factory = JsonSchemaFactory.getInstance(
                VersionFlag.V202012, builder -> builder.schemaLoaders(loaders -> loaders.add(mapBasedLoader)));

        for (String fileName : TOP_LEVEL_SCHEMAS) {
            getAndCacheSchema(fileName);
        }
    }

    private static void getAndCacheSchema(String schemaFileName) {
        try {
            if (!schemas.contains(schemaFileName)) {
                return;
            }

            URI schemaUri = URI.create(SCHEMA_BASE_URI + schemaFileName);
            JsonSchema schema = factory.getSchema(schemaUri);
            String schemaName = schemaFileName.replace(".json", "");
            schemaCache.put(schemaName, schema);
        } catch (Exception e) {
            DMR.LOGGER.error("Failed to get schema from factory: {}", schemaFileName, e);
        }
    }

    public static boolean validate(String schemaName, String jsonString, String fileIdentifier) {
        JsonSchema schema = schemaCache.get(schemaName);
        if (schema == null) {
            DMR.LOGGER.error("Schema not found: {}", schemaName);
            return false;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            Set<ValidationMessage> errors = schema.validate(jsonNode);

            if (!errors.isEmpty()) {
                DMR.LOGGER.error("Schema validation failed for {} with schema {}", fileIdentifier, schemaName);
                errors.forEach(error -> DMR.LOGGER.error("  - {}", error.getMessage()));
                return false;
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON string", e);
        }

        return true;
    }
}
