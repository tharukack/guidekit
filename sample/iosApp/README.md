# GuideKit iOS Sample Host

This folder contains the SwiftUI host files for the Compose Multiplatform sample.

The Compose UI entry point is exported from `sample/composeApp` as the `GuideKitSample` framework through `MainViewController()`.

To run it in Xcode, create an iOS app target named `iosApp`, add these Swift files, and add the Gradle-generated `GuideKitSample` framework build phase from `sample/composeApp`.
