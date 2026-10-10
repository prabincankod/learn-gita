# Learn Gita — Android (`com.prasuco.gita`)

A calm, offline-first daily Bhagavad Gita app. One verse every morning, in plain
English — with reflections, scholar commentary, a home-screen widget, and
gallery-ready verse cards. Native Kotlin + Jetpack Compose. No accounts, no network.

## Features

- **Today** — daily lesson: hero, verse + saying, Listen (TTS) / Save / Save-image
  pills, reflections, Sivananda scholar take, past-week carousel
- **Chapters** — 18-chapter library with journey progress
- **Chapter detail** — verse grid with read / next-up / saved states
- **Verse reader** — translation + commentary source switcher, notes, sticky
  mark-read / save / save-image console
- **Search** — offline search plus a "What are you facing today?" dilemma engine
  (Anxiety, Grief, Indecision, Duty, Fear of Failure, Burnout)
- **Saved** — bookmarks with swipe-to-remove
- **Verse widget** — resizable 2×2–4×2, opens the verse; hourly refresh + boot/date
- **Reminder** — gentle morning notification with the day's verse (time picker)
- **Verse cards** — on-the-fly Canvas-rendered 1:1 + 9:16 images saved to gallery

## Build

```sh
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Requires Android SDK (`local.properties` → `sdk.dir`) and JDK 17.

Release builds are signed via `signingConfigs.release`, which reads (in order)
env vars (`SIGNING_KEY_STORE_PATH`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`,
`SIGNING_KEY_PASSWORD`) or `~/.gradle/gradle.properties` (`SIGNING_STORE_FILE`, …).
The upload keystore lives in `keystores/` and is **never committed**.

## Offline data

`tools/make_bundle.py` builds `app/src/main/assets/gita_bundle.json` (~7 MB):

- 18 chapters, 701 verses
- Translations: Purohit EN, Sivananda EN, Ramsukhdas HI, Tejomayananda HI
- Commentaries: Sivananda EN, Ramsukhdas HI

On first launch `Repository.ensureSeeded()` loads the bundle into Room (`gita.db`).
Regenerate with: `python3 tools/make_bundle.py`

## Project layout

```
app/src/main/java/com/prasuco/gita/
  GitaApp.kt  MainActivity.kt
  data/       Entities.kt  GitaDatabase.kt  Prefs.kt  Repository.kt  Sayings.kt
  ui/         Theme.kt  Routes.kt  Components.kt  ChapterArt.kt
              Onboarding.kt  HomeScreen.kt  ChaptersScreen.kt
              ChapterDetailScreen.kt  VerseScreen.kt  SearchScreen.kt  SavedScreen.kt
  audio/      VerseSpeaker.kt
  share/      ShareCards.kt
  widget/     Providers.kt  WidgetUpdater.kt
  work/       Work.kt   (WidgetRefreshWorker/Receiver, ReminderReceiver/Scheduler)
app/src/main/res/  layout/widget_verse{,_wide}.xml  drawable/widget_{card,pill}.xml
app/src/main/assets/gita_bundle.json
tools/make_bundle.py
```

One pattern: one screen per file, shared UI only in `Components.kt`.
