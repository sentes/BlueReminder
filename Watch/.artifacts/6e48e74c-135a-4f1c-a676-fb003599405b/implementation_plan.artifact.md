# Add Snooze Button to Reminders

Add a button to each reminder item that allows the user to increase the reminder time by one hour (snooze).

## Proposed Changes

### Service Layer

#### [MODIFY] [PhoneService.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/service/PhoneService.kt)
- Add a new method `snoozeReminder(reminderId: String)` that sends a message to the phone with the path `/reminder/snooze`.

### ViewModel Layer

#### [MODIFY] [MainViewModel.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainViewModel.kt)
- Add a new method `snoozeReminder(reminder: Reminder)` that calls `phoneService.snoozeReminder`.

### UI Layer

#### [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt)
- Update `ReminderItem` to include a snooze button.
- I will use a `Row` layout inside the item or adjust the existing `Button` to accommodate the snooze action. To keep it consistent with Wear OS Material 3, I'll use a `FilledTonalButton` for the main content and a small `Button` for the "+1h" snooze action.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project builds successfully.

### Manual Verification
- Deploy to a Wear OS device.
- Verify that clicking the snooze button sends a message (can check logs or verify the phone app handles it).
