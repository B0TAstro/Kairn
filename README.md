# Kairn 🏔️

Kairn is a community mobile app to create, share, and follow hiking and walking paths.

## Product Vision 🌍

- Discover local hikes on a 3D map.
- Browse a dedicated hike catalog with list and detailed route pages.
- Start a route and track progress in real time with GPS.
- Create and edit routes directly in the app.
- Create groups, add friends, chat, and organize meetup hikes.
- Earn XP based on route difficulty, progress, and completion time.
- Climb leaderboards at city, region, country, and global levels.

## Target Platforms 📱

- `v1`: Native Android (Kotlin + Jetpack Compose).
- `v2`: Native iOS (Swift/SwiftUI).
- `v3`: React web app (PWA) with 3D rendering via Three.js.

## MVP Feature Architecture 🧭

- `Home` tab 🗺️
  - Regional 3D map with visible hikes.
  - Quick route cards with: hike name, estimated time, difficulty, distance (km), elevation gain, and recommended level.
  - Quick route details + `Start` action.
  - Live GPS route tracking.
- `Catalog` tab 📚
  - Scrollable hike list with filter/sort (near me, difficulty, distance, duration, elevation, level).
  - Card-based browsing for all available hikes.
  - Detailed hike page with full description, route breakdown, points of interest, creator profile, creator photos, community photos, and comments.
- `Editor` tab ✍️
  - Hike creation and editing.
  - GPS points and metadata editing (distance, elevation, difficulty).
- `Social` tab 👥
  - Friends, groups, and chat.
  - Hike sharing and meetup planning.
- `Account` tab 👤
  - Profile (username, avatar, level, XP).
  - Activity history and progression summary.

## Database (Supabase) 🗄️

Minimum tables:

- `accounts`: user profile, first name, last name, username, avatar, age, level, total XP, city/region/country.
- `paths`: hikes/routes, author, geometry, difficulty, metadata.
- `groups`: community groups, description, visibility.

Recommended product tables:

- `path_points`: ordered GPS points for each route.
- `path_pois`: points of interest linked to a route.
- `group_members`: group membership and roles.
- `friendships`: friend relationships.
- `messages`: chat messages.
- `path_comments`: user comments and feedback for routes.
- `path_media`: route photos (creator and community uploads).
- `path_runs`: route sessions, progress, duration, earned XP.
- `leaderboard_snapshots`: precomputed ranking snapshots by area.

## Offline + GPS 📡

- Local cache for routes and profile with deferred sync.
- Offline action queue (likes, edits, messages).
- Automatic sync resume when network returns.
- GPS tracking with `FusedLocationProviderClient` (Android).
- Signal loss tolerance with interpolation and recovery.

## 3D Map + XP Progression 🧠

- 3D route visualization with terrain/perspective.
- During an active route, progress is computed from GPS position along the route polyline.
- XP logic example (to tune):
  - `xp_base = distance_km * 10`
  - `xp_difficulty = xp_base * difficulty_coefficient`
  - `xp_time = bonus when time objective is met`
  - `xp_total = xp_difficulty + xp_time`

## Recommended Stack and Libraries 🛠️

Android:

- Kotlin, Jetpack Compose, Navigation Compose.
- `MapLibre` (2.5D/3D map rendering) with OSM data.
- `Google Play Services Location` for GPS.
- `Room` + `WorkManager` for offline-first sync.
- `Hilt` for dependency injection.
- `Kotlinx Serialization` + `Ktor/OkHttp`.
- `Supabase Kotlin` (Auth, PostgREST, Realtime, Storage).
- `Timber` for logging.

Backend:

- Supabase (Postgres, Auth, Realtime, Storage, Edge Functions).

Web PWA (future phase):

- React + TypeScript.
- Three.js for 3D visualization.
- MapLibre GL JS (map layer) + 3D integration.
- TanStack Query for cache/sync.
- Service Worker for offline support.

iOS (future phase):

- SwiftUI.
- MapLibre iOS (or equivalent based on product constraints).
- CoreLocation + local offline mode.

## Roadmap Checklist ✅

### Phase 0 - Foundations 🧱

- [ ] Define Supabase data model (`accounts`, `paths`, `groups`).
- [ ] Implement Auth and RLS security rules.
- [ ] Set up Android architecture (`ui`, `data`, `domain`).
- [ ] Deliver login/sign-up flow.
- [ ] Validate baseline sync behavior.

### Phase 1 - Navigation (Home tab) 🗺️

- [ ] Display a regional 3D map with hikes.
- [ ] Show route quick details on map cards (name, time, difficulty, distance, elevation, level).
- [ ] Start a route and track GPS progression.
- [ ] Ensure core flow works offline and syncs later.

### Phase 2 - Hikes Editor (Editor tab) ✍️

- [ ] Build hike create/edit flow.
- [ ] Edit GPS points and route metadata.
- [ ] Enable user route publishing.

### Phase 3 - Social (Social tab) 👥

- [ ] Add friend system and group creation.
- [ ] Add group chat and hike sharing.
- [ ] Add comments and creator/community photos on hike detail pages.

### Phase 4 - XP + Leaderboards 🏆

- [ ] Finalize XP formula by hike difficulty, completion, and time.
- [ ] Add level progression rules and thresholds.
- [ ] Launch leaderboards (city/region/country/global).
- [ ] Add anti-cheat validation for route run consistency.

### Phase 5 - Production Readiness 🚀

- [ ] Add monitoring, crash reporting, and product analytics.
- [ ] Add instrumentation tests and CI/CD pipeline.
- [ ] Optimize battery and GPS performance.
- [ ] Release a stable private beta build.

### Phase 6 - Platform Expansion (Post-MVP) 🌐

- [ ] Start native iOS app (SwiftUI).
- [ ] Build React PWA with Three.js.

## License 📄

This project is licensed under the [MIT License](./LICENSE).
