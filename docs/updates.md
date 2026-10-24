# Gallery releases and update notices

Gallery checks the public releases of `ifny75/Gallery` in the background at most once
per 12 hours after a successful check. Settings also has a manual check. Offline
failures are silent during startup. No photo data, account credentials or device IDs
are sent. Cached offers remain available when returning to Photos; Later dismisses
that version. Stable builds ignore prereleases; preview/debug builds accept them.

Each release intended for in-app discovery must contain `gallery-update.json` and
its signed APK. A manifest entry must match the installed application ID and exact
BuildConfig flavor, with a strictly higher APK version code. Unsupported schemas,
malformed filenames, other repositories and non-HTTPS asset links are ignored.
Use the actual APK code (ABI codes are multiplied), not the base Gradle version.

```powershell
python tools/Create-UpdateManifest.py --apk C:\Graphene\build-output\Gallery-4.2.2-preview.apk --aapt2 "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.0.0\aapt2.exe" --variant arm64-v8aNoML --output C:\Graphene\build-output\gallery-update.json
```

The script inspects the APK and includes its SHA-256 for independent verification.
For a multi-variant release, combine the generated entries in one `updates` array.
Upload the exact named APK and manifest as release assets. Sign future updates
with the same signing key and keep the same package. The installed testing build
uses `com.dot.gallery.debug`; a release for `com.dot.gallery` cannot update it.
Do not commit keystores or signing passwords.

The top Photos banner opens a confirmation dialog. Download APK opens the exact
GitHub release asset in the user's browser; Android handles installation and
signing checks. Gallery does not silently install packages or request installer
permissions at startup. The app does not itself verify the downloaded APK's SHA-256;
the checksum in the manifest is available to release tooling/users.

Network access must be present and allowed for checks. Upstream CI jobs which strip
INTERNET produce intentionally offline builds and cannot check releases.

The initial 4.2.2 preview establishes this release contract. A newer compatible
published release is required to exercise a genuine upgrade; no fictitious version
is advertised for testing.


## In-app release notes

What’s new lists the history of `ifny75/Gallery`, starting with this fork’s 4.2.1
redesign. It is stored offline in `HelpRepository.ALL_RELEASES` with descriptions
in English and Russian resources (`gallery_release_*`). Add an entry when changing
the application version and use the fork’s actual changes/date. Do not reintroduce
the upstream ReFra release history as the current fork changelog. The screen links
to `https://github.com/ifny75/Gallery/commits/main/` for the complete commit history.
