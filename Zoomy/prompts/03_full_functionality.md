ROLE
You are a senior Android engineer. The Zoomy project (package com.zoomy.app) already has the complete UI and a working 3-lane car dodging game on a Canvas engine. Your job in this phase is to make the whole app FULLY WORK: persistence, game-state restore, audio, haptics settings, daily road, economy, boosters, garage, rewarded and interstitial ads (Google test IDs), Google Play Billing (consumables, non-consumable, subscription), ad consent, lifecycle handling, and tests.

STRICT RULES
1. The visual design is FROZEN. Do not change any color, font, size, radius, duration, easing, layout or animation that already exists. The ONLY visual additions allowed are the four listed under "ALLOWED UI ADDITIONS". Reuse existing components and tokens for them.
2. Every value below is final. Do not choose alternatives, add options, or "improve" anything. If something is unspecified, use the simplest behavior consistent with this document.
3. This phase explicitly OVERRIDES the earlier instruction not to edit Gradle files: you must edit gradle/libs.versions.toml and app/build.gradle.kts and AndroidManifest.xml as specified. Do not change Gradle, AGP, Kotlin, Compose BOM or minSdk/targetSdk versions.
4. No placeholder code, no TODO, no stubs that do nothing. Every button in the app must do what this document says.
5. Output the full source code of every new or changed file, plus SETUP.md.

DEPENDENCIES (exact; add to libs.versions.toml and app/build.gradle.kts)
- androidx.datastore:datastore-preferences:1.1.1
- androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7 and androidx.lifecycle:lifecycle-runtime-compose:2.8.7
- org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0
- org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3 plus plugin org.jetbrains.kotlin.plugin.serialization version 2.0.21
- com.google.android.gms:play-services-ads:23.6.0 (includes the User Messaging Platform consent SDK)
- com.android.billingclient:billing-ktx:7.1.1
- Test: junit:junit:4.13.2, org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0
- Enable buildFeatures { buildConfig = true }.

MANIFEST
- Permissions: INTERNET, ACCESS_NETWORK_STATE, VIBRATE, com.android.vending.BILLING, com.google.android.gms.permission.AD_ID.
- Inside <application>: <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="ca-app-pub-3940256099942544~3347511713"/> (Google's official TEST app id). Register android:name=".ZoomyApp" on <application>.
- Activity: android:screenOrientation="portrait", android:launchMode="singleTop".

ARCHITECTURE
- Package folders (create): data/ (Repository, Prefs, Models), game/ (existing engine; add GameSession, DailyRules), audio/ (SynthSounds, SoundManager), ads/ (AdsManager, ConsentManager, AdIds), billing/ (BillingManager, Products), ui/ (existing).
- Single Application subclass ZoomyApp (registered in the manifest) holds an AppContainer with: PrefsRepository, SoundManager, AdsManager, ConsentManager, BillingManager. Use manual DI, no Hilt.
- ViewModels (androidx ViewModel + StateFlow, collected with collectAsStateWithLifecycle): AppViewModel (economy, entitlements, settings, daily), GameViewModel (the engine session). Screens only observe state and send events.
- All disk work on Dispatchers.IO; the game loop runs on the main thread via withFrameNanos and must not allocate in the per-frame path (reuse arrays, Paths, Brushes, Paints).

PERSISTENCE (DataStore Preferences, file "zoomy_prefs")
Keys and defaults:
- coins Int = 1250 (first launch only); bestScore Int = 0; bestDistanceM Int = 0; totalCoinsEarned Int = 0; gamesPlayed Int = 0
- boosterShield Int = 2; boosterMagnet Int = 1; boosterSlowmo Int = 0
- soundOn Boolean = true; hapticsOn Boolean = true
- removeAds Boolean = false; plusActive Boolean = false (cached from billing; billing is the source of truth)
- ownedCars StringSet = {"pip","bumble","minty","blueberry","grapey"}; equippedCar String = "pip" (ids: pip, bumble, minty, blueberry, grapey, orangy, pinky, goldie)
- lastPlusGrantDate String = ""; dailyClaimedDate String = ""; dailyBestDate String = ""; dailyBest Int = 0
- gameOverCount Int = 0; lastAdShownEpochMs Long = 0
- processedTokens StringSet = {}
Rules: coins never go below 0; every write is atomic via DataStore.edit. Expose Flows through PrefsRepository. There is no mid-run save: a crash or process death mid-run simply ends it (the run restarts from Play).

GAME SESSION RULES
- GameSession wraps the existing engine and exposes: score, bestScore (live), distanceM, runCoins, speed, combo, isGameOver, shieldActive, magnetRemainingMs, slowmoRemainingMs, revivesUsedAd (0/1), revivesUsedCoins (0/1), mode (NORMAL or DAILY), laneCount (3).
- Every run starts fresh at Play (normal: Random seeded from System.nanoTime; daily: see DAILY ROAD).
- Lifecycle: on ON_PAUSE of the activity, pause the engine loop and show the existing Pause overlay if the Game screen is visible. Resuming from the overlay continues exactly where it stopped. All time-based rules (spawn timer, combo window, booster timers, invulnerability, crash delay) use the engine's simulated time, not wall-clock, so pausing never advances them.
- Game over (after the crash animation): increment gamesPlayed and gameOverCount; if score > bestScore for NORMAL mode, update bestScore and mark "new best" (shows the existing NEW BEST pill; plays the new-best sound; spawns the existing confetti for 1 pass: 12 rectangles 8x14dp in the car colors); update bestDistanceM; coins += runCoins * (plusActive ? 2 : 1) and totalCoinsEarned += the same, shown in the existing "+N coins" line (N includes the Plus doubling). Coins are credited only at game over (not live), and also when the user leaves via Home from the Pause card ("Home" from Pause ends the run and credits coins without a Game Over screen).
- Revive (Continue): allowed at most once via ad and once via 50 coins per run. A used option is shown at alpha 40% and tapping it shakes it (translationX +-6dp, 4 cycles, 300ms) and does nothing else. Coin revive with coins < 50: shake the button and show the Snackbar "Not enough coins". After a revive the 2000ms invulnerability from the existing spec applies and runCoins/score continue.

DAILY ROAD
- Date source: java.time.LocalDate.now() in the device's default zone. Re-evaluate on app resume and every second on Home (for the countdown).
- Seed = the date's epoch day. Daily mode uses a separate Random seeded with this value for the spawner (lane choice, obstacle type, coin lines), so everyone gets the same traffic that day. Daily runs start at the day's modifier values.
- Modifier by day of week (Mon..Sun): Mon "Fast start" (start speed 340 dp/s); Tue "Rainy road" (lane change takes 220ms); Wed "Rush hour" (spawn interval x0.8); Thu "Trucks day" (Truck 55%, Car 25%, Cone 20%); Fri "Coin rain" (a coin line in a free lane on every row); Sat "Zig-zag day" (every row has exactly 2 obstacle lanes); Sun "Cruise day" (no change). The Home card Title shows the day's name.
- Target: reach a score of 3,000 in the daily run. On first reaching it on a given date (dailyClaimedDate != today): coins += 50 (x2 with Plus), set dailyClaimedDate = today, show confetti once and the claim pill (see ALLOWED UI ADDITIONS). Replays are allowed without reward; dailyBest tracks the best score of that date. The run continues after claiming.
- Home card behavior: tapping the card starts the Daily run (iris transition from the card center instead of the Play button). Label shows the live countdown "Resets in HH:MM:SS" until local midnight; when dailyClaimedDate == today the Label instead shows an Icons.Rounded.CheckCircle 14dp Lime deep followed by "Claimed", and the "+50 coins" pill gets alpha 40%.

BOOSTERS AND ECONOMY
- Counts persisted. Using a booster decrements its count (never below 0) and applies the existing effect (Shield: absorbs the next crash, stays until used, only one active at a time; Magnet: 8000ms; Slow-mo: 4000ms). A booster can't be used while its effect is active (tap does the existing shake). Count 0 shows the existing ad badge: tapping it requests a rewarded ad; on reward, +1 of that booster. If no ad is available: Snackbar "Ad isn't ready. Try again soon".
- Shop coin purchases of boosters: "Shield x3" costs 100 coins (+3 Shield), "Magnet x3" costs 150 coins (+3 Magnet). Not enough coins: shake the CompactButton and show Snackbar "Not enough coins".
- Plus daily grant: when plusActive and lastPlusGrantDate != today, set each of the three boosters to max(current, 3) and lastPlusGrantDate = today. Evaluate on app start, on resume and after a purchase.
- Garage: tapping an owned car's pill equips it. Locked prices: bumble 500, minty 900, blueberry 1500, grapey 2500, orangy 4000, pinky 6000 coins (buy once, auto-equips; not enough coins: shake the pill and Snackbar "Not enough coins"). "goldie" requires Plus: if not Plus, tapping its pill opens the Plus screen; with Plus it is owned and equippable while Plus is active; if Plus lapses and it was equipped, equippedCar falls back to "pip". The equipped car is used in the Game, the Home attract road, the Splash and the Plus hero is always Goldie.
- Coins are shown everywhere through the single AppViewModel coins state (StatChip Coins on Home, Shop). Coin changes animate the count: count up/down over 500ms EmphasizedDecelerate.

AUDIO (synthesized, no asset files)
- At first launch, SynthSounds generates 16-bit mono 44100 Hz PCM buffers in memory, writes each as a WAV file into cacheDir/sfx/ (regenerate if missing), and SoundManager loads them into one SoundPool (maxStreams 8, AudioAttributes USAGE_GAME, CONTENT_TYPE_SONIFICATION).
- Sounds (all with a 5ms linear attack, exponential decay envelope exp(-t / tau), peak amplitude 0.5, then a final 10ms linear fade to zero):
  - tap: sine 1000 Hz, 40ms, tau 12ms.
  - swipe (lane change): sine sweeping 500 -> 700 Hz over 70ms, tau 25ms.
  - coin: two sines 1318 Hz then 1760 Hz, 60ms each, tau 30ms. Rate limit: max 1 per 50ms; pitch rises by 2^(n/12) for n = consecutive coins within 600ms (max 7).
  - close_2 .. close_5: sine sweeping f -> 1.5 f over 120ms (tau 45ms) with f = 600 * 2^((combo - 2) * 2 / 12) Hz.
  - shield: sine sweeping 400 -> 800 Hz over 150ms, tau 60ms; shield_break: noise 120ms at 0.2 plus sine 300 Hz 100ms.
  - magnet: sine 520 Hz with 8 Hz tremolo, 220ms, tau 100ms; slowmo: sine sweeping 700 -> 300 Hz over 250ms, tau 90ms.
  - crash: noise burst 300ms (amplitude 0.35, exp decay tau 90ms) plus sine sweeping 160 -> 70 Hz over 300ms.
  - gameover: 4 descending notes 392, 330, 262, 196 Hz, 150ms each, tau 90ms.
  - newbest: 5-note ascending arpeggio 523, 659, 784, 1046, 1318 Hz, 80ms each.
  - reward: 2-note 784 -> 1046 Hz, 100ms each (plays when a coin or booster reward is credited).
  - pop: sine 700 -> 1400 Hz over 80ms, tau 25ms (obstacle pops on revive and Shield).
- Triggers: tap on every ZoomyButton, IconButton and chip press; swipe on each lane change; coin on pickup; close_n on close calls (the sound for combo 1 is none); shield/magnet/slowmo on activation; crash on crash; gameover and newbest on the overlay's appearance (newbest 300ms after gameover); reward on credited rewards.
- If soundOn is false, SoundManager plays nothing. No background music.

HAPTICS
- Keep the existing HapticFeedbackType calls but route every haptic through one Haptics helper that does nothing when hapticsOn is false.

ADS (AdMob, Google TEST ids only; real ids replaced later by the developer in one file: ads/AdIds.kt)
- Test ids: REWARDED = "ca-app-pub-3940256099942544/5224354917", INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712".
- ConsentManager: on app start, request consent info with UserMessagingPlatform and show the form if required; initialize the Mobile Ads SDK only after canRequestAds() is true. If consent is not required it initializes immediately. Set RequestConfiguration maxAdContentRating = "G".
- AdsManager: preloads one rewarded and one interstitial ad; reloads after each is shown or fails (retry with exponential backoff 2s, 4s, 8s, then stop until the next screen visit). All ad callbacks run on the main thread.
- No ads at all (no loading, no showing) when removeAds or plusActive is true.
- Rewarded placements: (1) Game Over "Continue" (revive), (2) booster ad badge (+1 booster). Grant the reward ONLY in onUserEarnedReward. If the user closes the ad early, grant nothing and return to the previous screen state. While an ad is showing, pause the physics loop.
- Interstitial placement: shown on Game Over when the user taps "Play again" or "Home" (not before the overlay appears), only if gameOverCount is a multiple of 3 AND at least 90000 ms have passed since lastAdShownEpochMs (updated whenever any ad is shown). Never on the first two game overs.
- If an ad is not ready when needed for a rewarded placement: Snackbar "Ad isn't ready. Try again soon".
- Pause card has a "Privacy settings" TextButton (see ALLOWED UI ADDITIONS) that opens the UMP privacy options form; it is only visible if privacyOptionsRequirementStatus == REQUIRED.

BILLING (Google Play Billing 7.1.1)
- Products (ids): INAPP consumables "coins_500", "coins_1500", "coins_4000"; INAPP non-consumable "remove_ads"; SUBS product "zoomy_plus" with base plan ids "yearly" and "monthly", each also with a free-trial offer id "trial3".
- BillingManager: BillingClient.newBuilder(context).setListener(...).enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build(). Connect on app start; auto-reconnect with exponential backoff 1s, 2s, 4s, 8s, 16s (max 5 tries) and again on every app resume.
- On connect: queryProductDetails for all ids; queryPurchasesAsync for INAPP and SUBS to restore entitlements. Prices shown in the UI replace the mock strings when ProductDetails are available (formatted price); if unavailable, show the mock prices and make purchase buttons show Snackbar "Store unavailable. Try again later" when tapped.
- Coin packs: on PURCHASED, consume the purchase (consumeAsync), then add coins 500 / 1500 / 4000 (x2 NOT applied) and play the reward sound; guard against double credit using the purchase token (store processed tokens in the StringSet "processedTokens").
- remove_ads: acknowledge on PURCHASED, set removeAds = true. Restore sets it as well.
- Plus: on PURCHASED subscription, acknowledge, set plusActive = true. When the user chooses the yearly plan use base plan "yearly", monthly uses "monthly"; select the offer with offerId "trial3" if present in subscriptionOfferDetails (this is how the free trial is applied), else the base offer. Entitlement is derived from queryPurchasesAsync on each start and resume: plusActive = any SUBS purchase for "zoomy_plus" in state PURCHASED. If none, plusActive = false (and the daily grant, Goldie-car fallback and ads rules update immediately).
- PENDING purchases: show Snackbar "Purchase pending" and grant nothing until PURCHASED arrives.
- Errors: USER_CANCELED shows nothing; ITEM_ALREADY_OWNED triggers a restore; every other error shows Snackbar "Purchase failed. Try again".
- Restore purchases buttons (Plus screen TextButton "Restore purchase") run a full restore and show Snackbar "Purchases restored" (or "Nothing to restore" if nothing was found).
- Launch the billing flow from the current Activity through BillingManager.launch(activity, productDetails, offerToken?).
- Debug builds only (BuildConfig.DEBUG): long-press on the StatChip Coins adds 1000 coins.

ALLOWED UI ADDITIONS (the only visual changes in this phase)
1. Snackbar: pill, Ink fill, text Quicksand Bold 14sp white, horizontal padding 20dp, vertical padding 12dp, max width screen - 40dp, centered, bottom margin 24dp + inset, appears with translationY +24dp -> 0 and alpha 0 -> 1 over 250ms EmphasizedDecelerate, holds 2200ms, fades out over 200ms. One at a time; a new one replaces the current.
2. Pause card additions: after "Restart" (12dp gap) add two rows (each 48dp, full width): Title "Sound" with a Switch on the right (on = Lime); Title "Haptics" with a Switch. Then, only if required, a TextButton "Privacy settings". Then the existing "Home" TextButton. Toggles write soundOn/hapticsOn immediately.
3. Daily mode Game screen: the "Best" pill text becomes "Daily · 3,000 to win". After claiming, the pill turns Lime tint fill with Lime deep text "+50 coins claimed" and Icons.Rounded.CheckCircle 16dp Lime deep.
4. Garage CarCard pills: "EQUIP" / "EQUIPPED" / price / "PLUS" states exactly as already drawn (now live).

NAVIGATION ADDITIONS
- Android back button/gesture: on Game opens Pause (if Pause is open it resumes); on Game Over overlay it behaves like "Home"; on Garage/Shop/Plus it pops the screen; on Home it exits the app. Keep the existing transitions.
- Configuration changes are locked by portrait orientation; state must also survive process death through the saved run and DataStore.

ERROR AND EDGE CASES (must be handled)
- Offline: ads and billing fail gracefully with the specified Snackbars; the game, daily puzzle, collection and settings work offline.
- App killed during a run: the run restores on next Play (normal or daily), after discarding anything past local midnight for daily.
- Rapid double taps on purchase, revive or booster buttons must not double-spend (debounce 500ms and use an in-flight flag).
- Coins, boosters and flags are modified only through the repository functions; UI never writes DataStore directly.
- Clock changes backwards: daily claim uses dailyClaimedDate equality only, so it cannot be claimed twice on the same date.
- Never crash on a null Activity, missing ProductDetails or an ad callback after the screen has closed.

TESTS (JUnit, in app/src/test)
1. Engine determinism: with a fixed seed and fixed lane inputs, 600 steps produce identical object positions on two runs.
2. Spawner rules: 1000 generated rows never block all 3 lanes; a coin line only appears in a free lane.
3. Collision and Shield: overlapping hitboxes crash; with Shield the obstacle is removed and the run continues.
4. Score and combo: close calls within 2000ms raise the combo up to 5; close-call score = 25 x combo; coin = +10; distance score = floor(distance / 10).
5. Daily: the same date gives the same spawn sequence; different dates differ; modifier by weekday matches the table.
6. Economy: coins never go negative; Plus doubles run coins; the Plus daily grant tops boosters up to 3 once per date; car purchase deducts coins and auto-equips; Goldie falls back to Pip when Plus lapses.
7. Interstitial rule: shown only on every 3rd game over and only after 90s since the last ad.

SETUP.md (deliver this file)
List these manual developer steps clearly: (1) replace applicationId and sign the release build, (2) create the Play Console products with the exact ids and base plans above and add a license-tester account, (3) replace the AdMob test IDs in ads/AdIds.kt and the manifest APPLICATION_ID with real ones, (4) publish a privacy policy URL and complete Data safety and Ads declarations, (5) add server-side purchase verification before launch, (6) test on an emulator image with Google Play services and on an internal testing track.

DELIVERY CHECKLIST (verify before finishing)
- ./gradlew assembleDebug and ./gradlew testDebugUnitTest both pass.
- A full flow works end to end: first launch -> Splash -> Home (coins 1,250) -> Play -> lane changes, coins, close calls award score and sounds -> background the app -> reopen -> the Pause overlay is shown and Resume continues -> crash -> game over, coins credited, best score saved -> Continue via test rewarded ad -> second game over -> Play again.
- Daily card starts the daily run with the day's modifier; reaching 3,000 claims 50 coins once per date.
- Garage and Shop: coin packs, Remove ads, car buy/equip and booster purchases all behave as specified; Plus purchase flips ads off, unlocks Goldie and grants boosters; Restore works.
- Consent form appears when required; ads never appear for Plus or Remove-ads users.
- No visual regressions: every screen looks exactly as before except the allowed additions.
