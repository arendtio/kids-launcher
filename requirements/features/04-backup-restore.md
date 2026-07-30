# Feature: Backup & Restore

## User Story

As a parent, I want to export and import my KidSpace configuration so I can move settings between devices or keep a backup.

## Acceptance Criteria

- [ ] Parent area has a **Backup** tab
- [ ] Export writes a JSON file containing all child tiles and appearance settings
- [ ] Custom background photos are embedded in the JSON (Base64)
- [ ] Import reads a valid JSON backup and replaces current tiles and appearance
- [ ] Import shows a confirmation dialog before overwriting existing data
- [ ] Success and error feedback is shown after export/import

## Backup Format

- Versioned JSON (`version: 1`)
- Tiles: type, label, target, iconKey, sortOrder
- Appearance: background type/preset, colors, optional embedded custom image
