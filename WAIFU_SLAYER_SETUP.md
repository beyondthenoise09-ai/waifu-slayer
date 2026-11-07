# WAIFU SLAYER — Baseline build setup (Phase 2/3)

Base project: SimpMusic (maxrave-dev), GPL-3.0. Original LICENSE, `core/LICENSE`, CODE_OF_CONDUCT and
attribution files are untouched. Package/application ID is unchanged (`com.maxrave.simpmusic`; the
**debug** build type appends `.dev` upstream, so the debug APK installs as `com.maxrave.simpmusic.dev`).

## What changed in this baseline (no app code or UI touched)
1. `core/` was an empty git-submodule folder. It now contains the supplied core-multiplatform source
   (16 modules, all paths in `settings.gradle.kts` verified to exist).
2. `.gitmodules` removed, because `core/` is now ordinary tracked source, not a submodule.
3. `.github/workflows/build-debug-apk.yml` replaced with a workflow that runs
   `:androidApp:assembleDebug -PisFullBuild=false` (uses the `-empty` stubs for Crashlytics, Cast and
   Last.fm so no secrets / google-services.json are needed).
4. Upstream workflows that need secrets or other platforms were moved to `.github/workflows-disabled/`
   (android.yml, android-release.yml, desktop-package.yml, pr-triage.yml). Not deleted.

## Not verified
- The exact upstream Core commit (ZIPs carry no git history).
- That the project compiles. See WAIFU_SLAYER_BUILD_REPORT.md.

## Branding step 1 (resources only, no UI redesign yet)
Logo source: the supplied glossy play-note logo. Only the symbol is used for the icon.
- Launcher: adaptive icon (background `#08050F`, foreground = logo symbol), legacy webp icons at all densities,
  monochrome (themed-icon) layer. Old `drawable/monochrome.xml` replaced by `drawable-nodpi/monochrome.png`.
- Splash: Android 12+ system splash (`values-v31/themes.xml`) and a layer-list window background for older versions.
  Wordmark + "BY CHANDAN NAYAK" cannot be shown by the system splash; they need a Compose splash (UI phase).
- App name "WAIFU SLAYER" (strings, manifest labels, welcome dialog text).
- Palette colours added in `values/waifu_colors.xml`. Compose theme seed changed to `#9B4DFF`
  (Material scheme is generated from it). Full palette/UI redesign is NOT done yet.
- Package ID, integrations, playback code untouched.

## Step 2: five WAIFU player styles (source written, NOT compiled)
New files in `composeApp/src/commonMain/kotlin/com/maxrave/simpmusic/ui/screen/player/content/`:
- `WaifuPlayerCommon.kt` (shared pieces), `WaifuPlayerStylesA.kt` (Classic, Aura, Minimal),
  `WaifuPlayerStylesB.kt` (Immersive, Glass), `WaifuPlayerStylePicker.kt` (picker with previews).
All five render from the same `NowPlayingContentState` / `NowPlayingContentActions` as the original styles,
so there is one playback engine, one queue, and switching style never touches playback.
Edited: `NowPlayingScreen.kt` (dispatch), `SettingScreen.kt` (opens the picker),
core `DataStoreManager.kt` (5 new style id constants) and `DataStoreManagerImpl.kt` (default = WAIFU_CLASSIC).
Original three styles stay selectable under "Original styles".
Limits: WAIFU styles show artwork only (no video surface / Spotify canvas; use an original style for those).
The waveform seek bar is a visual control, not real audio analysis.

## Not done yet
Home, Search, Library, Settings redesign, mini-player restyle, Compose splash with wordmark + byline.
