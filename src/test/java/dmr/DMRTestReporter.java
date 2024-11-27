package dmr;

import java.util.HashMap;
import net.minecraft.Util;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.TestReporter;

public class DMRTestReporter implements TestReporter {

	public static HashMap<String, String> testResults = new HashMap<>();

	@Override
	public void onTestFailed(GameTestInfo testInfo) {
		testResults.put(testInfo.getTestName(), Util.describeError(testInfo.getError()));
	}

	@Override
	public void onTestSuccess(GameTestInfo testInfo) {}
}
