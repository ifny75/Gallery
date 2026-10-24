param(
    [string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    [string]$Serial = '',
    [string]$Artifacts = "$PSScriptRoot\..\build\simultaneous-scroll"
)
$ErrorActionPreference = 'Stop'
if (-not $Serial) {
    $connected = @(& $Adb devices | ForEach-Object { if ($_ -match '^([^\s]+)\s+device\s*$') { $Matches[1] } })
    if ($connected.Count -ne 1) { throw 'Connect one device or specify -Serial.' }
    $Serial = $connected[0]
}
$null = New-Item -ItemType Directory -Path $Artifacts -Force
function Read-Ui([string]$name) {
    & $Adb -s $Serial shell uiautomator dump /data/local/tmp/gallery-short-scroll.xml | Out-Null
    $path = Join-Path $Artifacts "$name.xml"
    & $Adb -s $Serial pull /data/local/tmp/gallery-short-scroll.xml $path 2>$null | Out-Null
    return [xml](Get-Content -LiteralPath $path -Raw)
}
function Bounds($node) {
    if ($null -eq $node) { throw 'Required UI node missing; use English UI and portrait Photos.' }
    return @([regex]::Matches($node.bounds, '\d+') | ForEach-Object { [int]$_.Value })
}
& $Adb -s $Serial shell am start -W -n com.dot.gallery.debug/com.dot.gallery.Launcher_ReFra | Out-Null
$ui = Read-Ui 'navigation'
$tab = $ui.SelectSingleNode("//node[@text='Photos']")
$c = Bounds $tab
& $Adb -s $Serial shell input tap ([int](($c[0]+$c[2])/2)) ([int](($c[1]+$c[3])/2))
& $Adb -s $Serial shell input swipe 540 600 540 1900 400
& $Adb -s $Serial shell input swipe 540 600 540 1900 400
$before = Read-Ui 'before'
$titleBefore = Bounds ($before.SelectSingleNode("//node[@text='All pictures']"))
$photo = $before.SelectNodes('//node') | Where-Object { $_.'content-desc' -match '\.(jpg|jpeg|png|webp)$' } | Select-Object -First 1
$photoBefore = Bounds $photo
# Shorter than half of the collapsing title range: the title snaps back open.
# The old pre-scroll consumer spent this entire gesture on the title and moved no photos.
& $Adb -s $Serial shell input swipe 540 1600 540 1500 1200
$after = Read-Ui 'after'
$titleAfter = Bounds ($after.SelectSingleNode("//node[@text='All pictures']"))
$samePhoto = $after.SelectNodes('//node') | Where-Object { $_.'content-desc' -eq $photo.'content-desc' } | Select-Object -First 1
$photoAfter = Bounds $samePhoto
if ($photoBefore[1] - $photoAfter[1] -lt 30) { throw 'Photos did not move during the short header gesture.' }
if (($titleAfter[3]-$titleAfter[1]) -lt 0.9*($titleBefore[3]-$titleBefore[1])) { throw 'Invalid sample: title collapsed completely.' }
"Passed: photo moved $($photoBefore[1]-$photoAfter[1]) px while the title remained expanded."
