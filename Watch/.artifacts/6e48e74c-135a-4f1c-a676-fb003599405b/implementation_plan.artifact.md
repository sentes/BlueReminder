# Show Loading Indicator for Reminders

Add a loading indicator to the Wear OS app to provide feedback to the user while reminders are being requested from the phone.

## Proposed Changes

### UI Logic Layer

#### [MODIFY] [MainViewModel.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainViewModel.kt)
- Add an `isLoading` `StateFlow` to track the status of the reminder request.
- Update `refreshReminders()` to toggle `isLoading`.

### UI Layer

#### [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt)
- Observe the `isLoading` state from the `MainViewModel`.
- Display a `CircularProgressIndicator` when `isLoading` is true.
- If not loading and the list is empty, continue showing the "No reminders" message.

## Verification Plan

### Manual Verification
- Launch the app and verify the loading indicator appears initially while it fetches reminders.
- Press the "Refresh" button and verify the loading indicator appears again.
- Ensure the indicator disappears once the request to the phone is completed.
