param (
    [string]$WebhookUrl,        # Discord webhook URL
    [string]$ModName,           # Name of the mod
    [string]$NewReleaseVersion, # New release version
    [string]$Changelog,         # Changelog content
    [string]$GitAuthor,         # Git commit author
    [string]$AuthorAvatar,      # Author avatar URL
    [hashtable[]]$Buttons       # List of buttons as name -> url pairs
)

# Generate the components section from the Buttons parameter
$buttonComponents = @()
foreach ($button in $Buttons) {
    $buttonComponents += @{
        type = 2
        style = 5
        label = $button.Name
        url = $button.Url
    }
}

# Get the current timestamp in ISO 8601 format
$timestamp = (Get-Date).ToString("o")

# Define the payload for the Discord webhook
$payload = @{
    content = "Version $NewReleaseVersion of $ModName has been released on Modrinth and CurseForge!"
    embeds = @(
        @{
            description = "**Changelog**: `n========= `n$Changelog"
            color = 3447003
            author = @{
                name = $GitAuthor
                icon_url = $AuthorAvatar
            }
            timestamp = $timestamp
            footer = @{
                text = "Released at"
            }
        }
    )
    components = @(
        @{
            type = 1
            components = $buttonComponents
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
