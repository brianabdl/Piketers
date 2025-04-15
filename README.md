# Piket Scheduler App

An Android app to manage and generate housekeeping (piket) schedules for your group, built with Jetpack Compose and Kotlin.

## Features

- Generate and view today's piket (housekeeping) assignments
- Randomly assign members to different tasks
- Send assignments to Telegram group chats
- Store Telegram bot settings securely

## Setup Instructions

### Requirements

- Android Studio Arctic Fox (2020.3.1) or newer
- Kotlin 1.5.31 or newer
- Android SDK 21+

### Telegram Bot Setup

1. Create a new Telegram bot using BotFather:
    - Open Telegram and search for `@BotFather`
    - Send `/newbot` command
    - Follow instructions to create your bot
    - Save the API token provided by BotFather

2. Get Chat ID:
    - Add your bot to the target group or start a conversation with it
    - In the app, go to Settings
    - Enter your bot token
    - Enter a username and use the search button to find the chat ID
    - Alternatively, enter the chat ID directly if you know it

### Building the App

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on your device or emulator

## Usage

1. **Home Screen**:
    - Shows the current day and assigned piket tasks
    - "Regenerate" button creates new random assignments
    - "Send to Telegram" button sends assignments to the configured Telegram chat

2. **Settings Screen**:
    - Enter your Telegram bot token
    - Enter the target chat ID or search for it by username
    - Settings are automatically saved

## Customization

To modify members or schedule, edit the appropriate data in `PiketRepository.kt`:

```kotlin
// Members list
private val members = listOf(
    "Mas Brian",
    "Mas Pras",
    "Mas Jojo",
    // Add or remove members here
)

// Schedule definition
private val schedule = mapOf(
    "Senin" to PiketSchedule("Senin", "jendela", listOf(16)),
    // Modify schedule here
)
```

## Libraries Used

- Jetpack Compose - UI toolkit
- Navigation Compose - Navigation between screens
- DataStore - Persistent preferences storage
- OkHttp - HTTP client for API calls
- Material3 - Material Design components

## License

This project is licensed under the MIT License.