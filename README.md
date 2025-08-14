<img width="1280" height="960" alt="image" src="https://github.com/user-attachments/assets/1d3421f0-b912-4d6d-b3b6-9214c5ca0bac" />

# B-Barq ⚡

B-Barq is a simple Android app that monitors the latest power outage schedules from the official site and reminds you on time — so your phone never runs out of charge and your work stays safe.

## How It Works

1. **Provide your Bill ID (شناسه قبض)** to the app.
2. **Grant the required permissions.**
3. **Start the foreground service** to begin monitoring.

The service checks the outage source every hour, displays the latest schedules in a persistent notification, and alerts you **30 minutes before each outage**.

## Features

* Hourly monitoring of official outage schedules.
* Persistent notification showing the latest info.
* Reminders 30 minutes before each scheduled outage.

## Requirements

* Android 6.0+
* Internet access

## Installation

1. Download the latest APK from [Releases](https://github.com/alijafari-gd/B-Barq/releases).
2. Install it on your device.
3. Enter your Bill ID, grant permissions, and start the service.

## Privacy

B-Barq only uses your Bill ID to fetch outage schedules and **does not collect or share any personal data**.

## Contribution

Contributions are welcome! If you have ideas, improvements, or bug fixes, feel free to:

* Fork the repository
* Create a new branch
* Submit a pull request

## TODO (Upcoming Features)

* Automatic Bill ID detection based on the user's location
* Rewriting the app withJetpack Compose and clean architecture&#x20;

## License

MIT License
