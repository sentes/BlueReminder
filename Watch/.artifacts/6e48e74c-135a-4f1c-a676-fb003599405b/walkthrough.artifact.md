# Walkthrough - Enter Custom Title for Quick Reminders

I have implemented the ability to enter a custom title for quick reminders by launching the Wear OS system input interface.

## Changes

### Build Configuration
- [MODIFY] [libs.versions.toml](file:///C:/git/BlueReminder/Watch/gradle/libs.versions.toml): Added `androidx.wear:wear-input` dependency.
- [MODIFY] [build.gradle.kts](file:///C:/git/BlueReminder/Watch/app/build.gradle.kts): Included the `wear-input` library.

### UI Logic Layer
- [MODIFY] [MainViewModel.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainViewModel.kt): Updated `addQuickReminder` to accept a custom `title`.

### UI Layer
- [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt):
    - Integrated `RemoteInputIntentHelper` and `rememberLauncherForActivityResult` to handle system text input.
    - Updated the "Dodaj (+1h)" button to launch the system input dialog before creating the reminder.
- [MODIFY] [strings.xml](file:///C:/git/BlueReminder/Watch/app/src/main/res/values/strings.xml): Added `enter_reminder_title` for the input dialog prompt.

## Verification Results

### Automated Tests
- Ran `./gradlew :app:assembleDebug`: **PASSED**

## How to Test
1. Launch the app on your Wear OS device.
2. Click the "Dodaj (+1h)" button.
3. The system input interface will appear. You can type, use voice, or choose an emoji for the reminder title.
4. After confirming the input, the reminder will be sent to your phone with the title you provided.
