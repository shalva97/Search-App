# Portal: The "Browser Bar" for Your Phone

**The Problem:** The average user has 80+ apps. Finding one often involves swiping through screens, opening folders, or using slow system search tools that prioritize web results over local apps.

**The Solution:** A lightning-fast, minimalist utility that treats your app library like a browser. You don't "go" to an app; you "search" it into existence.

### 1. Key Features

- **Instant Focus:** Opening the app immediately triggers the keyboard. No tapping a search bar first.
- **Fuzzy Matching:** Type "nfx" to find *Netflix* or "msg" for *Messages*. Prefix boosting and usage-based scoring prioritize your most relevant apps.
- **Dynamic Startup View:** A compact 4x2 grid of your most recently opened apps appears by default when the search bar is empty.
- **Integrated Settings:** A gear icon within the search bar provides quick access to app management features.
- **App Privacy & Visibility:** Hide distracting or private apps from search results and the home grid via the Settings menu.
- **Contextual Actions (Long-Press):**
    - **Hide App:** Instantly remove an app from the search interface.
    - **Uninstall:** Launch the system uninstallation flow directly.
    - **Open in Play Store:** Jump to the app's store page for updates or reviews.
- **Recently Installed:** A dedicated view in Settings shows apps installed in the last hour, making it easy to find and test new downloads.
- **Invisible UX:** The app is excluded from Android's recents list and uses `singleInstance` launch mode to ensure a clean, one-shot interaction.
- **Done-to-Launch:** Press the "Done" key on your keyboard to instantly open the first search result.

### 2. Technical Architecture (MVVM & Kotlin)

Following **Google’s Guide to App Architecture**, the app is built with a separation of concerns to ensure testability and performance.

- **UI Layer (Jetpack Compose):** A reactive, modern UI that handles state-driven layouts (Grid vs. List) and custom window insets for stable keyboard transitions.
- **Domain Layer (ViewModel):** Manages UI state, coordinates search logic on `Dispatchers.Default`, and handles navigation and app-launch intents.
- **Data Layer (Repository):** The single source of truth, managing a Room database that indexes installed packages.
- **Background Tasks (WorkManager):** Performs periodic incremental indexing to keep the local database synchronized with the system's package manager.

### 3. Tech Stack & Libraries

- **Language:** 100% Kotlin with Coroutines and Flows for real-time reactivity.
- **Dependency Injection:** Hilt for modular and testable component management.
- **Local Storage:** Room Persistence Library (Schema Version 3) with `fallbackToDestructiveMigration` for rapid development.
- **Navigation:** Jetpack Navigation Compose for seamless transitions between Search, Settings, and Recently Installed screens.
- **Resources:** Vector-based adaptive icons and standard Android resource structures for high performance and low footprint.

### 4. Implementation Details

- **Incremental Indexing:** On startup, the app performs a fast delta-update of the database instead of a full refresh, significantly improving "Time to Interactive."
- **Search Scoring:** Results are ranked using a combination of fuzzy character matching, prefix boosting, and historical usage frequency.
- **State Persistence:** Search state resets automatically after an app is launched, ensuring the user is always greeted with a clean interface.
- **Zero Animation Policy:** Configured `adjustResize` and manual inset handling to eliminate jarring UI shifts when the keyboard appears.
- **CI/CD:** GitHub Actions workflow to automatically build and create releases with APK artifacts.

### 5. Development & CI/CD
- **Build System:** Gradle (Kotlin DSL).
- **GitHub Actions:**
    - Trigger: Push a tag starting with `v*` (e.g., `v1.0.1`) to automatically create a GitHub Release.
    - Artifacts: The workflow builds the `unsigned` release APK and attaches it to the release.
    - Manual Dispatch: Can also be triggered manually from the "Actions" tab.