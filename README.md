# LowKeyboard

**English** · [Русский](README.ru.md)

A lightweight multilingual on-screen keyboard (IME) for Android 4.0.4 – 7.1 (API 15–25).
Pure Java and the Android SDK only: no third-party libraries, no `INTERNET` permission,
everything you type stays on the device.

## Features

- **24 layouts out of the box**
  - Cyrillic: Russian, Ukrainian, Belarusian, Bulgarian, Serbian
  - Greek
  - Latin: English, German, French, Spanish, Portuguese, Italian, Polish, Czech, Turkish,
    Swedish/Finnish, Norwegian/Danish, Romanian, Hungarian, Indonesian, Vietnamese
  - Right-to-left: Arabic, Hebrew, Persian
- **Smart defaults** – English plus the system language; the keyboard starts in the system language.
  The set follows the system language until you choose languages yourself
  (a button in the settings restores the default).
- **Language switching** with a globe key that cycles through the enabled languages. The key is
  hidden when only one language is enabled. The current language name is shown on the space bar.
  The last used language is remembered.
- **Long-press the space bar** to open the system keyboard picker.
- **Long-press a letter** for accented and alternative characters (`a` → `à á â ä …`, `е` → `ё`, …).
  Long-press `.` / `,` / `-` for more punctuation, hold Delete to repeat.
- **Number row** – always visible by default. Optionally it can be shown only on the symbols layout
  (digits then appear as small hints on the top letter row; long-press to type a digit).
  In that mode the keyboard height never changes and symbols are split into two pages (`=\<` / `?123`).
- **Themes**: Holo, Material Light, Material Dark, MIUI 5, Flyme.
- **Key height** slider (30–60 dp).
- **Enter key** adapts to the editor action: magnifier (search), arrow (next/go), check mark (done).
- Shift with caps lock (double tap) and automatic capitalisation at the start of a sentence.
- **Hide the app icon** from the launcher (settings stay available in
  *Settings → Language & input → LowKeyboard*).
- The whole keyboard is drawn on a single `Canvas`; layouts and themes are stored in code, so
  the APK stays small.

### Not included

No suggestions, autocorrect or dictionaries, no clipboard manager or emoji panel, and no
languages that need composing input (Chinese, Japanese, Korean) or complex scripts
(Hindi, Thai, …).

## Installation

1. Install the APK (`adb install app-release.apk` or open it on the device).
2. Open **LowKeyboard** and tap **1. Enable the keyboard**, then **2. Select the keyboard**.
3. Pick languages, theme and key height in the app.

Settings are applied the next time the keyboard is shown.

### Building into the system image (preinstalled app)

- Put the APK into `/system/app/LowKeyboard/LowKeyboard.apk` (permissions `644`).
- The keyboard declares an `en_US` keyboard subtype so the system treats it as a regular IME;
  by default it offers English plus the current system language.
- To make it the default keyboard, set it in your ROM's default input method configuration.

> The behaviour on a specific firmware (automatic enabling, default selection) depends on the
> Android version and ROM and should be checked on the target device.

## Building

Requirements: JDK 17+ and the Android SDK (compileSdk 34).

```
./gradlew assembleRelease
```

The APK is written to `app/build/outputs/apk/release/`.

- `minSdk 15`, `targetSdk 25`, Java 8 source level, no dependencies.
- The release build uses R8 (`minifyEnabled`, `shrinkResources`) and keeps only `en` and `ru`
  string resources (`resConfigs`).
- The release build is signed with the **debug key** so that it can be installed right away.
  Replace `signingConfig` in `app/build.gradle` with your own key before distributing it.
- Lint for `ExpiredTargetSdkVersion` is disabled because `targetSdk 25` is intentional.

## Project layout

```
app/src/main/java/com/m929/lowkeyboard/
  KbService.java       input method service (InputConnection, editor actions, auto caps)
  KbView.java          the keyboard: layout, drawing, touch handling, popups
  Layouts.java         all layouts and long-press characters (generated, see tools/)
  Theme.java           colour schemes and dimensions
  GlobeIcon.java       globe icon as Path commands (generated from an SVG, see tools/)
  SettingsActivity.java  settings screen
tools/
  gen_layouts.py       generates Layouts.java
  svg2java.py          converts the globe SVG path to GlobeIcon.java
```

### Adding a language

Edit the table at the top of `tools/gen_layouts.py` (code, name, three letter rows, long-press
characters, extra symbols) and regenerate:

```
python3 tools/gen_layouts.py app/src/main/java/com/m929/lowkeyboard/Layouts.java
```

### Adding a theme

Add an entry to `Theme.ALL` and a name to `Theme.NAMES` in `Theme.java`.

## Settings storage

Preferences are kept in the `kb` SharedPreferences file: `mask2` (enabled languages), `lang2`
(current language), `theme`, `keyH` (key height, dp), `digSym` (number row on symbols only).
