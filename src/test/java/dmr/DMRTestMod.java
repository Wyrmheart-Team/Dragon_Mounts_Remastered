package dmr;

import dmr.DragonMounts.DMR;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.testframework.conf.ClientConfiguration;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.conf.MissingDescriptionAction;
import net.neoforged.testframework.impl.MutableTestFramework;
import org.lwjgl.glfw.GLFW;

public class DMRTestMod {

	public static void registerTestFramework(IEventBus eventBus, ModContainer container) {
		final MutableTestFramework framework = FrameworkConfiguration.builder(DMR.id("tests"))
			.clientConfiguration(() ->
				ClientConfiguration.builder().toggleOverlayKey(GLFW.GLFW_KEY_J).openManagerKey(GLFW.GLFW_KEY_N).build()
			)
			.onMissingDescription(MissingDescriptionAction.WARNING)
			.dumpers(new DMRTestDumper(), new DMRGithubTestDumper())
			.build()
			.create();

		GlobalTestReporter.replaceWith(new DMRTestReporter());

		framework.init(eventBus, container);
	}
}
