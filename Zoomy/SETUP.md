# Zoomy Setup Guide

This document outlines the required manual developer steps to prepare Zoomy for production release.

---

### 1. Replace applicationId and Sign the Release Build
1. In `app/build.gradle.kts`, change:
   ```kotlin
   defaultConfig {
       applicationId = "com.yourcompany.zoomy" // replace with your verified bundle ID
   }
   ```
2. Configure your release keystore in `app/build.gradle.kts`:
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file("path/to/your/release-keystore.jks")
           storePassword = System.getenv("KEYSTORE_PASSWORD")
           keyAlias = System.getenv("KEY_ALIAS")
           keyPassword = System.getenv("KEY_PASSWORD")
       }
   }
   buildTypes {
       release {
           signingConfig = signingConfigs.getByName("release")
           isMinifyEnabled = true
           proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
       }
   }
   ```

---

### 2. Configure Google Play Console In-App Products & Subscriptions
Create the following products in the Google Play Console under **Monetize > Products**:

#### A. In-App Products (Consumable)
- **Product ID:** `coins_500` — "500 Coins" ($1.99)
- **Product ID:** `coins_1500` — "1,500 Coins" ($4.99)
- **Product ID:** `coins_4000` — "4,000 Coins" ($9.99)

#### B. In-App Products (Non-Consumable)
- **Product ID:** `remove_ads` — "Remove Ads" ($3.99)

#### C. Subscriptions
- **Subscription ID:** `zoomy_plus` — "Zoomy Plus"
  - **Base Plan 1:** `yearly` ($19.99 / year)
    - Offer ID: `trial3` (3-day free trial offer)
  - **Base Plan 2:** `monthly` ($2.99 / month)
    - Offer ID: `trial3` (3-day free trial offer)

Add your Google account as a **License Tester** under Play Console Settings > License Testing so purchases can be tested without real charges.

---

### 3. Replace AdMob Test IDs with Real Ad Unit IDs
1. In `app/src/main/AndroidManifest.xml`, replace the test application ID:
   ```xml
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"/>
   ```
2. In `app/src/main/java/com/zoomy/app/ads/AdIds.kt`, replace the test ad unit IDs with your production ad units:
   ```kotlin
   object AdIds {
       const val REWARDED = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
       const val INTERSTITIAL = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
   }
   ```

---

### 4. Publish Privacy Policy, Data Safety & Ads Declarations
1. Host a public Privacy Policy URL detailing:
   - AdMob and User Messaging Platform (UMP) consent usage.
   - Google Play Billing transaction handling.
2. Complete the **Google Play Data Safety Form**:
   - Device or other IDs (collected for Advertising/Analytics by Google Mobile Ads SDK).
   - Purchase history (collected by Google Play Billing).
3. Complete the **Ads Declaration**: Select **Yes**, this app contains ads.

---

### 5. Server-side Purchase Verification
Before public launch:
- Integrate Google Play Developer API on your backend to verify purchase tokens (`purchases.products.get` and `purchases.subscriptionsv2.get`).
- Validate incoming tokens server-side to prevent client-side purchase forgery and fraud.

---

### 6. Testing & Release Verification
1. Test on an Android Virtual Device (AVD) running with **Google Play** (not vanilla AOSP).
2. Upload the release AAB to the **Internal Testing Track** on Google Play Console.
3. Verify:
   - Real billing purchases complete, grant coins/subscriptions, and restore correctly.
   - UMP consent dialog appears when testing in European Economic Area (EEA) / UK regions.
   - Ads render correctly and don't display for Plus subscribers or users who bought Remove Ads.
