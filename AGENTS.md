# AGENTS.md

KidSpace Launcher — a single-module Android home launcher (Kotlin, Jetpack Compose, Room, Gradle).
See `README.md`, `commands.md`, and `requirements/` for product/build details.

## Cursor Cloud specific instructions

Environment: JDK is preinstalled; the Android SDK lives at `$HOME/android-sdk`. The startup update
script installs the SDK only if missing and regenerates `local.properties` (gitignored, so it must be
recreated each run for Gradle to find the SDK). `ANDROID_HOME`/PATH are exported from the agent's
`~/.bashrc`, so a fresh login shell can run `./gradlew`, `adb`, and `emulator` directly.

- Build / test / lint / install / run: use the commands in `commands.md` (e.g. `./gradlew assembleDebug`,
  `./gradlew test`, `./gradlew lintDebug`, `./gradlew installDebug`). Do not duplicate them here.
- `./gradlew lintDebug` currently fails on a pre-existing `QueryAllPackagesPermission` error in
  `app/src/main/AndroidManifest.xml` (the app intentionally lists all installed apps). Lint itself works;
  this is a code-level finding, not an environment problem.
- Unit tests (`./gradlew test`) need no device/emulator. Instrumented tests
  (`./gradlew connectedDebugAndroidTest`) and actually running the app require an emulator/device.

### Emulator caveat (important)

The cloud VM has no KVM (`/dev/kvm` absent), so the x86_64 emulator only runs via slow software
(TCG) emulation. It DOES boot and the KidSpace app installs, launches, and renders correctly, but
`system_server`/`SystemUI` continuously trip the ANR watchdog, so recurring "Process system isn't
responding" dialogs steal input and make multi-step UI automation unreliable. Notes:

- Prefer the AOSP image `system-images;android-34;default;x86_64` over `google_apis` — much lower idle
  load and far fewer ANRs. Boot takes several minutes; wait on `getprop sys.boot_completed == 1`.
- Launch headless: `emulator -avd <name> -no-window -no-audio -no-boot-anim -no-snapshot -accel off -gpu swiftshader_indirect`.
- Capture screenshots with `adb exec-out screencap -p > out.png` (the emulator is `-no-window`, so
  there is no desktop window to screen-record).
- To exercise the child surface without fighting the ANR/parent-gate flow, seed the Room DB directly
  (debug build): `adb shell run-as com.kidspace.launcher sqlite3 databases/kidspace.db < seed.sql`,
  then relaunch `MainActivity`. Table `child_tiles(type,label,target,iconKey,sortOrder)`; `type` is
  `APP|WEBSITE|YOUTUBE`; use `iconKey` like `random:star` for offline icons.
