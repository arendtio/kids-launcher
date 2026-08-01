# UX Analysis: Child Home Grid (v1.2.1)

## Problem (before)

The child home grid felt **narrow, busy, and visually restless**:

| Issue | Cause |
|-------|--------|
| Cramped layout | 4 columns on portrait phones left little space per tile |
| Visual noise | App icons, favicons, and YouTube thumbnails filled tiles edge-to-edge with different shapes and colors |
| Inconsistent rhythm | Tight 10dp gaps and 6dp inner padding made labels compete with icons |
| No shared frame | Each icon type used the full tile area differently (crop vs. letterbox) |

## Design goals

1. **Calm** — reduce competing visual elements
2. **Scannable** — child can find favorites quickly
3. **Large tap targets** — still suitable for small hands
4. **Consistent** — same tile structure regardless of icon source

## Changes in 1.2.1

### Grid density
- **Portrait:** 3 columns (was 4) — larger tiles, more breathing room
- **Landscape:** 5 columns (was 6)
- **Spacing:** 16dp between tiles (was 10dp), 24dp screen padding (was 20dp)

### Uniform icon well
Every tile icon sits inside a soft neutral well (`#F2F5F9`):
- Icons scale with `ContentScale.Fit` inside 78% of the well
- App icons no longer bleed to tile edges
- Different icon sources feel part of the same system

### Tile card
- Larger corner radius (20dp)
- More inner padding (10–12dp)
- Slightly lower elevation (4dp) for a softer look
- Label at 12sp with clearer separation from icon area

## Preview screenshots

Generated without emulator/KVM:

```bash
python3 scripts/generate-preview-screenshots.py
```

Output: `docs/screenshots/01-child-home.png` (and other screens)

## Further ideas (not implemented)

- Optional “quiet mode” using monochrome app icon treatment
- Parent toggle for 3 vs. 4 column portrait grid
- Staggered animation on first load
