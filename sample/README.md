# GuideKit Sample

The sample is a Compose Multiplatform app that demonstrates GuideKit against real UI targets.

## Features Shown

- Measuring target bounds from existing Compose UI.
- Step-specific titles, descriptions, highlighted description text, and button text.
- Global style defaults through `GuideKitStyle`.
- Per-step arrow overrides for dashed, solid, and double-ended arrows.
- Rounded and circular target highlight styles.
- Auto-scroll for targets lower in a scrollable page.
- Skip, step-change, and finish callbacks.

## Run Android

```bash
./gradlew :sample:composeApp:installDebug
```

## Build iOS Framework

```bash
./gradlew :sample:composeApp:linkDebugFrameworkIosSimulatorArm64
```

The Swift host files are in `sample/iosApp`.
