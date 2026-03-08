# Portal: The "Browser Bar" for Your Phone

**The Problem:** The average user has 80+ apps. Finding one often involves swiping through screens, opening folders, or using slow system search tools that prioritize web results over local apps.

**The Solution:** A lightning-fast, minimalist utility that treats your app library like a browser. You don't "go" to an app; you "search" it into existence.

### 1. Key Features

- **Instant Focus:** Opening the app immediately triggers the keyboard. No tapping a search bar first.
- **Fuzzy Matching:** Type "nfx" to find *Netflix* or "msg" for *Messages*.
- **"Top Hits" Prediction:** Based on time of day and location, the app predicts your most likely destination (e.g., *Spotify* in the morning, *Uber* at 5 PM).
- **In-App Shortcuts (Deep Linking):** Search "New Tweet" to jump directly to the Twitter compose screen, or "Scan" to open the QR scanner in a banking app.
- **Browser-Style "Omnibox Spirit":** If an app isn't found locally, the search seamlessly transitions to a web search or a Play Store search.
- **Privacy First:** Local indexing only. No data leaves the device.

### 2. Technical Architecture (MVVM & Kotlin)

Following **Google’s Guide to App Architecture**, the app is built with a separation of concerns to ensure testability and performance.

### **Layered Architecture**

1. **UI Layer (View):** Built using **Jetpack Compose** for a reactive, modern UI. It observes state from the ViewModel.
2. **Domain Layer (ViewModel):** Manages UI state and business logic. It uses **Kotlin Flows** to stream search results to the UI in real-time.
3. **Data Layer (Repository):** The single source of truth. It abstracts the data sources (Local App Index vs. System Package Manager).

### **Tech Stack & Libraries**

- **Language:** 100% Kotlin with an emphasis on **Coroutines** for non-blocking search indexing.
- **Dependency Injection:** **Hilt** for standardizing DI across the app.
- **Local Storage:** **Room Persistence Library** to cache the app index and metadata (Usage frequency, last opened time).
- **Fuzzy Search Engine:** A custom Kotlin implementation or a lightweight library like *FuzzyWuzzy* ported to Kotlin to handle typo-tolerance.
- **Jetpack Libraries:**
    - **DataStore:** For storing user preferences (theme, layout settings).
    - **WorkManager:** For periodic background indexing of newly installed apps.
    - **Lifecycle:** To handle UI-related data in a lifecycle-conscious way.

### 3. UI Strategy

The UI should be **Invisible UX**—it only appears when needed and disappears the moment its job is done.

- **The "Zen" Home:** A completely blank screen with a single, elegant search line in the center or bottom (thumb-friendly).
- **Simple Backgrounds:** Transparent background.
- **One-Handed Layout:** All results appear at the bottom of the screen, within reach of the thumb.

### 4. Implementation Details

- **App Indexing:** On first launch, the app queries `PackageManager` to build a local database of installed packages and their `LauncherActivities`.
- **Performance:** The search query is debounced to avoid unnecessary database hits, ensuring the UI remains 60fps even with 500+ apps.