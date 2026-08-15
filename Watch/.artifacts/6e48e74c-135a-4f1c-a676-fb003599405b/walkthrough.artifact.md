# Walkthrough - Show Loading Indicator and Auto-Refresh on Resume

I have added a loading indicator to the Wear OS app and configured it to automatically refresh reminders whenever the app is resumed.

## Changes

### Data Layer
- [MODIFY] [RemindersRepository.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/data/RemindersRepository.kt): Added logic to automatically sort reminders by time whenever the list is updated.

### UI Logic Layer
- [MODIFY] [MainViewModel.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainViewModel.kt):
    - Added `isLoading` `StateFlow`.
    - Updated `refreshReminders` to set `isLoading` to `true` during the operation and `false` once finished.

### UI Layer
- [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt):
    - Replaced `LaunchedEffect(Unit)` with `LifecycleEventEffect(Lifecycle.Event.ON_RESUME)` to ensure reminders are refreshed every time the user returns to the app.
    - Wrapped `TransformingLazyColumn` in a `Box` and added a `CircularProgressIndicator` centered on the screen when `isLoading` is true.
    - Disabled the "Refresh" button while loading to prevent multiple simultaneous requests.

## Verification Results

### Automated Tests
- Ran `./gradlew :app:assembleDebug`: **PASSED**
