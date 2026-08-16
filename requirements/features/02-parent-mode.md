# Feature: Parent Mode

## User Story

As a parent, I want to unlock a protected area so I can manage apps, links, and appearance without the child accessing system settings.

## Acceptance Criteria

- [ ] Child surface has a discreet entry point to parent gate (long-press on logo area)
- [ ] Parent gate shows a **random sequence of numbers as English words** (e.g. "three, seven, one")
- [ ] User enters digits on a numeric pinpad; correct entry grants parent access
- [ ] Challenge regenerates on each attempt
- [ ] Parent area shows **all installed launchable apps** and **pinned web shortcuts** from browsers (Chrome, Firefox, and other supported browsers) when KidSpace is the default launcher
- [ ] Browser “Add to home screen” / pin-shortcut requests are accepted via Android shortcut APIs while KidSpace is the default launcher
- [ ] Parent can add apps, websites, or YouTube links to the child surface
- [ ] Parent can search YouTube by keyword and add multiple videos from search results (see feature 06)
- [ ] Parent can **start** any installed app directly from the Apps list without adding it to the child surface
- [ ] Tapping an app already on the child surface in the Apps list **removes** it again (toggle add/remove)
- [ ] Parent can remove or reorder child tiles via drag-and-drop (drag handle on each tile)
- [ ] Parent can **edit** existing tiles (name, URL, browser mode, and site permissions for links; custom name for apps)
- [ ] Tile list shows current browser/permission settings and a **Tap to edit** hint for website and YouTube tiles
- [ ] New tiles (apps, links, videos) are added at the **beginning** of the child screen list
- [ ] Parent can customize background and colors (see feature 03)
- [ ] Parent can exit back to child surface

## Challenge Rationale

The English number-word challenge requires reading ability, acting as a lightweight age gate without storing a separate PIN.
