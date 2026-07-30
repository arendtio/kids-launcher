# Feature: Parent YouTube Search

## User Story

As a parent, I want to search YouTube by keyword and add multiple videos to the child screen quickly.

## Acceptance Criteria

- [ ] Parent area has a **Videos** tab with YouTube search
- [ ] Parent can enter a search term and see matching videos in a list
- [ ] Each result shows thumbnail, title, and duration
- [ ] Parent can select multiple videos and add them to the child screen in one action
- [ ] Videos already on the child screen are marked and cannot be selected again
- [ ] Parent can save a YouTube Data API v3 key in the app (or provide it via `youtube.api.key` in `local.properties` for development)

## API Setup

1. Create a Google Cloud project
2. Enable **YouTube Data API v3**
3. Create an API key
4. Save the key in Parent mode → Videos, or add `youtube.api.key=YOUR_KEY` to `local.properties`
