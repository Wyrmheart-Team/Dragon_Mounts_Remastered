package dmr.DragonMounts.data;

import dmr.DragonMounts.DMR;
import dmr.DragonMounts.config.ClientConfig;
import dmr.DragonMounts.config.ServerConfig;
import dmr.DragonMounts.config.annotations.Config;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Generates language entries for configuration options.
 * This provider extracts config keys and comments from the Config annotations
 * and generates appropriate translation entries.
 */
public class ConfigTranslationProvider extends LanguageProvider {

    public ConfigTranslationProvider(PackOutput output) {
        super(output, DMR.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        DMR.LOGGER.info("Generating config translations");

        // Process server configs
        processConfigClass(ServerConfig.class);

        // Process client configs
        processConfigClass(ClientConfig.class);
    }

    /**
     * Processes a config class and generates translation entries for all annotated fields.
     *
     * @param configClass The config class to process
     */
    private void processConfigClass(Class<?> configClass) {
        // Set to track unique categories
        Set<String> categories = new HashSet<>();

        // First pass: collect all categories
        for (Field field : configClass.getDeclaredFields()) {
            Config configAnnotation = field.getAnnotation(Config.class);
            if (configAnnotation == null) continue;

            String[] categoryArray = configAnnotation.category();
            if (categoryArray.length > 0) {
                // Add the last category in the array
                categories.add(categoryArray[categoryArray.length - 1]);
            }
        }

        // Generate translation entries for categories
        for (String category : categories) {
            String categoryKey = "dmr.configuration." + category;
            add(categoryKey, formatCategoryName(category) + " Settings");
        }

        // Second pass: process fields
        for (Field field : configClass.getDeclaredFields()) {
            Config configAnnotation = field.getAnnotation(Config.class);
            if (configAnnotation == null) continue;

            String translationKey = configAnnotation.translation();

            // Generate default translation key if none is provided
            if (translationKey.isEmpty()) {
                // Determine if this is a server or client config
                String configType = configClass == ServerConfig.class ? "server" : "client";
                translationKey = "dmr.config." + configType + "." + configAnnotation.key();
            }

            // Generate the name translation (the config option name)
            add(translationKey, formatConfigName(configAnnotation.key()));

            // Generate the tooltip translation (the config comment)
            String[] comments = configAnnotation.comment();
            if (comments.length > 0) {
                add(translationKey + ".tooltip", String.join("\n", comments));
            }
        }
    }

    /**
     * Formats a category name into a user-friendly name.
     * For example, "base_stats" becomes "Base Stats".
     *
     * @param category The category name
     * @return A formatted name
     */
    private String formatCategoryName(String category) {
        return formatConfigName(category);
    }

    /**
     * Formats a config key into a user-friendly name.
     * For example, "hatch_time" becomes "Hatch Time".
     *
     * @param key The config key
     * @return A formatted name
     */
    private String formatConfigName(String key) {
        String[] words = key.split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }
}
