# Kairn 🏔️
Kairn is a community mobile app to create, share, and follow hiking and walking paths

## Description
- Discover local hikes on a 3D map
- Start a route and track progress in real time with GPS.
- Browse a dedicated hike catalog with list and detailed route pages
- Create and edit routes directly in the app
- Create groups, add friends, chat, and organize meetup hikes
- Earn XP based on route difficulty, progress, and completion time
- Climb leaderboards at city, region, country, and global levels

## Target Platforms
- `v1`: Native Android (Kotlin + Jetpack Compose).
- `v2`: Native iOS (Swift/SwiftUI)
- `v3`: React web app (PWA) with 3D rendering via Three.js

## Feature Architecture
- `Home` tab
  - Regional 3D map with visible hikes
  - Quick route details + `Start` action
  - Quick route cards with: hike name, estimated time, difficulty, distance (km), elevation gain, and recommended level
  - Live GPS route tracking
- `Catalog` tab
  - Scrollable hike list with filter/sort (near me, difficulty, distance, duration, elevation, level)
  - Card-based browsing for all available hikes
  - Detailed hike page with full description, route breakdown, points of interest, creator profile, creator photos, community photos, and comments
- `Editor` tab
  - Hike creation and editing
  - GPS points and metadata editing (distance, elevation, difficulty)
- `Social` tab
  - Friends, groups, and chat
  - Hike sharing and meetup planning
- `Account` tab
  - Profile (username, avatar, level, XP)
  - Activity history and progression summary

## Offline + GPS
- Local cache for routes and profile with deferred sync
- Offline action queue (likes, edits, messages)
- Automatic sync resume when network returns
- GPS tracking with `FusedLocationProviderClient` (Android)
- Signal loss tolerance with interpolation and recovery

## 3D Map + XP Progression
- 3D route visualization with terrain/perspective
- During an active route, progress is computed from GPS position along the route polyline
- XP logic example (to tune):
  - `xp_base = distance_km * 10`
  - `xp_difficulty = xp_base * difficulty_coefficient`
  - `xp_time = bonus when time objective is met`
  - `xp_total = xp_difficulty + xp_time`


## Stack and Libraries 🛠️
Android:
- Kotlin, Jetpack Compose, Navigation Compose.
- `MapLibre` (2.5D/3D map rendering) with OSM data.
- `Google Play Services Location` for GPS.
- `Room` + `WorkManager` for offline-first sync.
- `Hilt` for dependency injection.
- `Kotlinx Serialization` + `Ktor/OkHttp`.
- `Supabase Kotlin` (Auth, PostgREST, Realtime, Storage).
- `Timber` for logging

Backend:
- Supabase (Auth, Realtime, Storage)

## Database

- `accounts`: user profile, first name, last name, username, avatar, age, level, total XP, city/region/country
- `paths`: hikes/routes, author, geometry, difficulty, metadata
- `groups`: community groups, description, visibility

### Identity and user profile
- `profiles`: public user profile linked to `auth.users` (`id`, `username`, `avatar_url`, `bio`, `city_id`, `region_id`, `country_code`)
- `user_stats`: progression and counters (`user_id`, `level`, `total_xp`, `total_distance_m`, `completed_runs_count`)
- `user_settings`: app preferences (`user_id`, `language`, `units`, `privacy_flags`, `notification_flags`)
- `user_devices`: push tokens and device metadata (`id`, `user_id`, `platform`, `push_token`, `last_seen_at`)

### Geography and hike catalog
- `countries`: country catalog (`code`, `name`)
- `regions`: region catalog (`id`, `country_code`, `name`)
- `cities`: city catalog for local ranking (`id`, `region_id`, `name`, `center_geog`)
- `hikes`: main hike entities (`id`, `creator_id`, `title`, `description`, `difficulty`, `estimated_duration_min`, `distance_m`, `elevation_gain_m`, `recommended_level`, `status`)
- `hike_geometries`: route geometry (`hike_id`, `route_line`, `bbox`, `start_point`, `end_point`)
- `hike_points`: ordered editable points (`id`, `hike_id`, `seq`, `point_geog`, `elevation_m`)
- `hike_pois`: points of interest (`id`, `hike_id`, `name`, `description`, `poi_type`, `point_geog`)
- `hike_tags`: tag dictionary (`id`, `slug`, `label`)
- `hike_tag_links`: many-to-many links (`hike_id`, `tag_id`)

### Content and community around hikes
- `hike_media`: creator/community photos (`id`, `hike_id`, `uploader_id`, `storage_path`, `media_type`, `is_cover`)
- `hike_comments`: comments and replies (`id`, `hike_id`, `author_id`, `body`, `parent_comment_id`)
- `hike_ratings`: score and short review (`id`, `hike_id`, `user_id`, `rating`, `note`)
- `hike_bookmarks`: saved hikes (`user_id`, `hike_id`, `created_at`)

### Social and chat
- `friendships`: friend graph and requests (`id`, `requester_id`, `addressee_id`, `status`)
- `groups`: user-created groups (`id`, `owner_id`, `name`, `description`, `visibility`)
- `group_members`: group membership and roles (`group_id`, `user_id`, `role`)
- `group_invites`: invitation flow (`id`, `group_id`, `inviter_id`, `invitee_id`, `status`)
- `conversations`: chat channels (`id`, `type`, `group_id`)
- `conversation_members`: channel membership (`conversation_id`, `user_id`, `last_read_message_id`)
- `messages`: chat messages (`id`, `conversation_id`, `sender_id`, `body`, `message_type`)
- `message_attachments`: chat media (`id`, `message_id`, `storage_path`, `mime_type`)
- 
### Runs, GPS tracking, XP, and leaderboards
- `hike_runs`: a started/completed hike session (`id`, `hike_id`, `user_id`, `started_at`, `ended_at`, `status`, `duration_sec`)
- `hike_run_points`: sampled GPS points during a run (`id`, `run_id`, `seq`, `point_geog`, `recorded_at`, `accuracy_m`)
- `hike_run_summaries`: computed run metrics (`run_id`, `distance_m`, `elevation_gain_m`, `avg_speed_mps`, `completion_ratio`)
- `xp_events`: event-based XP ledger (`id`, `user_id`, `source_type`, `source_id`, `xp_delta`, `breakdown_json`)
- `level_rules`: XP thresholds (`level`, `xp_required_total`)
- `leaderboard_snapshots`: ranking snapshots (`id`, `scope_type`, `scope_id`, `period`, `snapshot_at`)
- `leaderboard_entries`: rank lines (`snapshot_id`, `user_id`, `rank`, `score`)

### Sync and moderation
- `sync_checkpoints`: per-user sync anchor (`user_id`, `entity`, `last_synced_at`, `last_version`)
- `sync_events`: optional delta stream (`id`, `entity`, `entity_id`, `action`, `version`)
- `reports`: user reports (`id`, `reporter_id`, `target_type`, `target_id`, `reason`)
- `moderation_actions`: admin audit trail (`id`, `admin_id`, `target_type`, `target_id`, `action`, `note`)

## Roadmap️ 🗺️
### Phase 0 - Foundations
- [x] Define Supabase data model (`accounts`, `paths`, `groups`)
- [x] Implement Auth and RLS security rules
- [x] Set up Android architecture (`ui`, `data`, `domain`)
- [x] Deliver login/sign-up flow

### Phase 1 - Navigation
- [ ] Display a regional 3D map with hikes
- [ ] Show route quick details on map cards (name, time, difficulty, distance, elevation, level)
- [ ] Start a route and track GPS progression
- [ ] Ensure core flow works offline and syncs later
- [x] Build Home map screen with OSM map rendering and real GPS location centering
- [x] Add Explore catalog with filters, hike cards, and detail navigation
- [x] Implement hike detail screen with shared element transition and CTA

### Phase 2 - Hikes Editor
- [ ] Build hike create/edit flow
- [ ] Edit GPS points and route metadata
- [ ] Enable user route publishing

### Phase 3 - Social
- [ ] Add friend system and group creation
- [ ] Add group chat and hike sharing
- [ ] Add comments and creator/community photos on hike detail pages
- [x] Add social navigation shell (Chat tab routing placeholder)

### Phase 4 - XP + Leaderboards
- [ ] Finalize XP formula by hike difficulty, completion and time
- [ ] Add level progression rules and thresholds
- [ ] Launch leaderboards (city/region/country/global)
- [ ] Add anti-cheat validation for route run consistency

### Phase 5 - Production Readiness
- [ ] Add monitoring, crash reporting, and product analytics
- [ ] Add tests and CI/CD pipeline
- [ ] Optimize battery and GPS performance
- [ ] Release a stable private beta build
- [x] Set up Hilt dependency injection and Supabase modules (Auth, PostgREST, Realtime, Storage)
- [x] Add baseline Android/JUnit and instrumented test scaffolding

### Phase 6 - Platform Expansion (Post-MVP)
- [ ] Start native iOS app (SwiftUI)
- [ ] Build React PWA with Three.js

## How to Start 🚀
### Prerequisites
- Android Studio (latest stable)
- JDK 17
- Android SDK installed (via Android Studio)

### 1. Clone the repository
```bash
git clone https://github.com/B0TAstro/Kairn.git
cd Kairn
```

### 2. Pull latest changes
```bash
git pull origin main
```

### 3. Open and sync the project
1. Open Android Studio
2. Click `Open` and select the `Kairn` folder
3. Wait for Gradle sync to finish

### 4. Build and run
```bash
./gradlew assembleDebug
```

## License

This project is licensed under the [MIT License](./LICENSE)
