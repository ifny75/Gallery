param(
    [string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    [string]$Serial = '',
    [string]$Artifacts = 'C:\Graphene\build-output\gallery-toolbar-tests'
)
$ErrorActionPreference = 'Stop'
if (-not $Serial) {
    $connected = @(& $Adb devices | ForEach-Object {
        if ($_ -match '^([^\s]+)\s+device\s*$') { $Matches[1] }
    })
    if ($connected.Count -ne 1) { throw 'Connect one device or specify -Serial.' }
    $Serial = $connected[0]
}
$null = New-Item -ItemType Directory -Path $Artifacts -Force
function Read-Ui([string]$name) {
    & $Adb -s $Serial shell uiautomator dump /data/local/tmp/gallery-toolbar-test.xml | Out-Null
    $path = Join-Path $Artifacts "$name.xml"
    & $Adb -s $Serial pull /data/local/tmp/gallery-toolbar-test.xml $path 2>$null | Out-Null
    return [xml](Get-Content -LiteralPath $path -Raw)
}
function Bounds($node) {
    return @([regex]::Matches($node.bounds, '\d+') | ForEach-Object { [int]$_.Value })
}
function Tap-Node($node) {
    if ($null -eq $node) { throw 'Required UI node was not found.' }
    $c = Bounds $node
    & $Adb -s $Serial shell input tap ([int](($c[0]+$c[2])/2)) ([int](($c[1]+$c[3])/2))
}
function Title($ui, [string]$text) {
    return $ui.SelectNodes("//node[@text='$text']") | Sort-Object { (Bounds $_)[1] } | Select-Object -First 1
}
& $Adb -s $Serial shell input keyevent KEYCODE_WAKEUP
& $Adb -s $Serial shell wm dismiss-keyguard
& $Adb -s $Serial shell am start -W -n com.dot.gallery.debug/com.dot.gallery.Launcher_ReFra | Out-Null
foreach ($entry in @(@('Photos','All pictures'), @('Albums','Albums'), @('Search','Search'), @('Bin','Bin'))) {
    $ui = Read-Ui 'before-tab'
    # Bottom navigation is below any same-named page title.
    $tab = $ui.SelectNodes("//node[@text='$($entry[0])']") | Sort-Object { (Bounds $_)[1] } -Descending | Select-Object -First 1
    Tap-Node $tab
    & $Adb -s $Serial shell input swipe 540 600 540 1900 350
    & $Adb -s $Serial shell input swipe 540 600 540 1900 350
    $expanded = Read-Ui "$($entry[0])-expanded"
    $before = Bounds (Title $expanded $entry[1])
    & $Adb -s $Serial shell input swipe 540 1900 540 600 450
    $collapsed = Read-Ui "$($entry[0])-collapsed"
    $heading = Title $collapsed $entry[1]
    if (-not $heading) { throw "$($entry[0]): collapsed title disappeared." }
    $after = Bounds $heading
    if ($after[1] -gt 300 -or $after[0] -ge $before[0] -or ($after[3]-$after[1]) -ge ($before[3]-$before[1])) {
        throw "$($entry[0]): heading failed to shrink and move into the left toolbar. Before: $before; after: $after"
    }
    "$($entry[0]): persistent collapsed title passed."
}
# Empty and populated queries must both offer Back to search.
$ui = Read-Ui 'back-to-search-tab'
Tap-Node ($ui.SelectNodes("//node[@text='Search']") | Sort-Object { (Bounds $_)[1] } -Descending | Select-Object -First 1)
foreach ($suggestion in @('Selfies','Screenshots')) {
    $ui = Read-Ui "$suggestion-before"
    Tap-Node ($ui.SelectSingleNode("//node[@text='$suggestion']"))
    $ui = Read-Ui "$suggestion-results"
    $back = $ui.SelectSingleNode("//node[@content-desc='Back to search']")
    if (-not $back) { throw "${suggestion}: Back to search is missing." }
    if ($suggestion -eq 'Selfies') {
        & $Adb -s $Serial shell input keyevent KEYCODE_BACK
    } else {
        Tap-Node $back
    }
    $ui = Read-Ui "$suggestion-reset"
    if (-not $ui.SelectSingleNode("//node[@text='Suggestions']")) { throw "${suggestion}: failed to return to suggestions." }
    "${suggestion}: return to suggestions passed."
}

# The secondary Library destination uses the same toolbar contract.
$ui = Read-Ui 'library-from-search'
Tap-Node ($ui.SelectSingleNode("//node[@text='Photos']"))
$ui = Read-Ui 'library-menu-source'
Tap-Node ($ui.SelectSingleNode("//node[@content-desc='More options']"))
$ui = Read-Ui 'library-menu'
Tap-Node ($ui.SelectSingleNode("//node[@text='Library']"))
& $Adb -s $Serial shell input swipe 540 600 540 1900 350
$ui = Read-Ui 'Library-expanded'
$before = Bounds (Title $ui 'Library')
& $Adb -s $Serial shell input swipe 540 1900 540 600 450
$ui = Read-Ui 'Library-collapsed'
$after = Bounds (Title $ui 'Library')
if ($after[1] -gt 300 -or $after[0] -ge $before[0] -or ($after[3]-$after[1]) -ge ($before[3]-$before[1])) {
    throw 'Library: heading failed to collapse.'
}
'Library: persistent collapsed title passed.'
& $Adb -s $Serial shell input keyevent KEYCODE_BACK

