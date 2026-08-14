# Allow Editing of Reminders Implementation Plan

Add functionality to edit existing reminders, including their title, description, and scheduled date/time.

## Proposed Changes

### UI Layer
- [MODIFY] [ReminderViewModel.kt](file:///C:/git/BlueReminder/Phone/app/src/main/java/com/sentes/bluereminder/ui/ReminderViewModel.kt): Add `updateReminder` function to handle full updates.
- [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Phone/app/src/main/java/com/sentes/bluereminder/MainActivity.kt):
    - Rename `AddReminderDialog` to `ReminderDialog` and update it to support pre-filling from an existing `Reminder`.
    - Add an "Edit" icon/button to `ReminderItem`.
    - Track the reminder currently being edited in `ReminderApp`.

## Verification Plan

### Manual Verification
- Deploy the updated app.
- Create a new reminder.
- Tap the "Edit" button on the reminder.
- Change the title, description, and time.
- Verify the changes are reflected in the list and persisted after app restart.
- Mark as completed after editing to ensure all actions still work.
