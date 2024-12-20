module.exports = {
    branches: [
        {
            name: "1.21",
        },
        {
            name: "1.20.4",
            range: "1.1.x",
        },
    ],
    plugins: [
        [
            "@semantic-release/commit-analyzer",
            {
                preset: "angular",
                releaseRules: [
                    { type: "feat", release: "minor" },
                    { type: "minor", release: "patch" },
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
                    commitsSort: ["subject", "scope"],
                    headerPartial: "## 🚀 Release {{version}} - {{formatDate date}}\n\n",
                    transform: (commit, context) => {
                        if (!commit.message) return [];
                        const regex = /(\w+):\s(.*?)(?=(\w+:)|$)/gs;
                        const parsedCommits = [];
                        let match;
                        while ((match = regex.exec(commit.message)) !== null) {
                            const type = match[1];
                            const message = match[2];
                            const section = context.typeMap[type];
                            if (section) {
                                parsedCommits.push({
                                    ...commit,
                                    type: section,
                                    subject: message.trim(),
                                });
                            }
                        }
                        return parsedCommits;
                    },
                    getExtraContext: (context) => {
                        const typeMap = {};
                        context.commitGroups.forEach((group) => {
                            group.commits.forEach((commit) => {
                                typeMap[commit.type] = group.title;
                            });
                        });
                        return { ...context, typeMap };
                    },
                    // Helpers for custom formatting
                    helpers: {
                        formatDate: (isoDate) => {
                            const date = new Date(isoDate);
                            const formatter = new Intl.DateTimeFormat("en-US", {
                                year: "numeric",
                                month: "long",
                                day: "numeric",
                            });
                            return formatter.format(date); // Example: "December 20, 2024"
                        },
                    },
                },
                presetConfig: {
                    types: [
                        { type: "feat", section: ":sparkles: Features", hidden: false },
                        { type: "fix", section: ":bug: Bug Fixes", hidden: false },
                        { type: "docs", section: ":memo: Documentation", hidden: false },
                        { type: "style", section: ":art: Code Styling", hidden: false },
                        { type: "refactor", section: ":recycle: Refactoring", hidden: false },
                        { type: "perf", section: ":zap: Performance", hidden: false },
                        { type: "test", section: ":white_check_mark: Testing", hidden: false },
                        { type: "ci", section: ":repeat: Continuous Integration", hidden: true },
                        { type: "chore", hidden: true },
                    ],
                },
            },
        ],
    ],
};
