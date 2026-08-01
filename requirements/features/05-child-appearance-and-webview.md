# Feature: Child Appearance Tile

## User Story

As a child, I want to change my home screen colors and background myself without entering parent mode.

## Acceptance Criteria

- [ ] A permanent **My Look** tile is always visible on the child home grid
- [ ] Tapping it opens a child-friendly appearance picker
- [ ] Child can choose illustrated backgrounds and color themes
- [ ] Changes are saved to the same appearance settings used by the parent Look tab

# Feature: In-App Web Browser

## User Story

As a parent, I want website links to open in a simple built-in browser without browser chrome, with permissions I control in advance.

## Acceptance Criteria

- [ ] When adding a website/YouTube link, parent can choose integrated browser vs external browser
- [ ] Integrated browser shows only the website — no address bar, tabs, or browser UI
- [ ] Navigation stays on the same domain (including subdomains)
- [ ] Parent configures auto-grant/deny for camera, microphone, and location per link
- [ ] Permission policies apply to all pages on that domain during the session
- [ ] WebRTC getUserMedia requests also trigger Android runtime microphone/camera permissions when parent policy allows
- [ ] Uses the system WebView (no extra browser engine bundled)
- [ ] YouTube links opened in-app use a dedicated embedded player instead of the full YouTube website
