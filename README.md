# مدرب الكاليسثينكس (Calisthenics Coach)

Offline Android app (Kotlin, Jetpack Compose, Material 3). Arabic RTL UI, no internet, no accounts, no ads.
Progress is stored locally in `state.json` inside the app's private storage; backup/restore uses a local JSON file.

## Build
Push to GitHub. The workflow `Build Calisthenics Coach APK` (.github/workflows/build-apk.yml) builds the debug APK;
download it from the run's **Artifacts** section (`CalisthenicsCoach-debug-apk`). It can also be run manually (workflow_dispatch).

## Known limitations (documented, not hidden)
- Exercise illustrations are simple animated stick figures drawn in code (no photos/videos); the written steps are the authoritative guide.
- Pull-ups, chin-ups, dips, muscle-up, front/back lever and free handstand need equipment/supervision the user does not have; the app lists them as unavailable and gives only partial strengthening alternatives. Short loop bands do not replace a pull-up bar.
- Timers run while the app is open on the workout screen (screen is kept awake); they do not continue if the app is closed.
- The project has no `gradlew` wrapper script; the workflow installs Gradle 8.7 directly. Android Studio uses `gradle/wrapper/gradle-wrapper.properties`.
- The code had not been compiled in the authoring environment (no Android SDK); the first GitHub Actions run is the first real compile.
