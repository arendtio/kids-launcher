# Feature: Child Surface

## User Story

As a child using the device, I want a colorful home screen with big picture buttons so I can open my allowed apps and links easily.

## Acceptance Criteria

- [ ] Child surface shows only tiles configured by a parent
- [ ] Each tile has a **graphic element** (app icon, website favicon, or permanent random icon)
- [ ] Tiles have large tap targets suitable for small hands
- [ ] Tapping an **app** tile launches that application
- [ ] Tapping a **website** tile opens the URL in the default browser
- [ ] Tapping a **YouTube** tile opens the video in a focused embedded player (in-app) or external browser (parent choice)
- [ ] In-app YouTube playback uses a full-screen embed player that loops the same video instead of auto-advancing
- [ ] YouTube tiles show the **video thumbnail** as their tile icon
- [ ] Background image and theme colors are applied to the child surface
- [ ] Empty state is friendly when no tiles are configured
- [ ] Child surface adapts to **landscape orientation** (wider grid, compact header) as well as portrait
- [ ] In-app browser and launcher activities support rotation (no forced portrait lock)

## Tile Types

| Type    | Target              | Icon source                    |
|---------|---------------------|--------------------------------|
| App     | Package name        | Application icon               |
| Website | HTTPS URL           | Favicon, else random permanent |
| YouTube | YouTube video URL   | Video thumbnail (`img.youtube.com`) |
