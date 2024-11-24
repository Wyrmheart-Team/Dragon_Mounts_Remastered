package dmr.DragonMounts.data;

import dmr.DragonMounts.DMR;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class DMRItemModelProvider extends ItemModelProvider {

	public DMRItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
		super(output, modid, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		for (DyeColor color : DyeColor.values()) {
			String name = color.getName();
			getBuilder("dragon_whistle." + name)
				.parent(new ModelFile.UncheckedModelFile("item/generated"))
				.texture("layer0", DMR.MOD_ID + ":" + "item/" + name + "_dragon_whistle");
		}
	}
}
