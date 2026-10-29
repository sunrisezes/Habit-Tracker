<div align="center">

# Aura — Mindful Habit, Ritual & Focus Studio

**An ultra-clean, tactile Android application for intentional daily rituals, deep focus timers, consistency heatmaps, and guided reflections.**

[![Download APK](https://img.shields.io/badge/⬇_Download_APK-Latest_Release_(v1.0.0)-312E81?style=for-the-badge&logo=android&logoColor=white)](https://github.com/sunrisezes/Habit-Tracker/releases/latest/download/Aura-Habit-Tracker.apk)
[![Live Preview](https://img.shields.io/badge/▶_Try_Live_in_Browser-AI_Studio_Preview-0F766E?style=for-the-badge&logo=googlechrome&logoColor=white)](https://ais-pre-snxhnrn2imimepehnru5s2-586190855991.asia-southeast1.run.app)

[![Platform](https://img.shields.io/badge/Platform-Android%20(API%2024%2B)-0F766E?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Kotlin-2.0%2B-312E81?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20(Material%203)-4F46E5?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Storage-Room%20SQLite%20%2B%20JSON%20Vault-10B981?style=flat-square&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)

[Direct APK Download](https://github.com/sunrisezes/Habit-Tracker/releases/latest) · [Live Web Preview](https://ais-pre-snxhnrn2imimepehnru5s2-586190855991.asia-southeast1.run.app) · [Report Issue](https://github.com/sunrisezes/Habit-Tracker/issues)

</div>

---

## Download & Install (Android APK)

Anyone can download and install **Aura** directly from this repository:

1. Click **[Download `Aura-Habit-Tracker.apk` (Latest Release)](https://github.com/sunrisezes/Habit-Tracker/releases/latest/download/Aura-Habit-Tracker.apk)** or visit the **[Releases Page](https://github.com/sunrisezes/Habit-Tracker/releases)**.
2. Open the downloaded `.apk` file on your Android phone (Android 7.0 / API 24 or newer) to install.

---

## Overview

**Aura** reimagines habit tracking with the calm precision of a luxury editorial journal and the analytical depth of modern health dashboards. Designed without noisy gradients, cluttered chrome, or gamified distractions, Aura helps you build sustainable daily rituals through kinetic completion rings, an integrated **Time Studio** (Countdown Timer, Centisecond Stopwatch, and Daily Alarm Clock), multi-week consistency heatmaps, and a guided mindfulness journal.

---

## Key Features

### 1. Intentional Daily Dashboard
- **Dynamic Completion Ring**: Real-time daily completion percentage with smooth spring-animated progress strokes, current streak, best streak, and total focused minutes.
- **Tactile Habit Cards**:
  - One-tap completion ring with multi-step target progress (`1/1`, `2/4`, etc.).
  - Swipe right to complete a ritual; swipe left to open the Habit Studio editor.
  - Long-press context sheet for quick editing, launching a focus session, archiving, or deleting.
  - Per-card quick actions for **Timer**, **Stopwatch**, and **Set Alarm**.
- **Instant Filtering & Search**: Filter rituals by category (*Mindfulness*, *Health & Vitality*, *Deep Work*, *Fitness*, *Learning*, *Evening Wind-down*) or search by keyword.

### 2. Integrated Time Studio (Timer, Stopwatch & Alarm)
- **Countdown Focus Timer**:
  - 7 instant duration presets (`5m`, `10m`, `15m`, `25m`, `30m`, `45m`, `60m`) plus fine-tune steppers (`-5m`, `-1m`, `+1m`, `+5m`, `+10m`).
  - Live session controls (Start/Pause, Reset, `+1m`/`+5m` extensions) and a collapsible floating mini-player so you can navigate the app while your timer runs.
  - **Save & Complete Ritual**: Logs focused minutes directly into your habit's daily log.
- **Precision Stopwatch**:
  - Centisecond accuracy (`MM:SS.cc`) with **Lap split recording** (delta split + total time) and one-tap **Log Elapsed Time to Habit**.
- **Daily Alarm & Reminder Studio**:
  - Custom time steppers (`-1h`, `-15m`, `-5m`, `+5m`, `+15m`, `+1h`) and quick time presets (`06:30` to `21:30`).
  - 4 curated alarm chime styles: *Zen Tibetan Bowl*, *Soft Singing Bell*, *Morning Dew Pulse*, and *Silent Haptic Only* with live chime preview and **+10m Snooze**.
  - Native Android `AlarmClock` integration to register system-level alarms directly from the app.

### 3. Habit Creation & Customization Studio
- Rich customization with **16 curated vector icons** and **8 desaturated editorial color tokens**.
- Flexible scheduling (**Daily**, **Weekly**, **Monthly**, **Custom** days of the week).
- Configurable **Difficulty** (*Easy*, *Medium*, *Hard*), **Priority** (*Low*, *Normal*, *High*), **Target Count**, **Estimated Duration**, and **Personal Intention Notes**.

### 4. Interactive Calendar & 12-Week Heatmap
- **Monthly Completion Ring Calendar**: Every day renders a proportional completion ring indicating exact ritual progress for that date.
- **12-Week Consistency Matrix**: GitHub/Linear-inspired 84-day activity grid with 5 intensity levels.
- **Historical Day Audit**: Tap any past or present date to inspect completed vs. missed rituals and retroactively toggle completion logs directly from the calendar.

### 5. Deep Analytics & Milestones
- **Completion Velocity Chart**: Interactive 7-day, 30-day, and 90-day smooth Bezier area chart built with Jetpack Compose `Canvas`.
- **Category Breakdown**: Proportional progress bars and completion ratios across all life domains.
- **Productivity Insights**: Automatically surfaces your **Consistency Score**, **Most Productive Day of the Week**, **Peak Month**, and **Total Focused Hours**.
- **Milestone Badges**: Unlock achievements (*First Spark*, *7-Day Rhythm*, *14-Day Momentum*, *30-Day Mastery*, *Deep Focus Initiate*, *Reflective Mind*).

### 6. Guided Reflection Journal
- Structured daily prompts with **Mood Selector** (*Radiant*, *Calm*, *Focused*, *Reflective*, *Stretched*), **5-Point Energy Level**, and **Linked Ritual Tags** to correlate how daily habits affect your mental clarity and energy.

### 7. Local-First Privacy & JSON Vault
- **100% Offline-Capable**: Powered by **Room SQLite** with reactive Kotlin `Flow` streams—zero mandatory cloud accounts or tracking.
- **Portable JSON Backup & Restore**: Export your entire database (habits, completion history, journal entries, and preferences) to a human-readable `.json` file via Android's Storage Access Framework, or save/restore an instant local snapshot.

---

## Design System & Typography

Aura implements a custom design system (`AuraTheme` & `AuraComponents`) built atop Material 3:

| Token | Light Mode (Alabaster Editorial) | Dark Mode (Obsidian Studio) |
| :--- | :--- | :--- |
| **Canvas Background** | `#FAF8F5` (Warm Alabaster) | `#0B0C10` (Near-Black Obsidian) |
| **Elevated Surface** | `#FFFFFF` (Pure Crisp Card) | `#14161D` (Carbon Slate) |
| **Primary Brand** | `#312E81` (Deep Indigo) | `#818CF8` (Luminous Indigo) |
| **Accent / Focus** | `#0F766E` (Botanical Emerald) | `#2DD4BF` (Mint Emerald) |
| **Hairline Border** | `#EAE6DF` (Warm Stone) | `#222634` (Muted Graphite) |

- **Editorial Display Font**: *Playfair Display* — used for greetings, hero titles, and philosophical quotes.
- **UI & Body Font**: *Plus Jakarta Sans* — clean geometric legibility for cards, controls, and navigation.
- **Tabular Metrics Font**: *JetBrains Mono* — ensures zero layout jitter on live timers, stopwatches, percentages, and streak counters.

---

## Architecture & Tech Stack

The project follows **Clean MVVM Architecture** with a single-activity Compose navigation structure and unidirectional data flow:

```text
app/src/main/java/com/example/
├── MainActivity.kt                  # Edge-to-edge host, adaptive Bottom Bar / Navigation Rail
├── data/
│   ├── AuraEntities.kt              # Room entities (Habit, HabitRecord, JournalEntry, UserProfile) & enums
│   ├── AuraDao.kt                   # Reactive Flow queries & transactional upserts
│   ├── AuraDatabase.kt              # Room database configuration
│   └── AuraRepository.kt            # Streak calculation, analytics aggregation, JSON export/import
├── viewmodel/
│   └── AuraViewModel.kt             # Consolidated UI state, Time Studio engine (Timer/Stopwatch/Alarm)
└── ui/
    ├── components/
    │   └── AuraComponents.kt        # Reusable cards, rings, badges, heatmaps, Canvas illustrations
    ├── screens/
    │   ├── DashboardScreen.kt       # Home view, Time Studio quick bar, swipeable habit cards
    │   ├── PomodoroFocusSheet.kt    # 3-tab Timer, Stopwatch & Alarm Studio modal + mini-player
    │   ├── AddEditHabitSheet.kt     # Full habit creation & editing modal sheet
    │   ├── CalendarScreen.kt        # Monthly completion ring calendar & 12-week heatmap
    │   ├── StatisticsScreen.kt      # Bezier charts, category breakdown, milestone badges
    │   ├── JournalScreen.kt         # Guided mood/energy reflections linked to habits
    │   └── SettingsScreen.kt        # Theme switcher, profile, notifications, JSON backup/restore
    └── theme/
        ├── Color.kt                 # Curated light/dark semantic palette & habit accent colors
        ├── Type.kt                  # Bundled Playfair Display, Plus Jakarta Sans & JetBrains Mono
        └── Theme.kt                 # Animated color transitions & custom composition locals
```

---

## Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 11** or higher
- **Android SDK 36** (minimum supported device API 24 / Android 7.0+)

### Clone & Run Locally

1. **Clone the repository**
   ```bash
   git clone https://github.com/sunrisezes/Habit-Tracker.git
   cd Habit-Tracker
   ```

2. **Open in Android Studio**
   - Launch Android Studio and select **Open** → choose the `Habit-Tracker` folder.
   - Wait for Gradle sync to finish, then click **Run (`▶`)** on an emulator or physical Android device.

3. **Build APK from Terminal**
   ```bash
   gradle assembleDebug
   ```
   The compiled APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

### Running Automated UI & Unit Tests
```bash
gradle :app:testDebugUnitTest
```

---

## License

Distributed under the MIT License. Created by [@sunrisezes](https://github.com/sunrisezes).
