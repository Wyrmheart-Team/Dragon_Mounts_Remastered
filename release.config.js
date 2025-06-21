const types = [
    { type: 'feat',     section: '✨ Features'       },
    { type: 'fix',      section: '🐛 Bug Fixes'      },
    { type: 'docs',     section: '📝 Documentation' },
    { type: 'style',    section: '🎨 Code Styling'   },
    { type: 'refactor', section: '♻️ Refactoring'    },
    { type: 'perf',     section: '⚡ Performance'    },
    { type: 'test',     section: '✅ Testing'        },
    { type: 'build',    section: '🚧 Build System'   },
    { type: 'ci',       hidden: true                },
    { type: 'chore',    hidden: true                }
    // uncomment to include CI/chore sections:
    // { type: 'ci',    section: '🔧 Continuous Integration' },
    // { type: 'chore', section: '🛠️ Chores'              },
];

module.exports = {
    branches: [
        'main'
    ],

    plugins: [
        [
            '@semantic-release/commit-analyzer',
            {
                preset: 'conventionalcommits',
                releaseRules: [
                    { type: 'feat',     release: 'minor' },
                    { type: 'fix',      release: 'patch' },
                    { type: 'refactor', release: 'patch' },
                    { type: 'docs',     release: 'patch' },
                    { type: 'test',     release: 'patch' },
                    { type: 'style',    release: 'patch' },
                    { type: 'perf',     release: 'patch' },
                    { type: 'build',    release: 'patch' },
                    { type: 'ci',       release: false   },
                    { type: 'chore',    release: false   }
                ],
                parserOpts: {
                    headerPattern:        /^(\w*)(?:\(([^)]*)\))?(!)?: (.*)$/,
                    headerCorrespondence: ['type','scope','breaking','subject'],
                    noteKeywords:         [
                        'BREAKING CHANGE',
                        'BREAKING CHANGES',
                        'BREAKING'
                    ]
                }
            }
        ],

        [
            '@semantic-release/release-notes-generator',
            {
                preset:        'conventionalcommits',
                linkCompare:   false,
                linkReferences:true,
                writerOpts: {
                    commitsSort: ['type','scope','subject'],
                    transform: (commit) => {
                        if (commit.scope) {
                            commit.subject = `**${commit.scope}:** ${commit.subject}`;
                        }
                        if (commit.author) {
                            commit.subject += ` (by ${commit.author.name})`;
                        }
                        return commit;
                    }
                },
                presetConfig: { types }
            }
        ],

        '@semantic-release/changelog',  // writes CHANGELOG.md locally
        '@semantic-release/github'      // publishes GitHub Release
    ]
};