# Highlight Overdue Reminders

Update the UI to show reminders that are past their scheduled time with a red background (error container color).

## Proposed Changes

### UI Layer

#### [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt)
- Import `java.time.LocalDateTime`.
- In `ReminderItem`, check if `reminder.reminderTime` is before `LocalDateTime.now()`.
- Use `ButtonDefaults.buttonColors` with `errorContainer` and `onErrorContainer` when the reminder is overdue.

## Verification Plan

### Manual Verification
- Launch the app with some reminders scheduled in the past.
- Verify that overdue reminders appear with a red/error background.
- Verify that future reminders appear with the default background.
