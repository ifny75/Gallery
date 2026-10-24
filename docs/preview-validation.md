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
