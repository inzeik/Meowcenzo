# Meowcenzo offline player: drop-in code

Kotlin + Jetpack Compose + Media3. No internet permission, no third-party image library.

## Install (5 steps)
1. Copy everything under `app/src/main/java/com/example/meowcenzo/` into your project.
   If your package is not `com.meowcenzo.music`, rename the package lines and imports.
   Replace your existing `MainActivity.kt` and `ui/theme/Theme.kt` with these.
2. Copy `res/drawable/ic_meow_fg.xml`, `res/values/meow_colors.xml` and the two
   `res/mipmap-anydpi-v26/*.xml` files (rectangular launcher icon).
3. Paste `app/build.gradle.kts.dependencies.txt` into your `dependencies { }`.
4. Merge `AndroidManifest.additions.xml` into your manifest (permissions + `PlaybackService`).
5. Run on a device with music files. Tap "Allow access".

Needs minSdk 24+ and a recent compileSdk (35 or newer).

## What you get
| Idea from | Feature here |
|---|---|
| SoundCloud | Waveform scrubber (tap or drag), gradient progress, recently played row |
| Apple Music | Now Playing tinted from album art, art shrinks when paused, queue sheet, Play next / Add to queue |
| Samsung Music | Songs / Albums / Artists / Playlists / Folders tabs, sort menu, favourites |
| Groove Music | Bold album tiles, Most played list, minimal chrome |
| Player basics | Shuffle, repeat off/all/one, speed 0.75x to 2x, sleep timer, gapless, lock-screen and Bluetooth controls, pauses on unplug |
| Audio | System equalizer with presets, per-band sliders, bass boost (settings saved) |
| Library | Offline MediaStore scan, search, rescan button, playlists, history, play counts |

## The rectangle rule
Every tappable thing is a rounded rectangle: `RectButton`, `GradientPlayButton` (a wide tile),
tabs, artwork, slider thumb, waveform bars, the playing-bars indicator and the launcher icon.
Tokens live in `ui/theme/Theme.kt` (`Meow.TileSmall / Tile / TileLarge`). Change one radius there
and the whole app follows.

## Not verified
- I could not open your repo's `app/` folder (GitHub blocks automated access), so package name,
  theme name and existing files are assumed from a standard Compose template.
- This was written without a Kotlin compiler, so expect a few small import or version fixes on first build.

## Good next steps
Restore the queue after restart, drag-to-reorder queue, lyrics (.lrc beside the file),
ContentObserver to auto-rescan, crossfade, Room if playlists grow large, custom font in `res/font`.
