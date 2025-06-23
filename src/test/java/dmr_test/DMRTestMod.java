package dmr_test;

import dmr.DragonMounts.DMR;
import dmr_test.reporters.DMRGithubTestDumper;
import dmr_test.reporters.DMRTestDumper;
import dmr_test.reporters.DMRTestReporter;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.conf.MissingDescriptionAction;
import net.neoforged.testframework.impl.MutableTestFramework;

@Mod(DMR.MOD_ID + "_test")
public class DMRTestMod {

    public DMRTestMod(IEventBus bus, ModContainer container) {
        registerTestFramework(bus, container);
    }

    public static void registerTestFramework(IEventBus eventBus, ModContainer container) {
        System.out.println("Registering DMR Test Framework");

        var testIds = ResourceLocation.fromNamespaceAndPath(DMR.MOD_ID + "_test", "tests");
        var frameworkConfig = FrameworkConfiguration.builder(testIds)
                .onMissingDescription(MissingDescriptionAction.WARNING)
                .setDumpers(new DMRTestDumper(), new DMRGithubTestDumper())
                .build();
        MutableTestFramework framework = frameworkConfig.create();

        GlobalTestReporter.replaceWith(new DMRTestReporter());

        framework.init(eventBus, container);
    }
}
