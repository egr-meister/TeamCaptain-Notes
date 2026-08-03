# TeamCaptain Notes

**TeamCaptain Notes** is a native Android app for amateur football team captains. It helps you
organize players, track attendance, manage a match checklist, and keep team notes before and after
the match — all stored locally on your device.

> Main question the app answers: **"How is my team prepared for the next match?"**

A secondary **Match Schedule** screen can load football matches from the
[football-data.org](https://www.football-data.org) API v4 as an extra reference. This is a
supporting convenience feature only — it is **not** a live-score app, **not** a betting app, and
**not** an official football app.

---

## Features

- **Player list** — add, edit, delete players; shirt number, preferred position, captain note, active/inactive; sort by name or number.
- **Match records** — create match notes with date, time, opponent, venue, team mood, pre-match notes, post-match notes and a short result summary.
- **Attendance tracking** — mark each player Present / Absent / Late / Unknown per match, with optional notes and live present/absent/late counts.
- **Match tasks** — a pre-match checklist (e.g. bring armband, bring ball, warm-up leader); assign a task to a player, mark complete.
- **Team notes** — pre-match, tactical, motivational and post-match notes stored per match.
- **Team mood** — one mood per match (Great / Good / Neutral / Low / Tense), shown as a badge.
- **Match history** — reverse-chronological cards with attendance and task summaries; open, edit or delete.
- **Match Detail** — one screen with mood, attendance, tasks, notes and result.
- **Match Schedule (secondary)** — football-data.org matches for a default 10-day window with manual refresh and local caching.
- **Settings** — captain preferences, schedule settings, cache/data management, disclaimers and privacy note.

### Captain notes disclaimer

> TeamCaptain Notes is a manual football captain helper app. Player attendance, match tasks, team
> notes, mood, and post-match notes are entered by the user. The app is not an official football
> organization tool and does not provide professional coaching advice.

### Match schedule API disclaimer

> Match data is provided by football-data.org. Availability, accuracy, competitions, and update
> frequency depend on the API provider and the current API plan.

---

## What this app is *not*

No ads. No analytics. No payments. No account registration. No Firebase. No cloud sync.
No betting, no odds, no bookmakers, no predictions, no gambling language. No official club or league
logos and no copied branding — team names appear only as plain text from the API. No real player
photos. No location, no notifications, no sensors, no Google Fit, no Health Connect, and no wearable
integration. The app performs **no automatic background tracking** of any kind.

---

## football-data.org API v4

The Match Schedule feature calls a single endpoint:

```
GET https://api.football-data.org/v4/matches?dateFrom=<today>&dateTo=<today+9days>
Header: X-Auth-Token: <FOOTBALL_DATA_API_TOKEN>
```

Only the `/matches` endpoint is used. The app never calls odds, predictions, bookmaker or
live-betting endpoints. All API code is isolated in
`data/remote/FootballDataRepository.kt` and `data/remote/FootballDataApiService.kt`.

### Default 10-day date window

By default the app requests matches from **today through today + 9 days** (a 10-day window). Dates
are formatted as `YYYY-MM-DD` and calculated locally from the device clock — no fixed dates are ever
hardcoded. A 10-day window keeps upcoming matches visible while staying small and controlled. You
can override the window in **Match Schedule settings**; leaving both dates empty restores the
default window.

### API usage policy

The free plan is rate-limited, so the app is deliberately conservative:

- No automatic polling and no background refresh. Refresh is **manual** (a button) or once per open
  if the cache is old.
- The latest successful response is cached locally and shown on next open.
- Friendly messages are shown when the token is missing, the rate limit is reached (HTTP 429), there
  is no internet, or the response is invalid. The rest of the app keeps working regardless.

---

## API token setup (`local.properties`)

The API token is **never** hardcoded in source and **never** committed to Git. It is read from
`local.properties` (or a CI environment variable) and exposed through generated `BuildConfig` fields:

- `BuildConfig.FOOTBALL_DATA_API_TOKEN`
- `BuildConfig.FOOTBALL_API_BASE_URL`

1. Get a free token at <https://www.football-data.org/client/register>.
2. Copy `local.properties.example` to `local.properties`.
3. Fill in your token:

   ```properties
   FOOTBALL_DATA_API_TOKEN=your_real_token_here
   FOOTBALL_API_BASE_URL=https://api.football-data.org/v4
   ```

> **Never commit `local.properties` or your real API token** to GitHub, screenshots, tests or CI
> logs. `local.properties`, keystores and `*.p12` files are already in `.gitignore`.

### Demo data fallback

If `FOOTBALL_DATA_API_TOKEN` is missing, empty or still equals `your_api_token_here`, the app uses
bundled **demo match data** (neutral placeholder team names — no real clubs, logos or photos), shows
a friendly message on the Match Schedule screen, and never blocks the rest of the app.

---

## Local storage

All user data is stored **locally only**, using **DataStore Preferences** with a single JSON string
serialized by **Kotlinx Serialization**. No database (Room) is used — simple JSON is enough.

Stored data: players, match records, attendance records, tasks, settings, onboarding flag, match
schedule settings, cached match schedule, last API update time, last API error, and the last
requested `dateFrom` / `dateTo`.

DataStore reads are defensive: empty storage, missing fields, and corrupted JSON all fall back to
valid defaults, so the app never crashes on load. Enums use safe fallbacks for unknown values.

### Internet & the INTERNET permission

The app declares exactly one permission — `INTERNET` — used **only** by the secondary Match Schedule
screen to reach football-data.org. It requests no runtime permissions and uses no location, camera,
microphone, contacts, storage/gallery, notifications, calendar, alarms, sensors, Google Fit, Health
Connect or wearable access.

---

## Visual design — "Green-Blue Captain Board"

A sporty, clean, practical captain planner built around player cards, a task checklist, attendance
chips, mood badges and a green-blue identity. Colors: strong green `#1F7A4D`, deep green `#17633E`,
strong blue `#2E3F8F`, deep blue `#243477`, soft panels, white content cards, and dark navy/charcoal
for contrast. Yellow is used only for warnings; red only for destructive actions. No casino gold, no
betting-slip layout, no deposit/balance/freebet visuals, no neon glow, no heavy gradients, no
official branding, no player photos.

**Layout uniqueness** — the app deliberately avoids the generic "mascot → title → subtitle → stats →
stack of big buttons → settings" template. The Home screen is a captain board: a green-blue header,
a next/latest match card, a compact three-stat row, a quick-action grid, and a small, non-dominant
Match Schedule card. The Match Schedule screen is intentionally simpler and secondary.

### App icon

A custom adaptive launcher icon: a rounded green/blue background with a white checklist/clipboard and
a captain armband accent plus a small football. Generated PNGs are provided for all densities plus an
adaptive `mipmap-anydpi-v26` icon. No default Android icon, no logos, no photos, no betting symbols.

### Splash screen

A custom splash (AndroidX SplashScreen) with a deep-green background and a centered white
checklist/armband vector — no default Android splash, no heavy assets, no copied branding.

---

## Project structure

```
app/src/main/java/com/teamcaptain/notes/
├── TeamCaptainApp.kt            # Application + simple manual DI container
├── MainActivity.kt             # Splash install + Compose entry point
├── data/
│   ├── model/Models.kt         # Serializable data classes + safe enums
│   ├── local/LocalRepository.kt# DataStore JSON storage (defensive)
│   └── remote/
│       ├── FootballDataApiService.kt   # Retrofit /matches interface
│       ├── FootballDataRepository.kt   # Fetch + normalize + FootballApiResult
│       ├── DemoMatches.kt              # Offline demo fallback
│       └── dto/FootballDtos.kt         # Nullable-safe API DTOs
├── domain/Summaries.kt         # Null-safe summary/statistics helpers
├── util/AppUtils.kt            # Ids, dates, validation
└── ui/
    ├── AppViewModel.kt         # All local CRUD (shared)
    ├── MatchScheduleViewModel.kt
    ├── AppText.kt              # Disclaimers / privacy copy
    ├── theme/Theme.kt
    ├── components/             # BoardCard, chips, scaffold, etc.
    ├── navigation/AppNavGraph.kt
    └── screens/                # 12 screens
```

Architecture: simple MVVM. One repository for local data, one repository for the football-data.org
API, ViewModels per concern, and a small UI-state class for the schedule screen. No over-engineering.

---

## Build & run

### Open in Android Studio

1. Install **Android Studio** (latest stable) with the **Android SDK Platform 35** and
   **Build Tools 35.0.0**.
2. `File → Open` and select the project root. Android Studio generates the Gradle wrapper JAR on
   first sync.
3. Copy `local.properties.example` to `local.properties` and add your API token (optional — demo
   data is used without it).
4. Run the `app` configuration on a device/emulator (Android 7.0 / API 24 or newer).

### Android configuration

- `compileSdk = 35`, `targetSdk = 35`, `minSdk = 24`.
- Kotlin + Jetpack Compose + Material 3 + Navigation Compose + Coroutines + ViewModel.
- DataStore Preferences + Kotlinx Serialization for storage; Retrofit + OkHttp for the API.
- Gradle Kotlin DSL, Android Gradle Plugin 8.6.x, Kotlin 2.0.x, Gradle 8.9.

### 16 KB page-size compatibility

The AAB targets Android 15+ **16 KB memory page sizes**. The app bundles **no native libraries**, and
`packaging { jniLibs { useLegacyPackaging = false } }` is set, so there are no legacy `.so`
alignment issues.

### Release optimization (R8 / shrink)

R8 minification and resource shrinking are enabled for release
(`isMinifyEnabled = true`, `isShrinkResources = true`) with `proguard-android-optimize.txt` plus
`app/proguard-rules.pro` (keep rules for Kotlinx Serialization models and Retrofit). **First verify a
non-minified release build** (`isMinifyEnabled = false`, `isShrinkResources = false`) before enabling
shrinking, and re-test launch after enabling it.

---

## Signing & Google Play

Release APK and AAB must be signed with a **real PKCS12 keystore**, never a debug key. The
`release` build type uses `signingConfigs.release` when a keystore is provided via environment
variables; otherwise a plain local `assembleRelease` falls back to debug signing for smoke tests only
(CI always provides the real keystore).

### 1. Generate a PKCS12 keystore

```bash
keytool -genkeypair -v -storetype PKCS12 \
  -keystore teamcaptain-notes-release-key.p12 \
  -alias teamcaptain_notes_key \
  -keyalg RSA -keysize 2048 -validity 10000
```

Use the **same password** for the keystore and the key. Keep this file and its passwords private —
never commit them.

### 2. Add GitHub Secrets

In your repo: **Settings → Secrets and variables → Actions → New repository secret**:

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | `base64 -w0 teamcaptain-notes-release-key.p12` output |
| `ANDROID_KEYSTORE_PASSWORD` | keystore password |
| `ANDROID_KEY_ALIAS` | `teamcaptain_notes_key` |
| `ANDROID_KEY_PASSWORD` | key password (same as keystore) |
| `FOOTBALL_DATA_API_TOKEN` | *(optional)* your API token |

Encode the keystore for the secret:

```bash
base64 -w0 teamcaptain-notes-release-key.p12   # Linux
base64 -i teamcaptain-notes-release-key.p12     # macOS
```

### 3. GitHub Actions

`.github/workflows/android-build.yml` runs on push to `main` and:

1. Sets up JDK 17 and the Android SDK.
2. Installs `platforms;android-35` and `build-tools;35.0.0`.
3. Creates `local.properties` from secrets (or safe placeholders — build still succeeds without an
   API token).
4. Decodes the keystore from `ANDROID_KEYSTORE_BASE64`.
5. Builds the **signed release APK** and **signed release AAB**.
6. **Verifies the APK signature** with `apksigner verify --print-certs`, prints the certificate, and
   **fails the workflow if the certificate contains `CN=Android Debug`** — preventing Google Play
   rejection from a debug-signed artifact.
7. Uploads the APK and AAB as build artifacts.

> **Google Play upload target is the `.aab` only**, not the `.apk`.

CI is responsible for a fast, stable, signed build only. No emulator smoke-test runs on free runners.

---

## Local launch verification checklist

A green CI build is **not** proof the app launches. Before release, install the release APK on a real
device or emulator and check `adb logcat` for the absence of crashes:

```bash
./gradlew :app:assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
adb logcat
```

Confirm none of these occur: `ClassNotFoundException`, `NoSuchMethodError`, serialization crash,
DataStore JSON parse crash, missing navigation argument crash, missing player/match/task/attendance
crash, invalid mood/date/time crash, invalid API response crash, missing API token crash, or a
signature misconfiguration.

Manual test flow: first launch with empty storage → onboarding → add/edit/delete players → create a
match → mark and update attendance → add and complete tasks → change mood → pre/post notes → open
history → edit/delete match → open Match Schedule with **no** token (demo) and **with** a token →
confirm the default request uses today + 9 days → manual refresh → simulate API failure (airplane
mode) → check cached matches → clear cache → reset all data → relaunch. Confirm the manifest requests
**only** `INTERNET`.

---

## Privacy note

> TeamCaptain Notes stores player lists, attendance records, match tasks, team mood, notes, settings,
> and cached match data on this device. The app uses internet only to load football match data from
> football-data.org. No account, no ads, no analytics, no payments, no Firebase, no location, no
> notifications, no sensors, no Google Fit, and no Health Connect.

---

## License / attribution

Match data is provided by [football-data.org](https://www.football-data.org). This app is an
independent, unofficial captain helper and is not affiliated with, endorsed by, or connected to any
football club, league, or governing body.
