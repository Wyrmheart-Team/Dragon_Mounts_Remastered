package dmr.DragonMounts.types.abilities;

import com.google.gson.annotations.SerializedName;
import dmr.DragonMounts.types.DatapackEntry;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DragonAbilityTag extends DatapackEntry {
    @SerializedName("abilities")
    private List<DragonAbilityEntry> abilities = new ArrayList<>();
}
