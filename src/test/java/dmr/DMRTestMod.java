package dmr;

import dmr.DragonMounts.DMR;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.conf.MissingDescriptionAction;
import net.neoforged.testframework.impl.MutableTestFramework;

public class DMRTestMod {

    public static void registerTestFramework(IEventBus eventBus, ModContainer container) {
        final MutableTestFramework framework = FrameworkConfiguration.builder(DMR.id("tests"))
                .onMissingDescription(MissingDescriptionAction.WARNING)
                .setDumpers(new DMRTestDumper(), new DMRGithubTestDumper())
                .build()
                .create();

        GlobalTestReporter.replaceWith(new DMRTestReporter());

        framework.init(eventBus, container);
    }
}
