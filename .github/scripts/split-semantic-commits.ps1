# split-semantic-commits.ps1
# This script splits a semantic versioning commit message into multiple commits
# for semantic-release to process

# Get the last commit message
$COMMIT_MSG = git log -1 --pretty=%B

Write-Host "Original commit message: $COMMIT_MSG"

# Create a temporary directory for the new repository
$TEMP_DIR = New-TemporaryFile | ForEach-Object {
    Remove-Item $_ -Force
    New-Item -ItemType Directory -Path $_.FullName
    $_.FullName
}
Write-Host "Created temporary repository at: $TEMP_DIR"

# Save the current directory
$ORIGINAL_DIR = Get-Location

# Move to the temporary directory and set up git
Set-Location $TEMP_DIR
git init
git config --local user.name "GitHub Actions Bot"
git config --local user.email "actions@github.com"

# Create an initial commit
New-Item -ItemType File -Path "README.md" -Force | Out-Null
git add README.md
git commit -m "Initial commit"

# Split the commit message and create separate commits
# Extract parts that match semantic versioning format
$regex = '(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\([^)]*\))?:[^,]+'
$matches = [regex]::Matches($COMMIT_MSG, $regex)

if ($matches.Count -eq 0) {
    # If no semantic versioning parts found, create a single commit with the original message
    Write-Host "No semantic versioning parts found, using original message"
    git commit --allow-empty -m $COMMIT_MSG
} else {
    # Create separate commits for each semantic versioning part
    foreach ($match in $matches) {
        $part = $match.Value.Trim()
        Write-Host "Creating commit with message: $part"
        git commit --allow-empty -m $part
    }
}

# Display the split commits
Write-Host "Split commit messages:"
git log --pretty=format:"%h %s" | Where-Object { $_ -notmatch "Initial commit" }

# Set environment variable for semantic-release to use this repository
$env:GIT_DIR = "$TEMP_DIR/.git"

# Return to the original directory
Set-Location $ORIGINAL_DIR

Write-Host "Temporary git repository with split commits is ready at: $TEMP_DIR"
Write-Host "GIT_DIR environment variable set to: $env:GIT_DIR"