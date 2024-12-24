param (
    [string]$WebhookUrl,        # Discord webhook URL
    [string]$ModName,           # Name of the mod
    [string]$NewReleaseVersion, # New release version
    [string]$Changelog,         # Changelog content
    [string]$GitAuthor,         # Git commit author
    [string]$AuthorAvatar,      # Author avatar URL
    [string]$ButtonsJson
)

$links = ""

# Generate the components section from the Buttons parameter
foreach ($button in $($ButtonsJson | ConvertFrom-Json)) {
    $links += "[[$($button.Name)]($($button.Url))] "
}

Write-Host "Sending Discord notification..."

# Get the current timestamp in ISO 8601 format
$timestamp = (Get-Date).ToString("o")

$description = $Changelog -replace '\\n', "`n"

# Define the payload for the Discord webhook
$payload = @{
    content = "Version $NewReleaseVersion of $ModName has been released on Modrinth and CurseForge!"
    embeds = @(
        @{
            title = "Release $NewReleaseVersion"
            description = $description + "`n" + $links
            color = 3447003
            author = @{
                name = $GitAuthor
                icon_url = $AuthorAvatar
            }
            timestamp = $timestamp
            footer = @{
                text = "Released"
            }
        }
    )
} | ConvertTo-Json -Depth 10 -Compress

# Send the payload to the Discord webhook
try {
    $response = Invoke-RestMethod -Uri $WebhookUrl -Method Post -ContentType "application/json" -Body $payload
    Write-Host "Discord notification sent successfully."
} catch {
    Write-Error "Failed to send Discord notification. Error: $_"
}
