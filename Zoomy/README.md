# Zoomy 🚗💨

Zoomy is a polished, fast-paced casual 3-lane car dodging game built natively for Android using **Kotlin** and **Jetpack Compose**. Featuring a custom deterministic 2D game engine, responsive physics, rich procedural vector visuals, and comprehensive mobile monetization & audio integrations.

---

## ✨ Features

- **Core Gameplay**:
  - Smooth 3-lane top-down dodging mechanics with swipe and tap controls.
  - Custom deterministic 2D canvas game engine with fixed timestep physics (`1/120s`).
  - Dynamic procedural obstacle spawning (cars, trucks, road cones) and collectible coin lines.
  - Speed scaling from 260 dp/s up to 620 dp/s with high-speed visual streak effects.
  - Close-call near-miss detection, combo multipliers (up to 5x), and floating combo callouts.
  - Active boosters: **Shield** (crash protection), **Magnet** (attract coins), and **2x Multiplier**.
  - Animated crash sequence with camera shake, spin-out, haptic feedback, and smoke puff particles.

- **Visuals & Polish**:
  - 100% vector & canvas-rendered graphics with custom radial and linear gradient lighting—zero heavy raster image assets.
  - 8 unique collectible toy cars with distinct color palettes, gloss, drop shadows, and accessories (Pip, Bumble, Minty, Blueberry, Grapey, Orangy, Pinky, Goldie).
  - Premium typography powered by Google Fonts: **Baloo 2** (headings & scores) and **Quicksand** (UI body & labels).
  - Tailored light aesthetic with soft pastel palettes, glassmorphic cards, and custom bouncy spring physics.

- **Audio & Haptics**:
  - Custom procedural audio engine generating synthesized sound effects via Android `AudioTrack` PCM synthesis (engine hum, lane swipe, coin pickup, close-call chime, shield impact, crash thud, booster activation).
  - Native haptic feedback for taps, close-call near misses, and crashes.

- **Progression & Economy**:
  - Persistent save state powered by **Jetpack DataStore (Preferences)**.
  - Garage system with car unlocking via collected coins.
  - Daily coin rewards, revive systems, and consumable booster inventory.

- **Monetization & Release Ready**:
  - **Google Play Billing v7**: Consumable coin bundles, non-consumable "Remove Ads", and "Zoomy Plus" subscriptions (monthly/yearly with 3-day trial support).
  - **Google Mobile Ads (AdMob)**: Preloaded Rewarded Video ads (revives, coin multiplier, free boosters) and Interstitials, with automatic ad suppression for premium users.
  - **User Messaging Platform (UMP)**: Consent management SDK integration for GDPR/EEA compliance.

---

## 📱 Screens & Architecture

Zoomy uses a single-activity architecture (`MainActivity`) with declarative Jetpack Compose Navigation:

- **SplashScreen**: Animated brand introduction and initialization.
- **HomeScreen**: Hero car showcase, play CTA with breathing animation, quick stats, and navigation to Garage, Shop, and Plus.
- **GameScreen**: Procedural 3-lane road canvas (`RoadView`), live score, speed HUD, active booster meters, and pause overlay.
- **GameOverOverlay**: Run stats, new high score celebrations, revive countdown, and rewarded multiplier buttons.
- **GarageScreen**: Interactive carousel to browse, select, or unlock custom vehicles.
- **ShopScreen**: Coin store, booster restock packs, and "Remove Ads" upgrade.
- **PlusScreen**: Premium subscription tier benefits with plan selection and trial terms.

---

## 🛠️ Tech Stack & Requirements

| Specification | Version / Detail |
|---|---|
| **Language** | Kotlin 2.0.21 |
| **UI Framework** | Jetpack Compose (BOM 2024.12.01) |
| **Min SDK** | API 26 (Android 8.0 Oreo) |
| **Target / Compile SDK** | API 35 (Android 15) |
| **Android Gradle Plugin** | AGP 8.7.3 |
| **Gradle** | 8.9 |
| **Storage** | Jetpack DataStore Preferences |
| **Monetization** | Google Play Billing Library 7.x, Google Mobile Ads (AdMob) 23.x |
| **Architecture** | Single-Activity, Compose Navigation, MVVM / Repository Pattern |

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio Ladybug (2024.2.1)](https://developer.android.com/studio) or newer.
- JDK 17 configured in your environment or Android Studio.
- Android device or emulator running API 26+.

### Building & Running

1. Clone or open the repository in Android Studio:
   ```bash
   cd Zoomy
   ```

2. Assemble the debug build via Gradle:
   - On Windows:
     ```powershell
     .\gradlew.bat assembleDebug
     ```
   - On macOS / Linux:
     ```bash
     ./gradlew assembleDebug
     ```

3. Run unit tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. Install directly to a connected device or emulator:
   ```bash
   ./gradlew installDebug
   ```

---

## 📋 Production Setup

To configure real Google Play In-App Purchases, AdMob ads, and publish your own release builds, refer to the step-by-step instructions in [SETUP.md](SETUP.md):
- Changing `applicationId` and signing with a production keystore.
- Registering In-App Products and Subscriptions in the Google Play Console.
- Configuring real AdMob App ID and Ad Unit IDs.
- Privacy policy, GDPR UMP consent, and Data Safety compliance.

---

## 📂 Project Structure

```
Zoomy/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/zoomy/app/
│   │   │   │   ├── ads/           # AdMob mediation, rewarded & interstitial managers, UMP consent
│   │   │   │   ├── audio/         # AudioTrack PCM procedural sound synthesizer & haptics
│   │   │   │   ├── billing/       # Google Play Billing 7 client and purchase handlers
│   │   │   │   ├── data/          # Jetpack DataStore repository, models, and mock data
│   │   │   │   ├── game/          # 2D deterministic engine, collision logic, and RoadView canvas
│   │   │   │   ├── navigation/    # Compose navigation destinations and routes
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/# Reusable buttons, badges, car sprites, and dialogs
│   │   │   │   │   ├── screens/   # Home, Game, Garage, Shop, Plus, GameOver, Pause, Splash
│   │   │   │   │   └── theme/     # Custom color tokens, typography scales, and motion easings
│   │   │   │   ├── MainActivity.kt# Root activity with edge-to-edge Compose window setup
│   │   │   │   └── ZoomyApp.kt    # Application class with dependency injection container
│   │   │   └── res/               # Vector drawables, Baloo 2 & Quicksand fonts, app launcher icons
│   │   └── test/                  # Game engine collision, scoring, and timestep unit tests
│   └── build.gradle.kts           # Module-level build configuration and dependencies
├── gradle/                        # Gradle wrapper files and version catalogs (libs.versions.toml)
├── SETUP.md                       # Production deployment, AdMob, and Play Billing setup guide
└── README.md                      # Project documentation
```

---

## 📄 License

This project is licensed under the Apache License 2.0 or standard open-source project terms. See project headers for details.
