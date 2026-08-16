# Cross Out Reminders Instead of Removing

Modify the dismissal logic so that clicking a reminder marks it as completed (crossed out) instead of removing it from the list.

## Proposed Changes

### Data Layer

#### [MODIFY] [Reminder.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/data/Reminder.kt)
- Add `val isCompleted: Boolean = false` to the `Reminder` data class.

#### [MODIFY] [RemindersStateFlow.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/data/RemindersStateFlow.kt)
- Replace `removeReminder` with `markAsCompleted(reminderId: String)`.
- This method will find the reminder and update its `isCompleted` status to `true`.

### UI Logic Layer

#### [MODIFY] [MainViewModel.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainViewModel.kt)
- Update `dismissReminder` to call `RemindersStateFlow.markAsCompleted` instead of `removeReminder`.

### UI Layer

#### [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt)
- Update `ReminderItem` to:
    - Apply `TextDecoration.LineThrough` to the title and time text if `isCompleted` is true.
    - Reduce opacity or change colors for completed reminders to make them look "dimmed".
    - Disable the snooze button for completed reminders.

## Verification Plan

### Manual Verification
- Deploy to a Wear OS device.
- Click on a reminder.
- Verify it remains in the list but appears crossed out and dimmed.
- Verify the snooze button becomes disabled/hidden for that reminder.
