# Walkthrough - Use Snooze Icon

I have replaced the "+" text with a standard snooze icon for a better visual representation of the snooze action.

## Changes

### Build Configuration
- [MODIFY] [libs.versions.toml](file:///C:/git/BlueReminder/Watch/gradle/libs.versions.toml): Added `androidx.compose.material:material-icons-extended` dependency.
- [MODIFY] [build.gradle.kts](file:///C:/git/BlueReminder/Watch/app/build.gradle.kts): Included the `material-icons-extended` library.

### UI Layer
- [MODIFY] [MainActivity.kt](file:///C:/git/BlueReminder/Watch/app/src/main/java/com/sentes/bluereminder/presentation/MainActivity.kt):
    - Replaced `Text("+", ...)` with `Icon(Icons.Default.Snooze, ...)` in the `ReminderItem`.
    - Added necessary imports for Material Icons.

## Verification Results

### Automated Tests
- Ran `./gradlew :app:assembleDebug`: **PASSED**

## How to Test
1. Launch the app on your Wear OS device.
2. Observe the snooze button on the right side of each reminder.
3. It should now show a clock with a snooze (Zzz) icon instead of a simple plus sign.
