# Walkthrough - Highlight Overdue Reminders

I have updated the Wear OS app to highlight reminders that are past their scheduled time with a red background.

## Changes

### UI Layer
- [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt):
    - Updated `ReminderItem` to check if a reminder is overdue by comparing `reminderTime` with `LocalDateTime.now()`.
    - Applied `MaterialTheme.colorScheme.errorContainer` as the background color for overdue items.

## Verification Results

### Automated Tests
- Ran `./gradlew :app:assembleDebug`: **PASSED**

## How to Test
1. Launch the app on a Wear OS device.
2. If there are reminders with a scheduled time earlier than the current time, they will appear with a red background.
3. Future reminders will continue to use the default theme colors.
