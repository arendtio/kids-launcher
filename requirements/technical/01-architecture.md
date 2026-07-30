# Technical Architecture

## Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Persistence:** Room (tiles), DataStore (appearance settings)
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34

## Launcher Integration

- `MainActivity` registers as `HOME` launcher via intent filter
- On first run, user must set KidSpace as default launcher (system prompt)

## Modules (single app module)

```
app/
  data/       Room entities, DAOs, repositories
  domain/     Parent gate logic, launch intents
  ui/         Compose screens (child, parent, theme)
  util/       Favicon fetch, icon assignment
```

## Data Model

### ChildTile

| Field       | Type   | Notes                          |
|-------------|--------|--------------------------------|
| id          | Long   | Primary key                    |
| type        | Enum   | APP, WEBSITE, YOUTUBE          |
| label       | String | Display name                   |
| target      | String | Package name or URL            |
| iconKey     | String | Favicon URL, asset key, or random seed |
| sortOrder   | Int    | Grid position                  |

### AppearanceSettings

| Field           | Type   |
|-----------------|--------|
| backgroundType  | PRESET / CUSTOM |
| backgroundPreset| String? |
| customBackgroundUri | String? |
| primaryColor    | Long (ARGB) |
| secondaryColor  | Long |
| accentColor     | Long |

## Security Notes

- Parent gate is obfuscation, not cryptographic security
- Launcher does not prevent leaving via system gestures on all devices; v1 focuses on curated home experience
