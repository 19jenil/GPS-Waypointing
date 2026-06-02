# GPS Waypointing

A modern Android navigation application built with Kotlin and Jetpack Compose that allows users to create, store, and navigate to GPS waypoints using real-time location tracking and compass-based navigation.

## Overview

GPS Waypointing is designed for outdoor navigation scenarios such as hiking, campus navigation, field exercises, and location tracking. The application combines GPS services, device sensors, and persistent local storage to provide an intuitive waypoint navigation experience.

The app enables users to save multiple waypoints, view nearby locations, calculate distances and bearings, and navigate using a real-time compass interface.

---

## Features

### Real-Time GPS Tracking

* Live location updates using Android Location Services
* Accurate latitude and longitude tracking
* Dynamic location refresh

### Waypoint Management

* Create waypoints from current GPS location
* Store multiple waypoints
* Select and navigate to saved waypoints
* Clear all waypoints when required

### Compass-Based Navigation

* Real-time compass using device sensors
* Dynamic direction updates
* Bearing calculations toward selected waypoints
* Interactive navigation display

### Distance Calculation

* Real-time distance measurement
* Automatic updates as the user moves
* Relative waypoint positioning

### Persistent Data Storage

* Internal file storage implementation
* Waypoints remain available after restarting the application
* Automatic save and load functionality

### Interactive User Interface

* Built with Jetpack Compose
* Responsive and modern Material Design interface
* Canvas-based compass visualization
* Touch gesture support

---



---

## Technology Stack

| Technology        | Purpose                 |
| ----------------- | ----------------------- |
| Kotlin            | Application Development |
| Jetpack Compose   | User Interface          |
| Android Studio    | Development Environment |
| Location Services | GPS Tracking            |
| Sensor Manager    | Compass Navigation      |
| Material Design 3 | UI Components           |
| Internal Storage  | Data Persistence        |

---

## Project Architecture

GPS-Waypointing

├── MainActivity.kt

├── SharedComposables.kt

├── Waypoint.kt

├── AndroidManifest.xml

└── Resources

### MainActivity.kt

Handles:

* GPS location updates
* Compass sensor management
* Waypoint creation
* Distance calculations
* File persistence

### SharedComposables.kt

Contains:

* GPSApplication UI
* CompassCard
* CompassCanvas
* ControlsRow
* WaypointsCard

### Waypoint.kt

Defines the waypoint data model including:

* ID
* Latitude
* Longitude
* Display Name

---

## Installation

### Clone Repository

```bash
git clone https://github.com/19jenil/GPS-Waypointing.git
```

### Open Project

1. Open Android Studio
2. Select "Open Existing Project"
3. Choose the cloned repository

### Build and Run

1. Sync Gradle
2. Connect an Android device or emulator
3. Run the application

---

## Future Improvements

* Google Maps Integration
* Route Visualization
* Cloud Synchronization
* Waypoint Categories
* Export and Import Features
* Offline Navigation Support

---

## Learning Outcomes

This project demonstrates practical experience with:

* Android Application Development
* Kotlin Programming
* Jetpack Compose
* GPS and Location Services
* Sensor Management
* Mobile UI Design
* Data Persistence
* Real-Time Navigation Systems

---

## Author

**Jenil Patel**

Android Developer | Mobile Application Development | Kotlin Enthusiast

GitHub: https://github.com/19jenil
