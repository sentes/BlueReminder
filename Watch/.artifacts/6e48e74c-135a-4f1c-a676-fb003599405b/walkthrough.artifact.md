# Walkthrough - Add Snooze Button to Reminders

I have added a snooze button to each reminder in the Wear OS app, allowing users to increase the reminder time by one hour directly from their watch.

## Changes

### Service Layer
- [MODIFY] [PhoneService.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/service/PhoneService.kt): Added `snoozeReminder(reminderId: String)` which sends a message to the phone app on the `/reminder/snooze` path.

### ViewModel Layer
- [MODIFY] [MainViewModel.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainViewModel.kt): Added `snoozeReminder(reminder: Reminder)` to bridge the UI action to the `PhoneService`.

### UI Layer
- [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt):
    - Updated `ReminderItem` to include a "+1h" button next to the reminder details.
    - Used a `Row` layout to accommodate the snooze button.
    - Styled the snooze button with `tertiaryContainer` color for clear distinction.

## Verification Results

### Automated Tests
- Ran `./gradlew :app:assembleDebug`: **PASSED**

## How to Test
1. Launch the app on your Wear OS device.
2. Each reminder in the list now has a "+1h" button on the right.
3. Tapping the "+1h" button will send a snooze request for that specific reminder to your phone.
