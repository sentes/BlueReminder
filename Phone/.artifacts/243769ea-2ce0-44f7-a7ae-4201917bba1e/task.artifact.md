# Add Date and Time to Reminders Task List

- [x] Update Data Layer
    - [x] `Reminder.kt`: Add `reminderTime` field
    - [x] `ReminderDatabase.kt`: Bump version and enable destructive migration
- [x] Update UI Layer
    - [x] `ReminderViewModel.kt`: Update `addReminder` signature
    - [x] `MainActivity.kt`: Implement Date/Time picking in `AddReminderDialog`
    - [x] `MainActivity.kt`: Display `reminderTime` in `ReminderItem`
- [x] Verification
    - [x] Build and deploy
    - [x] Verify date/time display and persistence
