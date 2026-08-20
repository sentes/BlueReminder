# Implement Snooze Selection Dialog

Replace the fixed "+1h" snooze button with a selection dialog allowing the user to choose between +1h, +2h, or +1d.

## Proposed Changes

### Service Layer

#### [MODIFY] [PhoneService.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/service/PhoneService.kt)
- Update `snoozeReminder` to accept `durationHours: Int`.
- Send a JSON payload: `{"id": "...", "durationHours": ...}` to the `/reminder/snooze` path.

### ViewModel Layer

#### [MODIFY] [MainViewModel.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainViewModel.kt)
- Update `snoozeReminder` to accept `durationHours: Int` and pass it to the service.

### UI Layer

#### [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt)
- Add state to track the active snooze target: `var snoozeTarget by remember { mutableStateOf<Reminder?>(null) }`.
- When the snooze button is clicked, set `snoozeTarget = reminder`.
- Implement a `SnoozeDialog` (using `AlertDialog` or a custom `Dialog`) that appears when `snoozeTarget != null`.
- The dialog will contain three options:
    - **+1 godzina**
    - **+2 godziny**
    - **+1 dzień**
- Clicking an option will trigger the snooze logic and clear the target.

## Verification Plan

### Manual Verification
- Deploy to a Wear OS device.
- Click the snooze button on a reminder.
- Verify that a selection dialog appears.
- Click each option and verify (via logs or phone behavior) that the correct duration is sent to the phone.
- Verify the dialog closes after selection or on swipe-to-dismiss.
