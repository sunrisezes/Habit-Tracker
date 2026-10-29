# Aura — Luxury Habit & Ritual Architecture

Aura is an ultra-refined, Apple-caliber personal ritual and habit system for Android that pairs tactile minimalism, precision typography, and physics-driven micro-interactions with deep behavioral analytics, an integrated Pomodoro focus timer, and guided daily reflections.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and product choices have been confirmed from your preferences and integrated with the core specification:

- **Confirmed Decision 1 — Local Room SQLite with JSON Backup & Restore**: All relational entities (`Habits`, `Categories`, `CompletionHistory`, `JournalEntries`, `UserSettings`, `Achievements`, and `StatisticsCache`) are persisted locally on-device using Room SQLite with reactive Kotlin `Flow` streams, paired with full one-tap JSON file export and import via Android's Storage Access Framework (`CreateDocument` / `OpenDocument`) for zero-friction backup and restore.
- **Confirmed Decision 2 — Guided Reflections with Mood, Energy Tags & Habit Links**: The Journal workspace provides structured daily reflection prompts, a 5-point emotional valence & energy matrix, and direct relational tags linking journal entries to specific completed or missed habits on that day.
- **Confirmed Decision 3 — Built-In Pomodoro Focus Timer + Scheduled Reminders**: Habits with estimated durations can launch directly into an elevated, distraction-free Pomodoro & countdown focus sheet with ring progress, haptic milestones, and auto-completion logging upon timer finish, complemented by configurable local notification reminders.
- **Native Android Execution**: Built natively with Kotlin, Jetpack Compose, Material 3 custom design tokens, and Room SQLite for authentic 60fps spring animations, adaptive phone/tablet/desktop layouts, and hardware-accelerated Canvas charts.

---

## 1. Overview & Core Concept

- **What It Does**: Aura transforms daily habit tracking from a utilitarian checklist into an intentional, calming daily ritual. Users curate habits across custom categories, track multi-target or timed completions with interactive progress rings, inspect multi-month consistency heatmaps, reflect in a habit-linked journal, and analyze deep behavioral trends.
- **Target Audience / Persona**: Discerning professionals, creatives, and mindfulness-oriented individuals who appreciate the visual restraint of Things 3, Linear, Apple Health, and Notion Calendar, and want deep personal analytics without visual clutter or gamified noise.
- **Key Value**: Unites four often-fragmented workflows—habit tracking, timed deep-work focus sessions, emotional/energy journaling, and longitudinal consistency analytics—into a single cohesive, privacy-first local database.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Dashboard & Daily Ritual Flow**:
   - Opens with a time-aware editorial greeting, formatted current date, curated philosophical quote card, and a hero **Daily Completion Ring** displaying today's completion percentage, active weekly progress bar, and monthly streak badge.
   - Displays today's scheduled habits filterable by category or time of day. Each **Habit Card** showcases its custom icon, title, concise description, category pill, difficulty & priority badges, estimated duration, reminder indicator, current streak, and an interactive progress ring / completion checkbox.
   - Users can **tap the completion ring** for multi-step or single-tap spring completion with custom Canvas particle burst and haptic feedback, **swipe right** to complete/increment, **swipe left** to launch the **Pomodoro Focus Sheet** or snooze, or **long-press** to open a contextual action menu (Edit, Focus Timer, Add Journal Note, Archive, Delete).
2. **Add & Edit Habit Studio Flow**:
   - Accessible via the floating Quick Add button or bottom sheet modal.
   - Provides a clean, sectioned form with live preview card: Title, Description, curated Vector Icon Picker, muted Luxury Color Picker, Category selector (with inline custom category creator), Frequency engine (`Daily`, `Weekly`, `Monthly`, `Custom` days of week), Target Count per day, Reminder time toggle & picker, Start Date & optional End Date, Difficulty (`Easy`, `Medium`, `Hard`), Priority (`Low`, `Medium`, `High`), Estimated Duration (minutes), and personal Notes.
3. **Interactive Calendar & Heatmap Flow**:
   - Displays a full monthly/annual **Consistency Heatmap** alongside a scrollable month calendar where each date cell renders a proportional **Completion Circle** ring.
   - Tapping any historical or current date dynamically updates the inspection pane below to reveal **Completed Habits** (with completion timestamp and notes), **Missed Habits** (with one-tap retroactive logging), and any **Journal Reflections** recorded on that date.
4. **Statistics & Behavioral Intelligence Flow**:
   - Features hardware-accelerated custom Canvas visualizations: a smooth **Completion Trends Line/Area Chart** (7D / 30D / 90D / 1Y), **Overall Success Rate**, **Current Streak** & **Longest Streak** hero metrics, **Consistency Score** gauge, **Category Breakdown** horizontal bars, **Time-of-Day Productivity Analysis**, **Most Productive Day of Week** & **Most Productive Month** callouts, and an unlockable **Achievements Gallery** (e.g., 7-Day Spark, 30-Day Ritualist, Deep Focus Master, Mindful Scribe).
5. **Guided Journal & Mood Flow**:
   - Presents daily guided prompts (e.g., *"What gave you energy today?"*, *"Which ritual felt most meaningful?"*), a tactile **Mood & Energy Selector** (Calm, Focused, Energized, Reflective, Drained), selectable chips linking the entry to today's habits, and a chronological timeline of past reflections with search and mood filtering.
6. **Settings, Theme & Data Portability Flow**:
   - Houses user **Profile** customization (Display Name, Daily Intention, Avatar monogram), live animated **Theme Switcher** (`Light`, `Dark`, `System`), **Notifications & Quiet Hours** preferences, **Backup & Restore** (one-tap JSON Export, JSON Import with merge/replace confirmation, and sample ritual seeding/reset), **Privacy** overview (100% local SQLite guarantee), and **About** specifications.

### Visual Identity & Theme

- **Aesthetic Direction**: *Quiet Luxury & Editorial Precision*—combining Apple Health's tactile data rings, Things 3's serene whitespace and paper-like cards, and Linear's crisp dark-mode contrast and hairline borders.
- **Color Palette & Mood**:
  - **Light Mode**: Warm Alabaster background (`#FAF9F5`), elevated Pure Ivory card surfaces (`#FFFFFF`) with subtle warm stone borders (`#EAE6DF`), Deep Slate primary ink (`#14151A`), and Muted Charcoal secondary text (`#636672`).
  - **Dark Mode**: Near-Black Obsidian background (`#0B0C0E`), elevated Graphite card surfaces (`#15171C`) with subtle translucent hairline borders (`#242832`), Crisp Alabaster primary ink (`#F4F4F6`), and Muted Pewter secondary text (`#8E929F`).
  - **Semantic Accents**:
    - *Primary*: Deep Indigo (`#3F4E8C` Light / `#6878C4` Dark)
    - *Accent*: Muted Emerald (`#1E7A5F` Light / `#34A884` Dark)
    - *Success*: Soft Sage Green (`#2E8B57`)
    - *Warning*: Warm Amber (`#C88A2B`)
    - *Danger*: Muted Terracotta Red (`#B84A4A`)
- **Typography & Hierarchy**:
  - Locally bundled Google Fonts via `font-util`: **Plus Jakarta Sans** for clean, geometric UI headers and body readability, **Instrument Serif** (or **Playfair Display**) for editorial greetings and motivational quotes, and **JetBrains Mono** for tabular numeric alignment on streaks, percentages, countdown timers, and analytics metrics.
- **Component Styling & Layout**:
  - **Adaptive Layout Engine**: Uses `BoxWithConstraints` to transition seamlessly between single-column vertical scroll with bottom navigation bar on **Phones**, two-column split dashboard/detail cards on **Tablets**, and a persistent left **Navigation Rail** with multi-pane grid on **Foldables/Desktop** windows.
  - **Reusable Design System**: Standardized spacing tokens (`4.dp` to `48.dp`), corner radii (`12.dp` compact, `20.dp` card, `28.dp` modal), subtle multi-layered elevation shadows, and frosted translucent surface overlays for bottom bars and floating sheets.
- **Interactive Feedback & Motion**:
  - Physics-based `spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)` animations on habit card completion, progress ring sweeps, theme crossfades (`animateColorAsState`), staggered list item entrances, and smooth chart path drawing.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Relational Room SQLite with Reactive Statistics Cache**:
  - *Chosen Approach*: Store normalized entities in Room (`habits`, `categories`, `completion_history`, `journal_entries`, `user_settings`, `achievements`, `statistics_cache`) and expose combined domain models via Kotlin `Flow` in a dedicated Repository layer, automatically updating `statistics_cache` and `achievements` whenever completions or journal entries change.
  - *Why*: Guarantees instantaneous 60fps UI updates, sub-millisecond historical date queries on the Calendar heatmap, and clean portability to JSON or server-side SQL schemas.
  - *Alternatives Considered*: On-the-fly unindexed list filtering in memory was rejected to ensure scalable performance over thousands of historical completion records.
- **Decision 2 — Integrated Non-Blocking Pomodoro Focus Sheet**:
  - *Chosen Approach*: Embed a stateful Pomodoro/duration timer managed by the ViewModel so users can start a timed habit session (e.g., "20 min Reading"), minimize it into a persistent floating mini-bar while browsing their Journal or Stats, and automatically log the habit completion when the countdown finishes.
  - *Why*: Turns the "Estimated Duration" field on every habit card into an actionable, value-generating ritual tool rather than static metadata.
  - *Alternatives Considered*: Static duration labels were rejected as less engaging; locked full-screen timers were rejected so users can jot journal thoughts while a session runs.
- **Decision 3 — Zero-Permission SAF JSON Backup & Restore**:
  - *Chosen Approach*: Use Android's native `ActivityResultContracts.CreateDocument("application/json")` and `OpenDocument()` to export and import complete database snapshots (including habits, categories, completion logs, journal reflections, settings, and achievements) plus a one-tap instant local snapshot backup/restore slot.
  - *Why*: Complies strictly with Google Play privacy policies (zero broad storage permissions required) while giving users full ownership of their data.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────────┐
│                     Adaptive Compose UI Layer (60 FPS)                     │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌─────────┐ ┌────────┐ │
│  │  Dashboard   │ │   Calendar   │ │  Statistics  │ │ Journal │ │Settings│ │
│  │  & Rituals   │ │  & Heatmap   │ │  & Insights  │ │ & Mood  │ │& Backup│ │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └────┬────┘ └───┬────┘ │
│         ┴────────────────┴────────────────┴──────────────┴──────────┴      │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │          Reusable Luxury Design System & Motion Components           │  │
│  │  • AuraCard / GlassSurface   • AnimatedProgressRing   • SwipeHabitRow│  │
│  │  • AddEditHabitSheet         • PomodoroFocusSheet     • CanvasCharts │  │
│  └──────────────────────────────────┬───────────────────────────────────┘  │
└─────────────────────────────────────┼──────────────────────────────────────┘
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                    Presentation & Domain State Layer                       │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                        AuraHabitViewModel                            │  │
│  │  • UI StateFlow Aggregation      • Streak & Consistency Calculator   │  │
│  │  • Pomodoro Timer Coroutine      • Achievement Unlock Evaluator      │  │
│  │  • Date & Category Filters       • JSON Backup / Restore Serializer  │  │
│  └──────────────────────────────────┬───────────────────────────────────┘  │
└─────────────────────────────────────┼──────────────────────────────────────┘
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                   Data & Persistence Layer (Room SQLite)                   │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                         HabitRepository                              │  │
│  └────┬───────────┬─────────────┬─────────────┬────────────┬────────────┘  │
│       ▼           ▼             ▼             ▼            ▼               │
│  ┌─────────┐ ┌──────────┐ ┌───────────┐ ┌──────────┐ ┌──────────────────┐  │
│  │  Habits │ │Categories│ │Completion │ │ Journal  │ │Settings, Stats & │  │
│  │  Table  │ │  Table   │ │  History  │ │  Table   │ │Achievements Table│  │
│  └─────────┘ └──────────┘ └───────────┘ └──────────┘ └──────────────────┘  │
└────────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State

1. **`HabitEntity` (`habits`)**:
   - `id`, `title`, `description`, `iconName`, `colorHex`, `categoryId`, `frequencyType` (`DAILY`, `WEEKLY`, `MONTHLY`, `CUSTOM`), `customDaysCsv`, `targetCount`, `reminderEnabled`, `reminderTime` (`HH:mm`), `startDateEpochDay`, `endDateEpochDay`, `notes`, `difficulty` (`EASY`, `MEDIUM`, `HARD`), `priority` (`LOW`, `MEDIUM`, `HIGH`), `estimatedMinutes`, `isArchived`, `createdAt`.
2. **`CategoryEntity` (`categories`)**:
   - `id`, `name`, `iconName`, `colorHex`, `sortOrder` (Pre-seeded with *Mindfulness*, *Health & Fitness*, *Deep Work*, *Learning*, and *Evening Ritual*).
3. **`CompletionHistoryEntity` (`completion_history`)**:
   - `id`, `habitId`, `dateEpochDay`, `completedCount`, `targetCountSnapshot`, `completedAtTimestamp`, `durationMinutesSpent`, `note`. Indexed by `(habitId, dateEpochDay)` for O(1) daily lookup and streak computation.
4. **`JournalEntryEntity` (`journal_entries`)**:
   - `id`, `dateEpochDay`, `timestamp`, `promptTitle`, `content`, `mood` (`CALM`, `FOCUSED`, `ENERGIZED`, `REFLECTIVE`, `DRAINED`), `energyLevel` (1–5), `linkedHabitIdsCsv`, `tagsCsv`.
5. **`UserSettingsEntity` (`user_settings`)**:
   - `id = 1`, `userName`, `userBioOrIntention`, `themeMode` (`SYSTEM`, `LIGHT`, `DARK`), `notificationsEnabled`, `quietHoursStart`, `quietHoursEnd`, `soundEffectsEnabled`, `hapticsEnabled`, `lastBackupTimestamp`.
6. **`AchievementEntity` (`achievements`)**:
   - `id`, `code`, `title`, `description`, `iconName`, `unlockedAtTimestamp`, `progressCurrent`, `progressTarget`.
7. **`StatisticsCacheEntity` (`statistics_cache`)**:
   - `id = 1`, `overallSuccessRate`, `currentGlobalStreak`, `longestGlobalStreak`, `consistencyScore`, `mostProductiveDayOfWeek`, `mostProductiveMonth`, `totalCompletions`, `totalFocusMinutes`, `updatedAtTimestamp`.

### Interactive Component & State Mapping

- **Habit Completion & Swipe Gestures**:
  - Tapping a habit card's progress ring or swiping right invokes `viewModel.incrementHabitCompletion(habit, dateEpochDay)`. If `completedCount + 1 == targetCount`, triggers a celebratory ring sweep, increments streak metrics, recalculates `StatisticsCacheEntity`, checks achievement thresholds, and persists atomically in Room.
  - Long-pressing any habit card opens a modal context sheet with direct actions to **Start Focus Timer**, **Log Journal Note for Habit**, **Edit Habit**, **Reset Today's Progress**, or **Delete Habit**.
- **Add / Edit Habit Validation & Frequency Scheduling**:
  - Validates non-empty title, target count $\ge 1$, and valid date range (`endDate >= startDate` when specified). Custom frequency habits only appear in "Today's Habits" when the selected day of the week matches or when "Show All Scheduled Habits" is toggled.
- **Calendar Date Selection & Retroactive Logging**:
  - Selecting any date cell on the Calendar screen updates `selectedCalendarDate: StateFlow<LocalDate>`. The detail list below partitions active habits into **Completed** and **Missed / Pending** for that specific date and allows toggling completions retroactively with automatic streak recalculation.
- **Pomodoro Focus Session Lifecycle**:
  - Launching a timer from a habit card sets `activeFocusSession: StateFlow<FocusSessionState?>` with `RUNNING`, `PAUSED`, or `COMPLETED` states. A coroutine ticker decrements remaining seconds at 1Hz; completing the session logs the elapsed minutes and increments the habit's completion count for today.
- **JSON Export, Import & Local Snapshot**:
  - Exporting serializes all 7 tables into a structured, human-readable JSON bundle and writes it to the user-selected URI or internal snapshot file. Importing validates the JSON schema and performs a transactional `@Transaction` upsert across all Room tables.
