# Changelog

All notable changes to GuideKit will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project follows semantic versioning.

## [1.0.0] - 2026-07-22

### Changed

- Expanded the README with the same installation, full-screen parent setup, target-bounds callback, persistence, customization, and API guidance used by the website.
- Replaced public arrow dash interval arrays with named presets directly in `GuideKitArrowLineStyle`: `Dotted`, `ShortDash`, `MediumDash`, `LongDash`, and `DashDot`.
- Renamed the generic `Dashed` line style to `SpacedDash` to describe its visible spacing and distinguish it from the length-based dash presets.
- Replaced public floating-point pixel sizes with integer styling values that GuideKit converts internally.
- Replaced the separate single-phrase `descriptionHighlight` option with the unified `descriptionHighlights` list.
- Changed `arrowHeadAngleDegrees` from a public `Float` to an `Int`, with conversion handled internally.

## [0.1.0] - 2026-07-16

### Added

- Initial Compose Multiplatform GuideKit library.
- Coachmark overlay with target highlights, instruction cards, arrows, step indicators, skip/finish callbacks, and auto-scroll support.
- Rounded rectangle and circular target highlight styles.
- Per-step style overrides for arrows, target highlights, instruction cards, and auto-scroll behavior.
- Sample Compose Multiplatform app under `sample/`.
