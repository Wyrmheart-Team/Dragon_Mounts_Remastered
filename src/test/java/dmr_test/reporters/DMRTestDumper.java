package dmr_test.reporters;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.Test.Result;
import net.neoforged.testframework.summary.FileSummaryDumper;
import net.neoforged.testframework.summary.FormattingUtil;
import net.neoforged.testframework.summary.TestSummary;
import net.neoforged.testframework.summary.TestSummary.TestInfo;
import net.neoforged.testframework.summary.md.Alignment;
import net.neoforged.testframework.summary.md.Table;
import org.slf4j.Logger;

public class DMRTestDumper implements FileSummaryDumper {

    private final Function<TestSummary, String> heading;

    public DMRTestDumper() {
        this("Test Summary");
    }

    public DMRTestDumper(String heading) {
        this(summary -> heading);
    }

    public DMRTestDumper(Function<TestSummary, String> heading) {
        this.heading = heading;
    }

    @Override
    public Path outputPath(ResourceLocation frameworkId) {
        return Path.of(".", "test-results.md");
    }

    @Override
    public boolean enabled(TestSummary summary) {
        return summary.isGameTestRun();
    }

    @Override
    public void write(TestSummary summary, Logger logger, PrintWriter writer) {
        writer.println("# " + this.heading.apply(summary));
        var testsByGroup =
                summary.testInfos().stream().collect(Collectors.groupingBy(TestInfo::groups, Collectors.toList()));

        Table.Builder summaryBuilder = Table.builder()
                .withAlignments(Alignment.LEFT, Alignment.CENTER, Alignment.CENTER, Alignment.CENTER)
                .addRow("Group", "✅ Succeeded", "❌ Failed", "⚠️ Skipped");
        testsByGroup.forEach((group, tests) -> {
            Map<Result, List<TestInfo>> testsByStatus = tests.stream()
                    .collect(Collectors.groupingBy(
                            test -> test.status().result(),
                            () -> new EnumMap<>(Test.Result.class),
                            Collectors.toList()));
            List<TestSummary.TestInfo> failedTests = testsByStatus.getOrDefault(Test.Result.FAILED, List.of());
            List<TestSummary.TestInfo> passedTests = testsByStatus.getOrDefault(Test.Result.PASSED, List.of());
            List<TestSummary.TestInfo> skippedTests = testsByStatus.getOrDefault(Result.NOT_PROCESSED, List.of());
            summaryBuilder.addRow(group.getFirst(), passedTests.size(), failedTests.size(), skippedTests.size());
        });

        Map<Result, List<TestInfo>> testsByStatus = summary.testInfos().stream()
                .collect(Collectors.groupingBy(
                        test -> test.status().result(), () -> new EnumMap<>(Test.Result.class), Collectors.toList()));
        List<TestSummary.TestInfo> failedTests = testsByStatus.getOrDefault(Test.Result.FAILED, List.of());
        List<TestSummary.TestInfo> passedTests = testsByStatus.getOrDefault(Test.Result.PASSED, List.of());
        Table.Builder builder = Table.builder()
                .withAlignments(Alignment.LEFT, Alignment.LEFT, Alignment.CENTER, Alignment.LEFT, Alignment.LEFT)
                .addRow("Test group", "Test Id", "Test Result", "Status message", "Test description");
        if (!failedTests.isEmpty()) {
            for (TestSummary.TestInfo failedTest : failedTests) {
                var message = failedTest.status().message();

                if (DMRTestReporter.testResults.containsKey(failedTest.testId())) {
                    message = DMRTestReporter.testResults.get(failedTest.testId());
                }

                builder.addRow(
                        failedTest.groups().getFirst(),
                        failedTest.testId(),
                        formatStatus(failedTest.result(), !failedTest.manual() && !failedTest.required()),
                        message,
                        getDescription(failedTest));
            }
        }
        if (!passedTests.isEmpty()) {
            for (TestSummary.TestInfo passedTest : passedTests) {
                builder.addRow(
                        passedTest.groups().getFirst(),
                        passedTest.testId(),
                        formatStatus(passedTest.status().result(), false),
                        passedTest.status().message(),
                        getDescription(passedTest));
            }
        }
        if (!passedTests.isEmpty() && failedTests.isEmpty()) {
            writer.println("All tests passed");
        }
        writer.println();
        writer.println(summaryBuilder.build());
        writer.println();

        writer.println("## Test Results");
        writer.println();
        writer.println(builder.build());
    }

    protected String formatStatus(Test.Result result, boolean optional) {
        if (result.failed() && !optional) {
            return "❌";
        } else if (result.passed()) {
            return "✅";
        }
        return "⚠️";
    }

    private static String getDescription(TestSummary.TestInfo failedTest) {
        return failedTest.description().stream()
                .filter(c -> !c.getString().equals("GameTest-only"))
                .map(FormattingUtil::componentToPlainString)
                .collect(Collectors.joining("<br/>"));
    }
}
