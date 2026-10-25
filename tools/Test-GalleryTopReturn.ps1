param(
    [string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    [string]$Serial = '',
    [string]$Artifacts = "$PSScriptRoot\..\build\top-return",
    [int]$Cycles = 3,
    [switch]$Mosaic
)
$ErrorActionPreference = 'Stop'
if (-not $Serial) {
    $connected = @(& $Adb devices | ForEach-Object { if ($_ -match '^([^\s]+)\s+device\s*$') { $Matches[1] } })
    if ($connected.Count -ne 1) { throw 'Connect one device or specify -Serial.' }
    $Serial = $connected[0]
}
$null = New-Item -ItemType Directory -Path $Artifacts -Force
function Bounds($node) {
    if ($null -eq $node) { throw 'Required node missing: use portrait Photos with English UI and a populated library.' }
    return @([regex]::Matches($node.bounds, '\d+') | ForEach-Object { [int]$_.Value })
}
$size = (& $Adb -s $Serial shell wm size) -join ' '
if ($size -notmatch '(\d+)x(\d+)') { throw 'Display size unavailable.' }
$width = [int]$Matches[1]; $height = [int]$Matches[2]
$densities = [regex]::Matches(((& $Adb -s $Serial shell wm density) -join ' '), 'density: (\d+)')
$density = [int]$densities[$densities.Count - 1].Groups[1].Value / 160.0
$x = [int]($width * .5); $top = [int]($height * .28); $bottom = [int]($height * .8)
for ($cycle = 1; $cycle -le $Cycles; $cycle++) {
    for ($i = 0; $i -lt 3; $i++) {
        & $Adb -s $Serial shell input swipe $x $bottom $x $top 100
    }
    for ($i = 0; $i -lt 5; $i++) {
        & $Adb -s $Serial shell input swipe $x $top $x $bottom 80
    }
    & $Adb -s $Serial shell uiautomator dump /data/local/tmp/gallery-top-return.xml | Out-Null
    $path = Join-Path $Artifacts "cycle-$cycle.xml"
    & $Adb -s $Serial pull /data/local/tmp/gallery-top-return.xml $path 2>$null | Out-Null
    $ui = [xml](Get-Content -LiteralPath $path -Raw)
    $title = Bounds ($ui.SelectSingleNode("//node[@text='All pictures']"))
    $photo = $ui.SelectNodes('//node') | Where-Object { $_.'content-desc' -match '\.(jpg|jpeg|png|webp|heic)$' } | Select-Object -First 1
    $tile = Bounds $photo
    # The expanded title is vertically centered in the 176dp header. Content
    # must start below that header, even after a fling with a large remainder.
    $headerBottom = ($title[1] + $title[3]) / 2.0 + 88 * $density
    & $Adb -s $Serial shell screencap -p /data/local/tmp/gallery-top-return.png
    & $Adb -s $Serial pull /data/local/tmp/gallery-top-return.png (Join-Path $Artifacts "cycle-$cycle.png") 2>$null | Out-Null
    if ($title[3] - $title[1] -lt 36 * $density) { throw 'Invalid sample: the header has not fully expanded at the start.' }
    if ($tile[1] -lt $headerBottom - 2) {
        throw "First photo overlaps the expanded header: photo top $($tile[1]), header bottom $headerBottom."
    }
    if (-not $Mosaic -and [Math]::Abs(($tile[3] - $tile[1]) - ($tile[2] - $tile[0])) -gt 2) {
        throw 'The first photo is clipped instead of displaying as a complete square.'
    }
    "Cycle ${cycle}: full first photo below expanded header (photo top $($tile[1]), header bottom $headerBottom)."
}
