# Gallery preview validation — 2026-10-24

Device: Pixel 10, Android 17 / GrapheneOS. Installed package: `com.dot.gallery.debug`.

## Build

`gradlew.bat :app:assembleArm64-v8aNoMLPreview`

The preview build uses R8/resource shrinking, is not debuggable, and uses the debug
application id and signing key so it updates the previous installation without clearing data.
The APK signature was verified with Android apksigner.

## Checks completed

- Six JVM tests passed, including five media-access policy cases covering Android 10–17,
  selected photos, images-only/video-only access, denied access, and unrelated permissions.
- Five device tests passed: app context, transparent pixels/different bitmap density,
  hardware bitmap export, GPS/camera/date/comment/XMP removal and EXIF orientation,
  and the existing media-grouping scaling test at 1,000, 5,000, 10,000 and 20,000 synthetic records.
- The scaling test measures data processing, not scrolling a real 20,000-photo library.
- Updated toolbar check: Photos, Albums, Search and Bin keep a compact title in the
  top bar after scrolling. One title scales down and moves toward the leading edge.
  Date headings scroll inline rather than remaining pinned.
- The optimized APK was exercised through viewer → editor → metadata-removal button.
  The generated PNG contained IHDR/sBIT/sRGB/IDAT/IEND and no EXIF or text/XMP chunks.
  Temporary exported test copies were removed; source photos were not modified.
- Landscape layout opened successfully and the previous rotation settings were restored.
- Test-only instrumentation APK was removed after checks.

## Earlier scrolling baseline (before the collapsing-toolbar update)

Same Pixel and existing media library, six pairs of 400 ms upward/downward swipes.
Photos was foregrounded; the What's new card was dismissed before the final measurement.
`dumpsys gfxinfo` was reset immediately before each sample.

| Build | Frames | Janky frames | p50 | p90 | p95 | p99 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Original debug preview | 430 | 7 (1.63%) | 8 ms | 14 ms | 26 ms | 46 ms |
| Updated debug | 395 | 9 (2.28%) | 8 ms | 14 ms | 21 ms | 29 ms |
| Updated optimized preview | 349 | 1 (0.29%) | 5 ms | 7 ms | 8 ms | 10 ms |

This measures the tested device/library and includes the effect of optimization;
it does not prove zero jank for every library, file type, temperature or device.
The updated layout also changes available scroll distance.

Repeat with Photos open at the top:

```powershell
.\tools\Test-GalleryScroll.ps1
```

The script rejects background/empty samples and flags p95 above 32 ms.
Raw reports and device-test output are saved in `C:\Graphene\build-output`.

## Metadata action

The shield button first displays a warning with Cancel and Save clean copy.
Cancel was verified to create no file. Confirmation was verified to export a PNG
without EXIF/XMP/text chunks. The shield button exports a new pixel-only PNG into `Pictures/Private`
using a fresh filename. It preserves the decoded orientation, dimensions and transparency.
It does not forward the source EXIF/GPS/XMP, filename or gain map. The original is retained.
This is a static SDR PNG export, not lossless preservation of RAW/animated/HDR containers.
Normal image-format information such as dimensions and color-space chunks remains.

## Startup

Only visual media permissions are requested. Selected-photo access is accepted on
Android 14+, and permission denial leaves an explanation and retry button.
Location, notifications, media management and all-files access are no longer offered
or required by onboarding. Optional features elsewhere retain their own declarations.

## Collapsing-toolbar / branding update

- Seven device UI regression cases passed again on the final optimized APK:
  persistent compact title in Photos, Albums, Search, Bin and Library; returning from empty
  Selfies via Android Back; returning from Screenshots via Back to search.
- Search cancellation now rejects late results from cancelled CPU-bound filters.
- Search does not animate its whole scaffold as a shared bound; filtered date headers
  have no placement animation or competing sticky copy.
- Settings → Gallery opens Authors & contributors with the requested introduction,
  ReFra, GrapheneOS Gallery and Contributors acknowledgements and repository links.
- All locale launcher labels are Gallery. The legacy package/launcher component names
  remain for update compatibility; the visible ReFra branding was replaced.
- The supplied SVG is retained in `design/gallery-logo.svg`. Launcher, monochrome and
  splash assets are native vectors. Four frame groups (top, left, right and bottom)
  animate toward the stationary landscape using overshoot interpolation.
- Splash animation was recorded and inspected frame by frame on Pixel 10.
- Six JVM tests passed; XML resource values and all locale launcher labels were checked.
- Only generated test exports were removed. Existing user exports and photos were kept.
- Final UI artifacts and the latest scroll report are under
  `C:\Graphene\build-output\gallery-toolbar-final-tests` and
  `C:\Graphene\build-output\gallery-collapsing-toolbar-gfx.txt`.

Re-run the new UI regression sequence (English UI, Pixel 10 resolution):

```powershell
.\tools\Test-GalleryToolbar.ps1
```

Latest collapsing-toolbar scroll sample on the Pixel: 401 rendered frames,
0 janky frames, p50 5 ms, p90 7 ms, p95 8 ms, p99 9 ms. This is a bounded
on-device sample, not a guarantee of zero jank for every situation.

Collapsed-title rotation was checked in landscape and back in portrait. The title
stays visible and retains the correct scale. Previous device rotation settings were restored.
Final seven-case results: `C:\Graphene\build-output\gallery-complete-ui-tests.txt`.


## Gallery 4.2.2 preview

- Published the previous tested redesign first as `5e3190f` before starting these changes.
- Header pre-scroll observes gestures without consuming the grid's movement. The new
  short-gesture regression moved a photo 81 px while the title remained expanded;
  previously that motion was spent entirely on the header.
- One shared Haze source per content container feeds the floating navigation and
  the entire selection panel. No per-thumbnail blur layers were added.
- The selection panel has readable padding/backdrop, horizontal scrolling and a
  Metadata action for selected images. Batch work runs sequentially on IO, releases
  each Glide target/bitmap and reports saved/total, including partial failures.
- Cancel was checked with a fresh dialog and created no file. Two selected 3000×4000
  photos produced two PNG copies. Generated copies contained no EXIF/XMP/text chunks.
  Only new test copies were deleted; the pre-existing export was retained.
- Appearance was rebuilt around the current Gallery grid and four-tab glass navigation,
  with system/neutral/custom accents, theme and visual-effects controls.
- Authors now credits ifny75 and links to that profile. The upstream acknowledgements remain.
- Release discovery, a Photos banner, download confirmation and a manual settings
  check were added. Compatibility/malformed input/URL/downgrade policy has five new
  JVM tests. See `updates.md` for publishing the release manifest and update limits.
- Eleven JVM tests passed. Five device tests passed, including the real sequential
  MediaStore export of rotated, GPS-tagged fixtures with source-byte preservation,
  and rejection of an unreadable input. Test fixtures/exports and instrumentation APK
  were removed after testing.
- Seven toolbar/search navigation cases passed on the 4.2.2 optimized build. Appearance,
  Authors and selection-panel layout were inspected on the final APK.
- Final APK signature verified, package `com.dot.gallery.debug`, actual version code
  `421024`, version `4.2.2-preview`; installed over the existing test app without clearing data.
- Final Pixel 10 scroll sample with blur: 486 rendered frames, 1 janky frame (0.21%),
  p50 9 ms, p90 11 ms, p95 12 ms, p99 14 ms. This is a bounded device/library sample,
  not a guarantee for every device or collection.

Final reports: `C:\Graphene\build-output\gallery-422-final-gfx.txt`,
`gallery-422-final-short-scroll.txt`, `gallery-422-device-tests.txt`,
`gallery-422-toolbar-results.txt`. Re-run the short-gesture regression in English,
portrait orientation at the Pixel test resolution:

```powershell
.\tools\Test-GallerySimultaneousScroll.ps1
```


## Gallery 4.2.3 preview

Trash and Metadata are pinned to the beginning of the horizontal action list, so
both are visible immediately after selection. The existing Trash confirmation was
opened for two selected photos on Pixel 10 and cancelled; originals were not deleted.
Eleven JVM tests passed, optimized APK signature verified, and version 421034 was
installed over the previous preview without clearing data.

A genuine GitHub update offer was verified from installed 4.2.2 to published 4.2.3,
including the download confirmation and cached banner. The APK and release manifest
are published in v4.2.3-preview. Gallery/Calculator are also hosted on valanium.com/apps;
public APK downloads were read fully and checked against their SHA-256 values.


## Gallery 4.2.4 preview

What’s new now lists the actual ifny75/Gallery fork releases (4.2.1–4.2.4), with
English/Russian descriptions and a link to that repository’s commit history.
The upstream ReFra release list was removed from this screen. Entries show full
text and use static cards rather than non-functional tutorial buttons.

The optimized preview build and lintVital passed; every new release-string reference
was checked against both locale resources. The APK signature verified and the update
manifest was generated from the actual signed APK (version code 421044).
An on-device check was attempted but ADB reported no connected devices; no installation
or visual device verification is claimed for this version.

## Gallery 4.2.5 preview — scrollbar headers and crop saving

Validated on the connected Pixel 10 / Android 17 on 2026-10-25.

- Headers observe the active LazyGridState / LazyListState, including scrollbar
  jumps. Photos, Albums, Search, Library and Bin use the same position binding;
  unrelated legacy album toolbars retain their existing behavior.
- A scrollbar drag starting near the beginning moved All pictures from bounds
  [353,348][832,461] to [110,220][420,293]. The selected thumb/date bubble was
  captured during the drag. Reverse scrolling deep in the grid changed the
  visible photos while the compact title bounds stayed unchanged.
- The short-scroll regression passed: the same photo moved 80 px while the title
  partially collapsed. The script now requires an expanded starting header and
  no longer expects the title to snap open after a short gesture.
- Crop-only saving passed through the editor UI in both debug and optimized
  preview builds. A 640x480 fixture produced copies of 623x466 and 626x470 after
  small handle drags and Save Copy, without pressing Apply crop or adding filters.
  The original remained 640x480. Save was disabled before editing and enabled
  after changing the frame. Temporary fixtures and generated copies were removed.
- 14 JVM tests passed, including three header-position regressions. Three device
  CropTest cases passed: one-pixel crop plus PNG round-trip, unchanged/subpixel
  frame detection, and safe bottom-right bounds. The test APK was uninstalled.
- Optimized preview installed successfully as com.dot.gallery.debug,
  version 4.2.5-preview / actual versionCode 421054. APK signature verified.
- Six scroll cycles: 882 rendered frames, p95 8 ms, 0 deadline-janky frames;
  legacy gfxinfo classification reports 23 janky frames (2.61%). This is a
  device/sample measurement, not a guarantee for every library or device.
- assemble and lintVital passed. Full lint remains failing: 1,641 errors,
  mostly MissingTranslation (1,593), plus existing API/resource/Compose findings.
  Four new release-note strings intentionally have English/Russian translations;
  other locales fall back to English. Full lint is not claimed clean.

The release manifest is generated from the actual signed APK. Updates continue
using ifny75/Gallery GitHub Releases, never upstream ReFra releases.

## Gallery 4.2.6 preview — full first row after a fast return

The reported clipping was captured on the connected Pixel before any navigation.
Scaffold reuses its PaddingValues object while resizing the top bar. Timeline
remembered a concrete copy keyed by that object, so the grid could retain the
compact top inset after the header expanded. Both regular and mosaic timelines
now read current inset values. Position-bound headers also ignore nested-scroll
height writes, leaving measured list position as the single height owner; the
Appearance toolbar explicitly retains gesture-driven behavior.

`tools/Test-GalleryTopReturn.ps1` performs repeated fast flings down and back to
start, then checks the expanded header, first-photo position and complete square
bounds. Use portrait Photos with English UI and a populated library. Set the
layout to Mosaic and pass `-Mosaic` to skip the square-shape assertion there.
The script does not create or delete media, or change preferences.

Validation on Pixel 10 / Android 17, 2026-10-25:

- Regression failed on 4.2.5: first photo top 520 px, expanded header bottom
  635.5 px (overlap). The bug was reproduced before installing the fix.
- Debug: three fast-return cycles passed with the release card, three without
  it (first photo top 814 px), and three in Mosaic (806 px). Original Grid
  preference restored after Mosaic verification.
- Optimized 4.2.6-preview: three cycles passed without the release card;
  complete first row starts at 814 px below the same 635.5 px header boundary.
  Before and after screenshots were captured locally, not committed as they
  contain the device photo library.
- Short-scroll regression passed: photo moved 160 px while the title partially
  collapsed. Fourteen JVM tests passed. assemble and lintVital passed; full lint
  was not rerun (the existing full-lint limitations are recorded above).
- Six scroll cycles: 896 frames, p95 10 ms, one deadline-janky frame (0.11%);
  legacy classification: 91 frames (10.16%). Sample-specific, not a zero-jank
  guarantee. This passes the existing p95 <=32 ms regression threshold.
- Signed optimized APK installed over the previous preview, package
  com.dot.gallery.debug, actual versionCode 421064. Signature verified. No user
  media were modified. The temporary keep-awake override was restored.
