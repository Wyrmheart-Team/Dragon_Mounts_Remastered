package dmr.DragonMounts.data;

import dmr.DragonMounts.DMR;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = DMR.MOD_ID, bus = Bus.MOD)
public class DataProvider {

	@SubscribeEvent
	public static void gather(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

		generator.addProvider(
			event.includeServer(),
			new BlockTagProvider(output, event.getLookupProvider(), DMR.MOD_ID, existingFileHelper)
		);
		generator.addProvider(
			event.includeServer(),
			new EntityTagProvider(output, event.getLookupProvider(), DMR.MOD_ID, existingFileHelper)
		);
		generator.addProvider(event.includeServer(), new DMRRecipeProvider(output, event.getLookupProvider()));
		generator.addProvider(event.includeServer(), new DMRItemModelProvider(output, DMR.MOD_ID, existingFileHelper));
	}
}
