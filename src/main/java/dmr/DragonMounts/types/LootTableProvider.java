package dmr.DragonMounts.types;

import java.util.List;

/**
 * Interface for datapack entries that provide loot tables
 */
public interface LootTableProvider {
    /**
     * Gets the loot tables associated with this entry
     * @return List of loot table entries
     */
    List<LootTableEntry> getLootTable();
}
