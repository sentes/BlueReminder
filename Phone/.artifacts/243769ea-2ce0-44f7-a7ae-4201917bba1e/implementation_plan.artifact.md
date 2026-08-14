# Add Date and Time to Reminders Implementation Plan

Enable users to set a specific date and time for their reminders.

## User Review Required

> [!IMPORTANT]
> This change involves modifying the database schema. I will enable destructive migration for now to simplify the update, which means **existing reminders will be deleted** when the app is updated.

## Proposed Changes

### Data Layer
- [MODIFY] [Reminder.kt](file:///C:/git/BlueReminder/Phone/app/src/main/java/com/sentes/bluereminder/data/Reminder.kt): Add `reminderTime: Long?` field.
- [MODIFY] [ReminderDatabase.kt](file:///C:/git/BlueReminder/Phone/app/src/main/java/com/sentes/bluereminder/data/ReminderDatabase.kt): Bump version to 2 and enable destructive migration.

### UI Layer
- [MODIFY] [ReminderViewModel.kt](file:///C:/git/BlueReminder/Phone/app/src/main/java/com/sentes/bluereminder/ui/ReminderViewModel.kt): Update `addReminder` to accept `reminderTime`.
- [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Phone/app/src/main/java/com/sentes/bluereminder/MainActivity.kt):
    - Update `AddReminderDialog` to include date and time pickers.
    - Update `ReminderItem` to display the scheduled date and time if available.

## Verification Plan

### Manual Verification
- Deploy the updated app.
- Create a reminder with a specific date and time.
- Verify the date and time are displayed correctly in the list.
- Create a reminder without a time and verify it still works.
