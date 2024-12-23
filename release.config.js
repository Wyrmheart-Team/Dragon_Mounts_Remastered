const Handlebars = require("handlebars");

const types = [
    { type: "feat", section: ":sparkles: Features", hidden: false },
    { type: "fix", section: ":bug: Bug Fixes", hidden: false },
    { type: "docs", section: ":memo: Documentation", hidden: false },
    { type: "style", section: ":art: Code Styling", hidden: false },
    { type: "refactor", section: ":recycle: Refactoring", hidden: false },
    { type: "perf", section: ":zap: Performance", hidden: false },
    { type: "test", section: ":white_check_mark: Testing", hidden: false },
    { type: "ci", hidden: true },
    { type: "chore", hidden: true },
];

// Handlebars.registerHelper("formatDate", (isoDate) => {
//     if (!isoDate) {
//         return "Unknown Date";
//     }
//     try {
//         const date = isoDate instanceof Date ? isoDate : new Date(isoDate);
//         const formatter = new Intl.DateTimeFormat("en-US", {
//             year: "numeric",
//             month: "long",
//             day: "numeric",
//         });
//         return formatter.format(date);
//     } catch (error) {
//         console.error("Error formatting date:", error);
//         return "Invalid Date";
//     }
// });

module.exports = {
    branches: [{ name: "1.21" }, { name: "1.20.4", range: "1.1.x" }],
    plugins: [
        [
            "@semantic-release/commit-analyzer",
            {
                preset: "angular",
                releaseRules: [
                    { type: "feat", release: "minor" },
                    { type: "fix", release: "patch" },
                    { type: "refactor", release: "patch" },
                    { type: "docs", release: "patch" },
                    { type: "test", release: "patch" },
                    { type: "style", release: "patch" },
                    { type: "perf", release: "patch" },
                    { type: "ci", release: false },
                    { type: "build", release: "patch" },
                ],
            },
        ],
        [
            "@semantic-release/release-notes-generator",
            {
                preset: "conventionalcommits",
                linkCompare: false,
                linkReferences: false,
                writerOpts: {
                    commitsSort: ["scope", "subject"],
                    headerPartial: "## 🚀 Release {{version}} - {{date}}\n\n",
                    transform: (commit, context) => {
                        if (!commit.message) return null;

                        // Parse the message into individual type sections
                        const regex = /(\w+):\s(.*?)(?=(\w+:)|$)/gs;
                        let match;
                        const parsed = [];

                        while ((match = regex.exec(commit.message)) !== null) {
                            const [_, type, message] = match;
                            const section = types.find((t) => t.type === type)?.section;

                            if (section) {
                                parsed.push({
                                    type: section,
                                    subject: message.trim(),
                                    section,
                                });
                            }
                        }

                        // Keep only the first type and annotate others in notes
                        if (parsed.length > 0) {
                            const mainCommit = parsed.reverse()[0];
                            mainCommit.notes = parsed.slice(1).map((p) => ({
                                title: p.type,
                                text: p.subject,
                            }));
                            return mainCommit;
                        }

                        return null;
                    },
                },
                presetConfig: {
                    types,
                },
            },
        ],
    ],
};
