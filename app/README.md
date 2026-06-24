# Location Tracking App

A simple Android application that tracks the device's location in the background at a user-defined interval, stores the readings in a local Room database, and lets the user view or delete the saved history.

## Features

- **Start / Stop Tracking** — runs a foreground service that fetches the device's current location periodically.
- **Configurable Interval** — set how often (in minutes) the location should be fetched, via the Settings screen.
- **View Stored Data** — see all saved location records (latitude, longitude, timestamp) in a list.
- **Delete All Data** — clear the entire location history from the database.
- **Persistent Notification** — while tracking is active, a foreground notification keeps the service alive and visible to the user.

## Project Structure

app/src/main/java/com/example/locationtrackingappp/

├── MainActivity.java              # Home screen: start/stop tracking, navigation

├── SettingsActivity.java          # Set the location fetch interval

├── StoredDataActivity.java        # View / refresh / delete saved locations

├── LocationAdapter.java           # RecyclerView adapter for the location list

├── LocationForegroundService.java # Foreground service that fetches & saves locations

├── AppDatabase.java               # Room database singleton

├── LocationDao.java               # Room DAO (insert / query / delete)

└── LocationEntity.java            # Room entity (id, latitude, longitude, timestamp)
app/src/main/res/

├── layout/activity_main.xml          # Home screen UI

├── layout/activity_settings.xml      # Settings screen UI

├── layout/activity_stored_data.xml   # Stored data list screen UI

└── drawable/ic_arrow_back.xml        # Back arrow icon
AndroidManifest.xml                   # Permissions, activities, service declaration

## Requirements

- Android Studio (latest stable version recommended)
- Android SDK with a minimum target supporting `ACCESS_BACKGROUND_LOCATION` (Android 10 / API 29+ recommended for full background behavior)
- A physical device or emulator with Google Play services (required for `FusedLocationProviderClient`)
- Dependencies used by the project (must be present in `build.gradle`):
    - `androidx.room:room-runtime` and `room-compiler` (annotationProcessor)
    - `com.google.android.gms:play-services-location`
    - `androidx.recyclerview:recyclerview`
    - `androidx.cardview:cardview`
    - `androidx.appcompat:appcompat`

## How to Run

1. **Open the project** in Android Studio (`File > Open` and select the project root).
2. **Let Gradle sync** complete. If any of the dependencies above are missing from `app/build.gradle`, add them and sync again.
3. **Connect a device or start an emulator** that includes Google Play services.
4. **Run the app** (`Run > Run 'app'` or the green ▶ button).
5. On first launch, tap **Start Tracking** — you will be prompted for:
    - Location permission (Fine Location)
    - Background Location permission (on Android 10+, this is a separate prompt/screen)
    - Notification permission (on Android 13+)
6. Once permissions are granted, tracking starts automatically and a persistent notification appears.

## How to Test

### Manual testing flow

1. **Start Tracking**
    - Tap **Start Tracking** on the home screen.
    - Grant the requested permissions.
    - Confirm the "Tracking Started" toast appears and the foreground notification ("Location Tracking Active") is visible in the status bar.

2. **Verify location capture**
    - Wait for the configured interval (default: 5 minutes) or lower the interval first via Settings to test faster.
    - A toast showing the captured latitude/longitude should appear each time a location is fetched.

3. **Change the interval**
    - Tap **Settings**.
    - Enter a new interval (e.g. `1` minute) and tap **Save Interval**.
    - Restart tracking (stop, then start again) so the service picks up the new value.

4. **View stored data**
    - Tap **View Stored Data**.
    - Confirm the list shows entries with latitude, longitude, and timestamp, most recent first.
    - Tap **Refresh Data** to reload from the database.

5. **Delete all data**
    - Tap **Delete All Data**.
    - Confirm the list clears and a "All data deleted" toast appears.

6. **Stop Tracking**
    - Return to the home screen and tap **Stop Tracking**.
    - Confirm the notification disappears and the "Tracking Stopped" toast appears.

### Testing on an emulator

If testing on an emulator, use Android Studio's **Extended Controls > Location** panel to set or simulate a custom GPS position so `FusedLocationProviderClient` has a location to return.

### Testing on a physical device (recommended)

This app is best tested on a **real Android phone**, since it relies on actual GPS hardware and real background-execution behavior that emulators only approximate.

1. **Enable Developer Options & USB Debugging** on the phone (`Settings > About phone > tap Build number 7 times`, then `Settings > Developer options > USB debugging`).
2. **Connect the phone via USB** (or set up Wi-Fi debugging) and select it as the run target in Android Studio.
3. **Grant location permission carefully**: on Android 10+, the first permission dialog only offers "Allow only while using the app" / "Deny." To get true background tracking, you may need to go to **Settings > Apps > Location Tracker > Permissions > Location** and manually select **"Allow all the time."**
4. **Disable battery optimization for the app**: go to **Settings > Apps > Location Tracker > Battery**, and set it to **Unrestricted** (or "Don't optimize"). Otherwise Android may pause the foreground service or delay location fetches after the screen turns off.
5. **Walk around or drive** with the app running to see different coordinates logged over time — this is the most reliable way to confirm the service is actually fetching fresh GPS data rather than a cached/mock location.
6. Lock the screen and leave the app running in the background for a while to confirm the persistent notification stays active and location entries keep appearing in **View Stored Data** afterward.

## Known Issues / Notes

- **List item layout mismatch**: There is a custom card-style layout (with `latitudeTextView`, `longitudeTextView`, `timestampTextView`) defined in resources, but `LocationAdapter` currently inflates the built-in `android.R.layout.simple_list_item_2` instead. The custom layout is unused unless the adapter is updated to inflate it.
- **Database migrations**: `AppDatabase` uses `fallbackToDestructiveMigration()`, meaning any future schema version change will **wipe existing stored location data** rather than migrate it. Fine for development/testing, but should be replaced with a proper `Migration` before any production release.
- **Background location accuracy**: actual fetch interval may vary depending on Android's battery optimization and Doze mode restrictions, especially on Android 12+.