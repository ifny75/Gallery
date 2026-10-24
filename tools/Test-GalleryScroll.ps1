param(
    [string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    [string]$Serial = '',
    [string]$Package = 'com.dot.gallery.debug',
    [string]$Output = "$PSScriptRoot\..\build\scroll-performance.txt",
    [int]$Cycles = 6
)
$ErrorActionPreference = 'Stop'
if (-not $Serial) {
    $connected = @(& $Adb devices | ForEach-Object {
        if ($_ -match '^([^\s]+)\s+device\s*$') { $Matches[1] }
    })
    if ($connected.Count -ne 1) { throw 'Connect one device or specify -Serial.' }
    $Serial = $connected[0]
}
# Open Photos and return to the top before running. Never measure a background app.
$foreground = & $Adb -s $Serial shell dumpsys activity activities
if (-not ($foreground -match "ResumedActivity[=:].*$([regex]::Escape($Package))")) {
    throw 'Gallery must be in the foreground, with Photos open.'
}
$display = (& $Adb -s $Serial shell wm size) -join ' '
if ($display -notmatch '(\d+)x(\d+)') { throw 'Cannot determine display size.' }
$width = [int]$Matches[1]; $height = [int]$Matches[2]
$x = [int]($width * 0.5); $top = [int]($height * 0.29); $bottom = [int]($height * 0.78)
$null = & $Adb -s $Serial shell dumpsys gfxinfo $Package reset
for ($i = 0; $i -lt $Cycles; $i++) {
    & $Adb -s $Serial shell input swipe $x $bottom $x $top 400
    & $Adb -s $Serial shell input swipe $x $top $x $bottom 400
}
$report = (& $Adb -s $Serial shell dumpsys gfxinfo $Package) -join "`n"
$parent = Split-Path -Parent ([IO.Path]::GetFullPath($Output))
$null = New-Item -ItemType Directory -Path $parent -Force
$report | Set-Content -LiteralPath $Output -Encoding utf8
if ($report -notmatch 'Total frames rendered: (\d+)' -or [int]$Matches[1] -lt 100) {
    throw 'Too few frames: invalid scroll sample.'
}
$report -split "`n" | Select-Object -First 20
# A regression guard, not a promise of zero jank on every device/library.
if ($report -match '95th percentile: (\d+)ms' -and [int]$Matches[1] -gt 32) {
    throw 'Scroll regression: 95th percentile exceeds 32 ms.'
}

