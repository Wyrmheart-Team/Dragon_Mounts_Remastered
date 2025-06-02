package dmr.DragonMounts.config;

import dmr.DragonMounts.config.annotations.Config;
import dmr.DragonMounts.config.annotations.RangeConstraint;
import dmr.DragonMounts.config.annotations.SyncedConfig;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigProcessor {
    private static final Map<String, ConfigField> SYNCED_CONFIGS = new HashMap<>();
    private static final Map<String, ModConfigSpec.ConfigValue<?>> CONFIG_VALUES = new HashMap<>();

    /**
     * Processes a configuration class and builds a ModConfigSpec.
     */
    public static ModConfigSpec processConfig(Class<?> configClass) {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        for (Field field : configClass.getDeclaredFields()) {
            Config configAnnotation = field.getAnnotation(Config.class);
            if (configAnnotation == null) continue;

            String[] categories = configAnnotation.category();
            ModConfigSpec.Builder targetBuilder = builder;

            for (String category : categories) {
                builder = builder.push(category);
            }

            processField(field, configAnnotation, targetBuilder, configClass);

            for (String ignored : categories) {
                builder = builder.pop();
            }
        }

        return builder.build();
    }

    private static void processField(
            Field field, Config configAnnotation, ModConfigSpec.Builder builder, Class<?> configClass) {
        String key = configAnnotation.key();
        String[] comment = configAnnotation.comment();
        String translation = configAnnotation.translation();
        boolean worldRestart = configAnnotation.worldRestart();

        // Add comment if present
        if (comment.length > 0) {
            builder.comment(comment);
        }

        // Generate default translation key if none is provided
        if (translation.isEmpty()) {
            // Determine if this is a server or client config
            String configType = configClass == ServerConfig.class ? "server" : "client";
            translation = "dmr.config." + configType + "." + key;
        }

        // Add translation
        builder.translation(translation);

        // Apply world restart if needed
        if (worldRestart) {
            builder.worldRestart();
        }

        // Get range constraints if present
        RangeConstraint rangeAnnotation = field.getAnnotation(RangeConstraint.class);
        double min = rangeAnnotation != null ? rangeAnnotation.min() : Double.MIN_VALUE;
        double max = rangeAnnotation != null ? rangeAnnotation.max() : Double.MAX_VALUE;

        // Create the config value based on field type
        try {
            field.setAccessible(true);
            Object defaultValue = field.get(null);
            ModConfigSpec.ConfigValue<?> configValue;
            Class<?> fieldType = field.getType();

            if (fieldType == boolean.class || fieldType == Boolean.class) {
                configValue = builder.define(key, (Boolean) defaultValue == true);
            } else if (fieldType == int.class || fieldType == Integer.class) {
                configValue = builder.defineInRange(key, (Integer) defaultValue, (int) min, (int) max);
            } else if (fieldType == long.class || fieldType == Long.class) {
                configValue = builder.defineInRange(key, (Long) defaultValue, (long) min, (long) max);
            } else if (fieldType == double.class || fieldType == Double.class) {
                configValue = builder.defineInRange(key, (Double) defaultValue, min, max);
            } else if (fieldType == String.class) {
                configValue = builder.define(key, (String) defaultValue);
            } else {
                throw new IllegalArgumentException("Unsupported config field type: " + fieldType.getName());
            }

            // Store the config value for later use
            CONFIG_VALUES.put(key, configValue);

            // Register synced configs
            // All server configs are automatically registered for syncing to client
            boolean isServerConfig = configClass == ServerConfig.class;
            SyncedConfig syncedAnnotation = field.getAnnotation(SyncedConfig.class);

            if (isServerConfig || syncedAnnotation != null) {
                SYNCED_CONFIGS.put(key, new ConfigField(field, configClass, syncedAnnotation, isServerConfig));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to process config field: " + field.getName(), e);
        }
    }

    /**
     * Updates the primitive field values from the ModConfigSpec values.
     */
    public static void updateFieldValues(Class<?> configClass) {
        for (Field field : configClass.getDeclaredFields()) {
            Config configAnnotation = field.getAnnotation(Config.class);
            if (configAnnotation == null) continue;

            String key = configAnnotation.key();
            ModConfigSpec.ConfigValue<?> configValue = CONFIG_VALUES.get(key);
            if (configValue == null) continue;

            try {
                field.setAccessible(true);
                Object value = configValue.get();
                field.set(null, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to update config field: " + field.getName(), e);
            }
        }
    }

    /**
     * Gets all synced config fields.
     */
    public static Map<String, ConfigField> getSyncedConfigs() {
        return SYNCED_CONFIGS;
    }

    /**
     * Gets the ModConfigSpec value for a config key.
     */
    public static ModConfigSpec.ConfigValue<?> getConfigValue(String key) {
        return CONFIG_VALUES.get(key);
    }

    /**
     * Represents a configuration field with its metadata.
     */
    public record ConfigField(Field field, Class<?> configClass, SyncedConfig syncConfig, boolean isServerConfig) {

        public boolean shouldSyncToClient() {
            return isServerConfig;
        }

        public boolean shouldSyncToServer() {
            return !isServerConfig && syncConfig != null;
        }

        public Object getValue() {
            try {
                field.setAccessible(true);
                return field.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to get config value: " + field.getName(), e);
            }
        }

        public void setValue(Object value) {
            try {
                field.setAccessible(true);
                field.set(null, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to set config value: " + field.getName(), e);
            }
        }
    }
}
