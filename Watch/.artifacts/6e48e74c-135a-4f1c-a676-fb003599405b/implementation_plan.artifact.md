# Enter Title for Quick Reminders

Enable users to enter a custom title for quick reminders by launching the Wear OS system input interface when the "Add (+1h)" button is clicked.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/git/BlueReminder/Watch/gradle/libs.versions.toml)
- Add `wearInput = "1.2.0"` version.
- Add `wear-input = { group = "androidx.wear", name = "wear-input", version.ref = "wearInput" }` library.

#### [MODIFY] [build.gradle.kts](file:///C:/git/BlueReminder/Watch/app/build.gradle.kts)
- Include `libs.wear.input` dependency.

### UI Logic Layer

#### [MODIFY] [MainViewModel.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainViewModel.kt)
- Update `addQuickReminder(title: String)` to accept the title as a parameter instead of using a hardcoded string.

### UI Layer

#### [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt)
- Implement `rememberLauncherForActivityResult` to handle the text input result.
- Create an intent using `RemoteInputIntentHelper` to launch the system input dialog.
- Trigger the input dialog when the "Add (+1h)" button is clicked.
- Pass the entered title to `viewModel.addQuickReminder`.

#### [MODIFY] [strings.xml](file:///C:/git/BlueReminder/Watch/app/src/main/res/values/strings.xml)
- Add `enter_reminder_title` string for the input dialog label.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify the build.

### Manual Verification
- Deploy to a Wear OS device.
- Click the "Dodaj (+1h)" button.
- Verify that the system input interface (keyboard/voice/emoji) appears.
- Enter a title and confirm.
- Verify that the reminder is created on the phone with the custom title.
