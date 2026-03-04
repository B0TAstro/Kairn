# Kairn

Kairn is a community mobile app to create, share, and follow hiking and walking paths.

## Product Vision

- Discover local routes on a regional 3D map.
- Start a route and track progress in real time with GPS.
- Earn XP based on route difficulty, progress, and completion time.
- Climb leaderboards at city, region, country, and global levels.
- Create groups, add friends, chat, and organize meetup hikes.
- Create and edit routes directly in the app.

## Target Platforms

- `v1`: Native Android (Kotlin + Jetpack Compose).
- `v2`: Native iOS (Swift/SwiftUI) if product validation is strong.
- `v3`: React web app as a PWA with 3D rendering via Three.js.

## MVP Feature Architecture

- `Home` tab
  - Regional 3D map with visible routes.
  - Route details + `Start` action.
  - Live GPS route tracking.
- `Account` tab
  - Username, level, XP, activity history.
  - Local and global rankings.
- `Social` tab
  - Friends, groups, chat.
  - Route sharing and meetup planning.
- `Editor` tab
  - Route creation/editing.
  - GPS points and metadata editing (distance, elevation, difficulty).

## Database (Supabase)

Minimum tables:

- `accounts`: user profile, level, total XP, city/region/country.
- `paths`: routes, author, geometry, difficulty, metadata.
- `groups`: community groups, description, visibility.

Recommended product tables:

- `path_points`: ordered GPS points for each route.
- `group_members`: group membership and roles.
- `friendships`: friend relationships.
- `messages`: chat messages.
- `path_runs`: route sessions, progress, duration, earned XP.
- `leaderboard_snapshots`: precomputed ranking snapshots by area.

## Offline + GPS

- Local cache for routes and profile with deferred sync.
- Offline action queue (likes, edits, messages).
- Automatic sync resume when network returns.
- GPS tracking with `FusedLocationProviderClient` (Android).
- Signal loss tolerance with interpolation and recovery.

## 3D Map + XP Progression

- 3D route visualization with terrain/perspective.
- During an active route, progress is computed from GPS position along the route polyline.
- XP logic example (to tune):
  - `xp_base = distance_km * 10`
  - `xp_difficulty = xp_base * difficulty_coefficient`
  - `xp_time = bonus when time objective is met`
  - `xp_total = xp_difficulty + xp_time`

## Recommended Stack and Libraries

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

## Roadmap Checklist

### Phase 0 - Foundations (Week 1-2)

- [ ] Define Supabase data model (`accounts`, `paths`, `groups`).
- [ ] Implement Auth and RLS security rules.
- [ ] Set up Android architecture (`ui`, `data`, `domain`).
- [ ] Add offline-first base (`Room` + `WorkManager`).
- [ ] Deliver login/sign-up flow.
- [ ] Persist user profile locally and remotely.
- [ ] Validate baseline sync behavior.

### Phase 1 - Navigation MVP (Week 3-6)

- [ ] Display a regional 3D map with routes.
- [ ] Show route details (distance, difficulty, elevation, duration).
- [ ] Start a route and track GPS progression.
- [ ] Compute and assign XP at route completion.
- [ ] Ensure core flow works offline and syncs later.

### Phase 2 - Social and Engagement (Week 7-10)

- [ ] Add friend system and group creation.
- [ ] Add group chat and route sharing.
- [ ] Launch leaderboards (city/region/country/global).
- [ ] Add route history and level progression.

### Phase 3 - Route Editor (Week 11-13)

- [ ] Build route create/edit tab.
- [ ] Edit GPS points and route metadata.
- [ ] Add basic moderation flow (report/review).
- [ ] Enable user route publishing.

### Phase 4 - Production Readiness (Week 14-16)

- [ ] Add monitoring, crash reporting, product analytics.
- [ ] Add instrumentation tests and CI/CD pipeline.
- [ ] Optimize battery and GPS performance.
- [ ] Add anti-cheat server validation for XP consistency.
- [ ] Release a stable private beta build.

### Phase 5 - Platform Expansion (Post-MVP)

- [ ] Start native iOS app (SwiftUI).
- [ ] Build React PWA with Three.js.
- [ ] Align progression model across Android/iOS/Web.

## License

This project is licensed under the MIT License. See [LICENSE](./LICENSE).

## Immediate Next Steps

1. Stabilize the Android app foundation (navigation, auth, local data model).
2. Integrate Supabase (Auth + base tables + RLS policies).
3. Deliver a playable MVP (`Home + Start route + XP`).
4. Add social features and rankings.
5. Evaluate iOS extension, then React/Three.js PWA.
