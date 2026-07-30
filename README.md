# KidSpace Launcher

A child-friendly Android home launcher for use on a regular (adult) Android profile. Parents configure a curated, colorful surface with large graphic tiles; children see only what is allowed.

## Features

- **Child surface** — Large, colorful tiles with app icons, favicons, or permanent random icons
- **Parent mode** — Protected by an English number-word challenge (e.g. "three, seven, one" → enter `371`)
- **App management** — Browse all installed apps and add them to the child surface
- **Custom links** — Add websites and YouTube videos as tiles
- **Customization** — Preset backgrounds, color themes, reorder and remove tiles

## Requirements

See the [`requirements/`](requirements/) directory for product vision, features, and architecture.

## Install

Download the latest debug APK: [`releases/KidSpace-debug.apk`](releases/KidSpace-debug.apk)

Or build locally:

```bash
bash scripts/setup-android-sdk.sh
export ANDROID_HOME=$HOME/android-sdk
./gradlew assembleDebug
./gradlew installDebug   # with device connected
```

See [`commands.md`](commands.md) for the full command registry.

## Set as Default Launcher

After installing the APK, open KidSpace and choose **Always** when Android asks which launcher to use. Long-press the header on the child screen to access parent mode.

## Screenshots

Preview images are in [`docs/screenshots/`](docs/screenshots/).

## License

See [LICENSE](LICENSE).
