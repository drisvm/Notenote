# Notenote

**Your everyday information, all in one place.**

Notenote is a local-first Android app for storing everyday information: appliance models, Wi-Fi
details, vehicle information, measurements, reference numbers, maintenance records and more.
It is designed to feel like a personal notebook — cozy, fast, private by default.

## Features

### Notes
- Create, edit, delete, duplicate notes
- Pin, favorite, archive/restore
- Move notes between folders
- Per-note colors (8-color paper palette) and emoji icons
- Tags with chip UI
- Full-text search across titles, content and tags
- Sorting (last modified / created / title / reminder, ascending or descending)
- Long-press contextual actions and multi-select mode with bulk actions
- Automatic saving (debounced autosave + flush on exit, so nothing is lost when closing)

### Folders
- Create, rename, delete folders
- Emoji icons and colors per folder
- Nested (sub)folders, with children promoted on delete
- Deleting a folder **never** deletes its notes — you choose where the notes go:
  General notes or another folder

### Reminders
- Per-note date & time reminders with themed Material 3 pickers
- Delivered via WorkManager notifications (permission-aware on Android 13+)
- Reminders are rescheduled automatically after reboot
- Reminders overview screen with overdue highlighting

### Appearance
- Light / Dark / Follow-system themes
- Custom accent color (6 preset accents + RGB color picker)
- Subtle, efficient tiled paper-grain texture (toggleable in Settings)
- All dialogs are themed to match the app (confirmation, text input, color picker,
  emoji picker, folder editor, sort menu, date & time pickers)

### Privacy & backup
- 100% local storage (Room + DataStore). No network permission, no analytics, no accounts.
- Export/import a portable JSON backup via the Android file picker (SAF).

## Tech stack

- Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose
- Room database, DataStore preferences
- Coroutines & Flow, MVVM with a lightweight manual DI container
- WorkManager for reminders, kotlinx-serialization for backups
- minSdk 26 (Android 8.0), targetSdk 35

## Building

Open the project in Android Studio (Koala or newer), or:

```bash
./gradlew :app:assembleDebug     # debug APK: app/build/outputs/apk/debug/
./gradlew :app:assembleRelease   # release APK (R8-minified, unsigned)
./gradlew :app:testDebugUnitTest # unit tests
```

> **Wrapper note:** if `gradle/wrapper/gradle-wrapper.jar` is missing from your clone, let
> Android Studio fix the wrapper (it offers this on first sync) or run
> `gradle wrapper --gradle-version 9.2.1` once with any local Gradle installation.

## Monetization

- Ads: AdMob banner on the main screens (test IDs in debug builds, real unit IDs in release),
  with Google's UMP consent flow before any ad loads.
- Remove ads: one-time non-consumable in-app purchase, product ID `remove_ads`. Entitlement is
  stored in DataStore and restored on every app start via Play Billing
  (`queryPurchasesAsync`). Create the product in Play Console before releasing.

Install on a connected device/emulator:

```bash
./gradlew :app:installDebug
```

For a Play-ready release build, add your own signing config in `app/build.gradle.kts`.

## Project layout

```
app/src/main/kotlin/com/notenote/app/
├── data/           # Room entities/DAOs, repositories, DataStore settings, backup
├── domain/model/   # Note, Folder, list/sort models
├── reminders/      # ReminderScheduler, Worker, boot receiver
├── notification/   # Notification channel + builder
├── ui/
│   ├── theme/      # Colors, typography, shapes, paper texture
│   ├── components/ # Themed dialogs, note card, pickers
│   ├── navigation/ # Nav graph + bottom navigation
│   └── screen/     # list, editor, folders, reminders, settings
└── util/           # Date helpers
```

## Testing

Unit tests cover tag persistence conversion, backup JSON round-trips (including forward
compatibility), date/reminder helpers and note sorting (including pin behavior):

```bash
./gradlew :app:testDebugUnitTest
```
