# Notes App

A modern, feature-rich Android application for note-taking and task management. Built entirely with **Kotlin** and **Jetpack Compose**, this app demonstrates a clean MVVM architecture, robust offline-first capabilities, hardware integrations (Camera & GPS), and test-driven development practices.

## ✨ Key Features

- **Advanced Note Management**: Create, edit, delete, and favorite notes. Includes support for tracking estimated hours, setting priority levels, and attaching external source URLs.
- **Smart Organization**: Categorize notes into dedicated folders (`Study`, `Work`, `Personal`, `Ideas`). Includes global search and filtering (e.g., show favorites only, sort ascending/descending).
- **Offline-First Architecture**: 
  - Seamlessly works offline using a local **Room** database.
  - Automatically synchronizes with a remote API (MockAPI via Retrofit) when the network is available.
  - Custom mapping between network DTOs and local database entities.
- **Hardware Integrations**:
  - 📸 **Camera**: Take and securely attach photos directly to your notes using `FileProvider`.
  - 📍 **Geolocation**: Tag notes with your current location using the `FusedLocationProviderClient`. Includes a custom feature to calculate and display the real-time distance to a specific landmark (e.g., Chernivtsi National University).
- **Modern UI/UX**:
  - Fully built with Jetpack Compose and **Material Design 3** (including dynamic color support).
  - Reactive state management using `StateFlow` and `SharedFlow`.
  - Toggleable List and Grid views.
  - Dark and Light theme support.
  - Adaptive layout for expanded screens (Tablet support using `WindowSizeClass`).
- **Onboarding & Personalization**: Personalized greeting and user name setup stored securely via DataStore Preferences.

## 🛠️ Tech Stack & Libraries

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles.
- **Local Storage**: 
  - [Room Database](https://developer.android.com/training/data-storage/room) (SQLite abstraction)
  - [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) (Theme, sorting, and user settings)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & Gson (REST API integration)
- **Concurrency & Reactive**: Kotlin Coroutines, `Flow`, `StateFlow`, `SharedFlow`
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Location Services**: Google Play Services Location API
- **Testing**: JUnit 4, MockK, Robolectric, Jetpack Compose UI Testing

## 🧪 Testing

The application includes a comprehensive testing suite to ensure reliability and correct behavior:
- **Unit Tests**: ViewModels (e.g., `NoteDetailsViewModel`) are tested using **JUnit 4** and **Robolectric**. Dependencies like the `NotesRepository` are mocked using **MockK** to isolate and verify business logic, state updates, and form validation rules.
- **UI Tests**: Automated UI tests utilize the **Jetpack Compose Testing** library to verify user flows, such as navigating the app, inputting text, and successfully creating new notes.

## 🚀 Getting Started

### Prerequisites
- Android Studio (Latest version recommended)
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36

### Installation & Running
1. Clone the repository:
   ```bash
   git clone [https://github.com/your-username/your-repo-name.git](https://github.com/your-username/your-repo-name.git)
   ```
2. Open the project in Android Studio.
3. Sync the Gradle files.
4. Build and run the app on an emulator or a physical device.

Note: Ensure you grant the necessary Camera and Location permissions when prompted in the app to test all hardware-related features.

---

## 📂 Architecture Overview
The application strictly follows the recommended Android Architecture guidelines:
- UI Layer: Composed of Jetpack Compose screens and ViewModels. State is exposed to the UI via StateFlow, ensuring a unidirectional data flow (UDF).
- Data Layer: The NotesRepository acts as the single source of truth. It manages data fetching and synchronization between the local cache (NoteDao via Room) and the remote data source (NoteApiService via Retrofit), handling network failures gracefully. Data mapping is handled cleanly between NetworkNote and local Note models.
