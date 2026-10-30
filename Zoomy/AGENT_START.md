# READ FIRST
This Android project is ALREADY scaffolded and configured (Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01, minSdk 26, targetSdk 35, package com.zoomy.app, fonts Baloo 2 (headings, numbers) and Quicksand (text) variable in res/font, Gradle wrapper, manifest, theme, launcher icon).

DO NOT: run any project generator, create a new project, change Gradle/AGP/Kotlin versions, edit libs.versions.toml or build.gradle.kts files (until phase 3 says so), rename the package, or re-download fonts.
DO: start writing code immediately in app/src/main/java/com/zoomy/app/ using the existing folders: ui/theme, ui/components, ui/screens, navigation, data, game.
Overwrite MainActivity.kt (placeholder). The Baloo and Quicksand FontFamily objects already exist in ui/theme/Type.kt.
Run ./gradlew assembleDebug once at the end.

Phase order: prompts/02_ui_and_core_gameplay.md, then prompts/03_full_functionality.md.
# READ FIRST
This Android project is ALREADY scaffolded and configured (Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.12.01, minSdk 26, targetSdk 35, package com.zoomy.app, fonts Baloo 2 (headings, numbers) and Quicksand (text) variable in res/font, Gradle wrapper, manifest, theme, launcher icon).

DO NOT: run any project generator, create a new project, change Gradle/AGP/Kotlin versions, edit libs.versions.toml or build.gradle.kts files, rename the package, or re-download fonts.
DO: start writing UI code immediately in app/src/main/java/com/zoomy/app/ using the existing folders: ui/theme, ui/components, ui/screens, navigation, data, game (game engine and road renderer).
Overwrite MainActivity.kt (placeholder). The Baloo and Quicksand FontFamily objects already exist in ui/theme/Type.kt.
Run ./gradlew assembleDebug once at the end.

Then follow the full UI specification below.

ROLE
You are a senior Android UI and game engineer. Build the COMPLETE UI and the core gameplay of an Android casual game called "Zoomy" (a simple top-down 3-lane car dodging game) using Kotlin + Jetpack Compose in the existing project (package com.zoomy.app). The game is drawn on a Compose Canvas with a small custom deterministic 2D game engine written by you in game/Engine.kt (no external game or physics library). No ads SDK, no billing, no audio, no database, no networking. Shop, Plus, boosters and coins are UI-only with hardcoded values in MockData.kt. Best score is kept in memory only.

STRICT RULES
1. Every value below is final. Do not choose alternatives, add options, or "improve" anything. If something is unspecified, reuse the nearest specified value. Do not introduce any new color, font, shape, duration or easing.
2. Light-only. No dark mode, no black backgrounds, no neon. Ignore the system dark theme.
3. No raster images. Everything is Canvas drawing, Brush gradients and Icons.Rounded vector icons.
4. Material 3 may be used for layout/accessibility only; visually every component follows this spec.
5. Edge-to-edge, transparent system bars, dark status-bar icons. Reference device 412 x 915 dp. Sizes in dp, text in sp.
6. If the system animator duration scale is 0, skip decorative animations (physics still runs).
7. Fonts already exist in Type.kt: Baloo (Display, Score, H1, H2, Number) and Quicksand (everything else).
8. There is NO bottom navigation bar, NO side drawer, NO top tab bar, NO floating action button anywhere.

DESIGN TOKENS (Theme.kt, use only these)
Colors:
- Canvas: vertical gradient #E6F7FF (top) to #FFF9E6 (bottom)
- Surface: #FFFFFF at 80% | Surface border: #FFFFFF 1.5dp
- Ink (text primary) #2B3A55 | secondary #6B7A99 | tertiary #B0BAD0
- Tomato (primary) #FF5A4D | Tomato light #FF8577 | Tomato deep #D93A2E | Tomato tint #FFE3E0
- Sky #4FB8FF | Sky deep #2E9BE0 | Sky tint #DDF3FF
- Sun #FFC83D | Sun deep #E0A314 | Sun tint #FFF1C4
- Lime #7ED957 | Lime deep #4FAE2B | Lime tint #E3F7D8
- Grape #A98BFF | Grape deep #7A5FE0 | Grape tint #ECE5FF
- Road #8E9AB5 | Road deep #7A87A5 | Danger #FF7A7A | Locked grey #E4E8F0 | Locked deep #CBD1E2 | Divider: Ink at 8%

Car colors (index 1-8, the garage): 1 "Pip" #FF5A4D; 2 "Bumble" #FFC83D; 3 "Minty" #5ED9B0; 4 "Blueberry" #4FB8FF; 5 "Grapey" #A98BFF; 6 "Orangy" #FF9A4D; 7 "Pinky" #FF86B5; 8 "Goldie" #FFD84D (wears a small crown on the roof). Car deep color = base darkened 14%; light color = base mixed 55% with white. Traffic cars use these same 8 colors except the player's.

Typography:
- Display: Baloo ExtraBold 56sp / 56sp
- Score: Baloo ExtraBold 40sp / 44sp
- H1: Baloo Bold 30sp / 34sp
- H2: Baloo Bold 20sp / 26sp
- Number: Baloo ExtraBold 18sp / 22sp
- Title: Quicksand Bold 17sp / 24sp
- Body: Quicksand SemiBold 15sp / 22sp
- Label: Quicksand Bold 13sp / 18sp
- Caption: Quicksand Bold 11sp / 14sp, UPPERCASE, letterSpacing 1sp

Shapes:
- Buttons: full pill (Play button height 64dp; other buttons 56dp; compact 44dp)
- Cards: RoundedCornerShape(32dp)
- Chips, badges, progress tracks: full pill
- Icon buttons: 48dp circle
- Bottom sheet / overlay cards: RoundedCornerShape(36dp)
- Road: custom (below)

Spacing: screen horizontal padding 20dp; gap between cards 12dp; card inner padding 20dp; section title to content 12dp.

Elevation: Card: y 8dp, blur 24dp, Ink at 10%. Button: y 8dp, blur 16dp, in the button's deep color at 45%.

EASINGS (Motion.kt)
- Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
- EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
- Bounce spring = spring(dampingRatio 0.45, stiffness 500)
- Press spring = spring(dampingRatio 0.55, stiffness 700)
- Push spring = spring(dampingRatio 0.8, stiffness 300)

COMPONENTS
1. CarSprite(color, height, crown): draws one top-down toy car, width = 0.52 x height. Body: rounded rectangle (corner 0.28 x width) with a vertical gradient (light top -> base -> deep bottom). Soft drop shadow: Ink at 18%, offset y 0.04 x height. Windshield: rounded rectangle (0.70 x width by 0.22 x height) in Sky tint at 90%, near the front (top) at 0.18 x height from the top; rear window 0.70 x width by 0.12 x height at 0.62 x height. Roof: rounded rectangle 0.70 x width by 0.24 x height between them, base color lightened 12%. Gloss: a white 45% ellipse (0.5 x width by 0.16 x height) on the roof's left. Headlights: two circles of 0.14 x width in Sun tint at the front corners (inset 0.10 x width) and two taillights of 0.10 x width in Danger at the back. Wheels: four rounded rectangles (0.16 x width by 0.20 x height) in Ink at 85%, half outside the body at 0.20 and 0.72 x height. Crown (Goldie): 5-point Sun crown width 0.5 x width on the roof with Sun deep outline 1.5dp. Draw with cached Paths/Brushes; no per-frame allocation. Trucks (traffic only) use CarSprite scaled to 1.4 x height with a cargo box: rounded rectangle on the back 62% in Ink at 15% fill with a white 60% stripe.
2. ZoomyButton (Primary/Accent): pill, vertical gradient (light top -> base bottom; Primary = Tomato light -> Tomato; Accent = Sun lightened 12% -> Sun), gloss: a white 35% ellipse covering the top 45% of the button inset 8dp horizontally, text Quicksand Bold 17sp UPPERCASE letterSpacing 0.5sp (Primary white; Accent Ink), Button shadow. Optional leading icon 24dp, 8dp gap. Idle (Play button only): scaleX 1.00 <-> 1.03 and scaleY 1.00 <-> 0.97 in counter-phase, 1600ms sinusoidal (Standard), infinite. Press: scale (1.06 x, 0.92 y) for 80ms then Press spring back to (1, 1), shadow alpha halves while pressed.
3. SecondaryButton: height 56dp, pill, Surface fill, border 2dp Tomato tint, text Bold 16sp Tomato deep. Same press.
4. CompactButton: height 44dp, pill, padding 20dp, ZoomyButton Primary style, text 14sp.
5. IconButton: 48dp circle, Surface fill + border, Card shadow at 60%, icon 24dp Ink. Press: scale (1.08, 0.92) then Press spring.
6. TextButton: Quicksand Bold 15sp Tomato deep, 48dp min height.
7. Card: Surface fill + border, RoundedCornerShape(32), Card shadow, padding 20dp. Tappable: press scale 0.97, Press spring.
8. StatChip: height 40dp pill, Surface fill + border, padding 14dp; icon 22dp + Number text. Variants: Coins (Icons.Rounded.MonetizationOn, Sun deep, "1,250"), Best (Icons.Rounded.EmojiEvents, Grape deep, "12,480").
9. BoosterButton: 72dp wide x 80dp tall, RoundedCornerShape(28), Surface fill + border, Card shadow; icon 28dp over Label; a badge at the top-right (offset 6dp): 22dp circle Grape deep with white Number 12sp count, or an ad badge (Lime circle with Icons.Rounded.PlayArrow 14dp white) when count is 0.
10. RoadView: see GAME below.
11. PackCard, CarCard, PlanCard: described in screens.

GAME: ROAD AND ENGINE (game/ folder)
Road geometry: interior 340dp wide x 540dp tall, centered horizontally, 3 lanes of 113.33dp; lane center x = 56.67, 170, 283.33. Visual: Road color fill (vertical gradient Road -> Road deep), white 90% curb strips 6dp wide on both sides, lime grass strips 24dp wide outside both curbs with 5 tiny white flower dots and 3 Lime deep bushes (circles 14-22dp) scrolling with the road, rounded corners 28dp on the whole frame (clip), a 3dp white 90% frame stroke, floor shadow ellipse (width 280dp, height 18dp, Ink at 10%) 10dp below. Lane markings: white 90% dashes (4dp wide, 28dp long, 28dp gap) on the two lane borders scrolling down at world speed.
Engine (fixed timestep 1/120 s, accumulate frame time, max 4 steps per frame; units dp and seconds):
- World scrolls downward: speed starts at 260 dp/s, increases 8 dp/s per second up to 620 dp/s. distance += speed x dt.
- Player car: size 44 x 84dp (hitbox 36 x 76), fixed y = 420dp from the road top, lane index 0-2 (start 1). Input: tap on the left half of the road moves one lane left, on the right half one lane right; a horizontal swipe over 40dp does the same; at most one lane per input; inputs during a lane change are queued (max 1). Lane change: x moves to the new lane over 140ms Standard; the car tilts +-8 degrees during the move (rotation by lane velocity) and returns.
- Spawner: every max(0.55, 1.2 - speed / 1000) seconds spawn a row at y = -140dp from a Random(seed): choose 1 lane (65%) or 2 lanes (35%) to hold obstacles (never 3); each obstacle is Car 55% (44 x 84), Truck 25% (52 x 120, hitbox 46 x 112), Cone 20% (24 x 24). With 40% chance put a coin line (5 coins, 20dp circles, 56dp apart) in a free lane. Obstacles scroll at world speed; Remove objects below the road.
- Coins: collected when the player's hitbox overlaps; +1 coin and +10 score; coin pickup shows a sparkle (4 stars, 6dp, Sun light, 240ms EmphasizedDecelerate) and the coin counter pops 1.0 -> 1.15 -> 1.0 (Press spring).
- Score = floor(distance / 10) + 10 x coins + close-call bonuses.
- Close call: when an obstacle in a lane adjacent to the player's lane passes the player's y-range (its center crosses the car's center y), award 25 x combo points. Combo: another close call within 2000ms raises combo by 1 (max 5), otherwise it resets to 1. Combo >= 2 shows a floating text at the car: "Close!" (2), "Zoom!" (3), "Wow!" (4+) in Baloo ExtraBold 22sp Grape with a 3dp white outline, rising 40dp and fading over 700ms Standard.
- Crash: overlap of the hitboxes with an obstacle ends the run (unless Shield is active). Crash effect: road shake +-5dp 6 cycles 300ms, the car spins 0 -> 360 degrees over 400ms EmphasizedDecelerate and fades, a puff burst (8 circles 12dp, white 80% -> 0%, radial to 40dp, 400ms Standard) and 8 sparkles (Sun), HapticFeedbackType.LongPress, then the Game Over overlay after 600ms.
- Speed lines: when speed > 480 dp/s draw 6 thin white 30% vertical streaks (2dp x 60dp) at fixed lane-border x positions scrolling 1.6 x faster.
- Face/animation: the player car has an idle 1dp vertical engine jitter at 30 Hz (render only); headlights glow (Sun tint circle 1.6 x size, alpha 25%) always on.
Boosters (mock counts: Shield 2, Magnet 1, Slow-mo 0):
- Shield: next crash is absorbed; a Sky ring (3dp, Sky at 70%) pulses around the car (scale 1.0 <-> 1.06, 600ms); on absorbing, the obstacle pops (scale 1.0 -> 1.25 then 0 over 240ms with 6 sparkles), the ring bursts and the shield ends.
- Magnet: for 8000ms coins within 140dp of the car fly to it at 600 dp/s; a Grape ring (2dp dashed, rotating 1 turn / 1200ms) shows around the car.
- Slow-mo: for 4000ms world speed is multiplied by 0.6 (smoothly over 200ms Standard in and out) and the road gets a Sky tint overlay at 8%.
Count 0 shows the ad badge; tapping it just grants +1 (UI mock) and plays the Press spring.
Revive (Game Over "Continue"): all obstacles with y in the lower 300dp of the road are removed with 30ms stagger pops, speed resets to 260 dp/s, and the car is invulnerable for 2000ms (alpha pulses 100% <-> 40%, 200ms), then play resumes.
Attract mode (Home hero road): the same engine at 0.6 scale (road 204 x 324dp interior), the car switches to a random free lane every 1200ms, auto-spawns traffic and coins, speed fixed at 300 dp/s, collisions disabled (the car always picks a lane that is free of traffic for the next 200dp, so it never crashes), no score.

EVERY SCREEN BACKGROUND
Canvas gradient plus 6 soft floating "puffs": circles (diameters 40, 64, 28, 52, 36, 72dp) filled white at 35%, at fixed positions spread over the screen, each drifting vertically by 24dp (sinusoidal, 9000-14000ms periods, offset phases). Static positions (dp from top-left): (30,140), (330,260), (60,520), (300,640), (180,780), (340,860).

NAVIGATION & TRANSITIONS (explicit)
- Splash -> Home: fade-through 400ms Standard (outgoing fades 0-150ms; incoming fades 150-400ms with scale 0.97 -> 1).
- Home PLAY -> Game: iris reveal: an Tomato circle starts at the Play button center and expands to cover the screen over 520ms EmphasizedDecelerate; the Game screen then fades in over 200ms while the circle fades out over 200ms. Game -> Home: reverse iris, 400ms Standard.
- Home -> Garage / Shop and Shop -> Plus: push. Incoming screen translationX 100% -> 0 with Push spring, outgoing translationX 0 -> -20% and alpha 1 -> 0.6. Back: reverse over 350ms Standard. Plus (from Home or Shop) slides up from the bottom instead, 450ms EmphasizedDecelerate; close slides down 320ms Standard.
- Pause and Game Over overlays: scrim Ink at 36% fades in 300ms. Pause card: scale 0.8 -> 1 with Bounce spring. Game Over card: translationY -420dp -> 0 with Bounce spring after the 300ms screen shake. Dismiss: 220ms Standard.
- Play again (Game Over): crossfade 250ms Standard while the road resets.

MOTION CATALOG (all subtle except the satisfying game moments)
- Stagger on entry: each card/list item alpha 0 -> 1, translationY 20dp -> 0, scale 0.95 -> 1, 380ms EmphasizedDecelerate, 45ms delay per index.
- Score digits: each digit that changes rolls vertically (old moves up 14dp and fades out, new enters from 14dp below), 250ms Standard. The score pill scales 1.0 -> 1.08 -> 1.0 with Press spring on each close call.
- Coin counters count up over 800ms EmphasizedDecelerate on first entry.
- Splash: a 120 x 200dp road strip (same road drawing) in the center; the Pip car drives in from below the screen to the center over 700ms EmphasizedDecelerate and stops with a squash (scaleY 0.94 -> 1, Bounce spring); then the wordmark "Zoomy" Baloo ExtraBold 48sp Tomato pops in (scale 0.5 -> 1 with Bounce spring) 500ms after the stop; the tagline "Dodge. Collect. Zoom." fades in 300ms after; a loading track (160dp x 8dp pill, Ink at 8%, Tomato fill) fills 0 -> 100% over 1400ms linear.
- Booster tap, chip tap, card press: squash press as defined.
- Haptics: HapticFeedbackType.LongPress on Play, on crash and on Game Over buttons; HapticFeedbackType.TextHandleMove on each close call.

SCREENS (build all 8; routes in brackets)

[splash] hold 400ms after the loading track completes, then auto-navigate.

[home]
- Top row at status inset + 12dp: StatChip Coins left, StatChip Best right.
- 16dp: Caption secondary centered "ZOOMY"; hero: the attract-mode road (above) centered, 340dp wide visual (204 x 324 interior scaled) inside no card.
- 20dp below the road: row (centered, 16dp gaps): 64dp circular IconButton-style button (Surface fill) with Icons.Rounded.DirectionsCar (Garage), the Play ZoomyButton (width 200dp, height 64dp, leading Icons.Rounded.PlayArrow, text "Play", idle breathing), 64dp circular button with Icons.Rounded.Storefront (Shop).
- 24dp: Card (tappable, padding 20dp): row: left column: Caption Tomato deep "TODAY'S ROAD", Title "Rush hour", Label secondary "Resets in 06:12:40"; right: pill Sun tint fill, Sun deep text "+50 coins" with Icons.Rounded.MonetizationOn 16dp. (Decorative; no navigation.)
- 12dp: Card (tappable -> Plus) Sun tint fill, no border: Caption Sun deep "ZOOMY PLUS", Title "No ads, double coins", trailing Icons.Rounded.WorkspacePremium 36dp Sun deep.
- Scroll bottom padding 24dp + inset. The screen scrolls if the content does not fit.

[game] (no navigation)
- Top row at status inset + 12dp, height 72dp: left IconButton Icons.Rounded.Pause (opens Pause); center column: Caption secondary "SCORE" over Score text with the rolling digits (current score); right column: a 64dp white (Surface) circle with the run's coin count (Number) centered and Icons.Rounded.MonetizationOn 14dp Sun deep above it, Caption secondary "COINS" under it (2dp gap).
- 8dp: a centered pill (height 28dp, Surface fill + border, padding 12dp): Icons.Rounded.EmojiEvents 16dp Grape deep + Label "Best 12,480".
- 12dp: RoadView (interior 340 x 540dp).
- 10dp: Speed strip: Caption secondary "SPEED" at left, a 10dp pill track (Ink at 8%, Sky fill = (speed - 260) / 360) 200dp wide, Number "128 km/h" at right (km/h = round(speed / 4)).
- 10dp: Booster row, 3 BoosterButtons centered with 16dp gaps: Shield (Icons.Rounded.Shield, count 2), Magnet (Icons.Rounded.Adjust, count 1), Slow-mo (Icons.Rounded.Timer, count 0 -> ad badge).
- Score starts at 0 each run. The road starts empty (first row spawns after 600ms). Best updates in memory.

[pause] (overlay over Game; scrim)
- Card width 300dp, padding 24dp, centered: H1 centered "Paused"; 20dp ZoomyButton Primary "Resume" full width; 12dp SecondaryButton "Restart"; 8dp centered TextButton "Home". The game loop is paused while it is open.

[game_over] (overlay over Game; scrim)
- Card width 320dp, padding 24dp, centered, with pop-in: if score beats best, a Sun pill at the top (overlapping the card top edge by 50%) "NEW BEST!" Caption Ink with Icons.Rounded.Star 14dp; Caption secondary centered "CRASHED!"; 4dp Display Ink centered with the final score; 12dp a row of two equal Tint pills (height 56dp, RoundedCornerShape(20)): Sky tint "DISTANCE" Caption Sky deep + Number distance (meters = distance / 20, formatted "1.2 km" at >= 1000); Lime tint "COINS" Caption Lime deep + Number coin count; 12dp centered Label secondary "+N coins" where N = coins collected with Icons.Rounded.MonetizationOn 14dp Sun deep.
- 20dp, ZoomyButton Accent full width with Icons.Rounded.PlayCircle "Continue" and a pill tag at its right inside: Lime fill, Label white "FREE" (rewarded ad mock; revive). 10dp SecondaryButton "Continue · 50 coins" (with Icons.Rounded.MonetizationOn 18dp Sun deep left of text). 10dp ZoomyButton Primary "Play again". 8dp centered TextButton "Home". Plus is reachable from Home and Shop only.

[garage]
- Push screen. Top row at status inset + 12dp: IconButton Icons.Rounded.ArrowBack (back), then H1 "Garage" (12dp gap). Under: Body secondary "5 of 8 unlocked".
- 12dp, thin progress track (10dp pill, Ink at 8%, Tomato fill 62%).
- 16dp, 2-column grid (gap 12dp) of 8 Cards (padding 16dp, height 200dp), each: centered CarSprite (height 92dp, idle 2dp bob), 12dp below Title name centered, 8dp below a full-width pill (height 32dp): owned and equipped = Lime tint "EQUIPPED" (Lime deep text); owned = Tomato tint "EQUIP" (Tomato deep text); locked = Locked grey pill with Icons.Rounded.MonetizationOn 14dp Sun deep and the price. Mock state: Pip equipped; Bumble, Minty, Blueberry, Grapey owned; Orangy 4,000; Pinky 6,000; Goldie locked with Icons.Rounded.WorkspacePremium 14dp and "PLUS" (card at alpha 70%). Tapping a pill in this phase only updates mock state: EQUIP -> becomes equipped (previous becomes EQUIP) with a Press spring pop.
- Scroll bottom padding 24dp + inset.

[shop]
- Push screen. Top row: IconButton Back, H1 "Shop", right-aligned StatChip Coins.
- 12dp: Plus banner Card (tappable -> Plus), Sun tint fill, no border, height 116dp: left Caption Sun deep "ZOOMY PLUS", H2 "No ads, double coins", 8dp CompactButton "Try free"; right a 72dp white-60% circle with Icons.Rounded.WorkspacePremium 40dp Sun deep.
- 24dp H2 "Coins", 12dp, 3 PackCards in a row (gap 12dp, width (screen-40-24)/3, height 150dp, padding 12dp, RoundedCornerShape(28), Surface fill + border): top Icons.Rounded.MonetizationOn 40dp Sun; Number "500"; Label secondary "coins"; bottom full-width pill 32dp Lime fill with Label white "$1.99". Packs: 500 / $1.99; 1,500 / $4.99 (middle: border 3dp Sun, with a Sun pill tag "POPULAR" Caption Ink overlapping its top edge); 4,000 / $9.99.
- 24dp H2 "Boosters", 12dp, 2 Cards (gap 12dp, padding 16dp, height 84dp): leading 52dp circle tile (Sky tint, icon Sky deep), Title, Label secondary, trailing CompactButton with Star 16dp and price. Rows: "Shield x3" / "Survive one crash" / 100; "Magnet x3" / "Pulls in nearby coins" / 150.
- 24dp, Card "Remove ads": leading 48dp Grape tint circle with Icons.Rounded.Block in Grape deep, Title "Remove ads", Label secondary "One-time purchase", trailing CompactButton "$3.99".
- Scroll bottom padding 24dp + inset.

[plus] (full-screen)
- Top-left IconButton Icons.Rounded.Close at status inset + 12dp.
- Hero: Tomato gradient (Tomato light -> Tomato) rounded rectangle (RoundedCornerShape(36)), width = screen - 40dp, height 220dp, 16dp below the close button; inside, centered: the Goldie CarSprite (height 110dp, top-down, crown on the roof) with a gentle 2dp bob, 1600ms sinusoidal, infinite; below it H1 white "Zoomy Plus"; Body white at 85% "Zoom without limits."; decorative: 6 tiny sparkles (4-point coins 10dp, white at 70%) at fixed positions around the cloud, each twinkling alpha 40% -> 100%, 1600ms, offset phases, infinite.
- 20dp, 5 benefit rows (gap 14dp): 28dp Lime circle with Icons.Rounded.Check 16dp white; 12dp; Title. Rows: "No ads, ever", "3 free boosters every day", "Double coins", "Exclusive cars", "Golden Goldie car".
- 24dp, two PlanCards in a column (gap 12dp), full width, height 76dp, RoundedCornerShape(28), padding 16dp, Surface fill + border (selected: 3dp Tomato border, Tomato tint at 40% fill, scale 1.02 with Bounce spring); left a 24dp circle radio (unselected 2dp Ink at 25% ring; selected Tomato fill with white Check 14dp); 12dp; column Title + Label secondary; right-aligned Title price.
   Yearly (selected by default): "Yearly", Label "Just $1.67 / month", "$19.99". A Lime pill tag overlapping the card's top edge, 12dp from right: Caption white "SAVE 44%".
   Monthly: "Monthly", Label "Billed every month", "$2.99".
- 20dp ZoomyButton Accent full width "Start 3-day free trial". 10dp Label tertiary centered "Then $19.99 per year. Cancel anytime." 4dp centered TextButton "Restore purchase".
- Content scrolls if needed; bottom padding 24dp + inset.

MOCK DATA (MockData.kt)
Coins 1,250; best score 12,480; boosters Shield 2, Magnet 1, Slow-mo 0; garage owned cars 1-5 with Pip equipped; shop, plans as written above.

PROJECT STRUCTURE (already exists)
game/ (Engine.kt, Traffic.kt, RoadView.kt, Effects.kt), ui/theme (Color.kt, Shape.kt, Motion.kt, Theme.kt; Type.kt exists), ui/components (one file per component incl. CarSprite.kt), ui/screens (one file per screen), navigation/ZoomyNavHost.kt, data/MockData.kt, MainActivity.kt.

DELIVERY CHECKLIST
- Builds with ./gradlew assembleDebug, no TODO placeholders.
- The game is fully playable: lane changes, traffic, coins, close-call combos, boosters, crash, pause, game over, revive and play again all work; frame rate stays smooth with up to 30 objects on screen.
- All 8 screens reachable: Splash -> Home; Play -> Game; Pause button; automatic Game Over; Garage and Shop from Home; Plus from Home and Shop.
- No color outside the token list. No dark backgrounds. No images. No navigation bar.
- Every animation listed exists with the stated duration/easing.
Output the full source code of every file.