Notes App

Android application developed during a university Android Development course.

The project was built incrementally throughout a semester and consists of 13 laboratory assignments. Each new version extended the previous one, gradually introducing modern Android development technologies and practices.

Project Evolution
Version	Features
v1.0.0	Basic OOP demo
v2.0.0	First Jetpack Compose interface
v3.0.0	Compose collections and UI improvements
v4.0.0	Reactive interface and dynamic content updates
v5.0.0	Screen navigation, bottom navigation and tabs
v6.0.0	MVVM architecture and ViewModels
v7.0.0	Material Design 3 and Dark Theme
v8.0.0	Room Database and DataStore
v9.0.0	REST API integration and asynchronous networking
v10.0.0	Form validation, keyboard handling and adaptive layouts
v11.0.0	Animations, Swipe-to-Dismiss, Pull-to-Refresh and context menus
v12.0.0	Camera integration and GPS functionality
v13.0.0	Unit testing and UI testing
Features
Note Management
Create notes
Edit notes
Delete notes
Search notes
Favorite notes
Sort notes
Filter favorites
Organize notes into folders
Note Properties

Each note can contain:

Title
Content
Folder category
Favorite status
Source URL
Estimated hours
Priority level
Attached photo
GPS coordinates
Available Folders
Study
Work
Personal
Ideas
Offline Support
Local Room database storage
REST API synchronization
Cached data available when offline
Network status handling
Device Features
Camera
Take photos directly from the application
Attach photos to notes
Local image storage using FileProvider
GPS
Get current device location
Save coordinates to notes
Display location information
Calculate distance to Chernivtsi National University
User Experience
Material Design 3
Dark Theme
Light Theme
Onboarding flow
User profile settings
Pull-to-Refresh
Swipe-to-Dismiss
Adaptive layouts for tablets and large screens
Technology Stack
Language
Kotlin
UI
Jetpack Compose
Material Design 3
Navigation Compose
Architecture
MVVM
Repository Pattern
Storage
Room Database
DataStore Preferences
Networking
Retrofit
Gson
Concurrency
Kotlin Coroutines
Flow
StateFlow
SharedFlow
Media
Coil
Android APIs
Camera
FileProvider
Fused Location Provider
Architecture Overview
Jetpack Compose UI
        │
        ▼
    ViewModel
        │
        ▼
   Repository
    ├─ Room Database
    └─ Retrofit API
Project Structure
app/src

├── androidTest/
│   └── UI tests
│
├── main/
│   ├── AndroidManifest.xml
│   │
│   ├── java/com/mtg/notes/
│   │   ├── DeviceUtils.kt
│   │   ├── MainActivity.kt
│   │   ├── MainTabScreen.kt
│   │   ├── MainViewModel.kt
│   │   ├── Navigation.kt
│   │   ├── NoteDao.kt
│   │   ├── NoteDetailsViewModel.kt
│   │   ├── Notes.kt
│   │   ├── NotesDatabase.kt
│   │   ├── NotesRepository.kt
│   │   ├── OnboardingScreens.kt
│   │   ├── ProfileViewModel.kt
│   │   ├── SettingsRepository.kt
│   │   │
│   │   ├── network/
│   │   │   ├── ApiClient.kt
│   │   │   ├── NetworkNote.kt
│   │   │   ├── NetworkResult.kt
│   │   │   └── NoteApiService.kt
│   │   │
│   │   └── ui/theme/
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   │
│   └── res/
│
└── test/
    └── Unit tests
Testing
Unit Testing

Implemented using:

JUnit 4
MockK
Robolectric
Coroutines Test

Covered scenarios:

Successful note creation
Form validation
Loading a non-existing note
UI Testing

Implemented using:

Jetpack Compose Testing

Covered scenarios:

Creating a note through the user interface
Requirements
Android 7.0+ (API 24+)
Internet connection (for API synchronization)
Camera permission (optional)
Location permission (optional)
