# Feature: Parent YouTube Search

## User Story

As a parent, I want to search YouTube by keyword and add multiple videos to the child screen quickly.

## Acceptance Criteria

- [ ] Parent area has a **Videos** tab with YouTube search
- [ ] Parent can enter a search term and see matching videos in a list
- [ ] Each result shows thumbnail, title, and duration
- [ ] Parent can select multiple videos and add them to the child screen in one action
- [ ] Videos already on the child screen are marked and cannot be selected again
- [ ] Search uses YouTube's internal web search endpoint (Innertube) — **no API key required**

## Technical Notes

- Endpoint: `POST https://www.youtube.com/youtubei/v1/search`
- Same internal API used by youtube.com
- Unofficial and may change without notice; parser walks nested `videoRenderer` objects
