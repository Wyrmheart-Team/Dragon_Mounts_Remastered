package dmr;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.testframework.summary.TestSummary;

import java.nio.file.Path;

public class DMRGithubTestDumper extends DMRTestDumper {

	@Override
	public Path outputPath(ResourceLocation frameworkId) {
		return Path.of(System.getenv("GITHUB_STEP_SUMMARY"));
	}

	@Override
	public boolean enabled(TestSummary summary) {
		return summary.isGameTestRun() && System.getenv().containsKey("GITHUB_STEP_SUMMARY");
	}
}
