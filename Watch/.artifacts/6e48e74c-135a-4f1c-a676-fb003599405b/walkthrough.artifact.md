# Walkthrough - Loading State for Individual Reminders

I have added a loading indicator that appears on a specific reminder when it is being updated (e.g., when snoozing or toggling completion).

## Changes

### Data Layer
- [MODIFY] [RemindersStateFlow.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/data/RemindersStateFlow.kt):
    - Added `beingLoadedReminderId` to track which reminder is currently waiting for an update from the phone.
    - Updated `updateSingleReminder` to automatically clear the loading state when the update is received.

### UI Logic Layer
- [MODIFY] [MainViewModel.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainViewModel.kt):
    - Set the `beingLoadedReminderId` before sending snooze or toggle requests to the phone.
    - Added error handling to clear the loading state if the request fails.

### UI Layer
- [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt):
    - Updated `ReminderItem` to show a `CircularProgressIndicator` inside the button if it is currently "being changed".
    - Adjusted the layout of the reminder button to accommodate the progress indicator on the right side.

## Verification Results

### Automated Tests
- Ran `./gradlew :app:assembleDebug`: **PASSED**

## How to Test
1. Launch the app on your Wear OS device.
2. Tap on a reminder to complete it or use the snooze icon.
3. You should see a small loading spinner appear on the right side of the reminder button until the phone app responds with the updated state.
