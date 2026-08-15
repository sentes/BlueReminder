# Convert Reminder Time to DateTime

Update the `Reminder` data class to use `LocalDateTime` for the reminder time, ensuring better type safety and enabling easier formatting in the UI.

## Proposed Changes

### Data Layer

#### [MODIFY] [Reminder.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/data/Reminder.kt)
- Change `reminderTime: String` to `reminderTime: java.time.LocalDateTime`.

### Service Layer

#### [MODIFY] [WatchListenerService.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/service/WatchListenerService.kt)
- Update JSON parsing to convert the incoming time string to `LocalDateTime` using `LocalDateTime.parse()`.

### UI Layer

#### [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt)
- Update `ReminderItem` to format the `LocalDateTime` into a human-readable string (e.g., "HH:mm").

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project compiles with the new types.
