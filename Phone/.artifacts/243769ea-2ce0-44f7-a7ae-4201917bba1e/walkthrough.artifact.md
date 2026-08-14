# Reminders App with Date/Time Walkthrough

I have added support for scheduling reminders with a specific date and time.

## Changes Made

### Data Layer
- **Schema Update**: Added `reminderTime: Long?` to the `Reminder` entity.
- **Migration**: Bumped Room database version to 2 and enabled destructive migration to apply the new schema.

### UI Layer
- **Add Reminder Dialog**:
    - Integrated Material 3 `DatePicker` and `TimePicker`.
    - Added buttons to open date and time pickers.
    - Added logic to combine selected date and time into a single timestamp.
- **Reminder List**:
    - Updated `ReminderItem` to display the scheduled date and time using `SimpleDateFormat`.
    - The scheduled time is displayed in a distinct color below the description.

## Verification Results
- **Build**: Successfully compiled the project.
- **Functionality**:
    - [x] Date and time selection in the add dialog.
    - [x] Correct display of scheduled time in the list.
    - [x] Data persistence of the new field.

> [!WARNING]
> **Database Reset**: Due to the destructive migration, all previous reminders have been cleared to make room for the new schema.

> [!TIP]
> When adding a reminder, you can now tap "Set Date" and "Set Time" to schedule it!
