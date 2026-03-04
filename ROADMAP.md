# Kairn Roadmap

## Phase 0 - Foundations (Week 1-2)

- Define Supabase data model (accounts, paths, groups).
- Implement Auth and RLS security policies.
- Set up Android architecture (UI, data, domain).
- Build offline-first foundations (Room + WorkManager).

Done when:

- Login/sign-up are working.
- User profile is persisted locally and remotely.
- Baseline sync flow is operational.

## Phase 1 - Navigation MVP (Week 3-6)

- Display a regional 3D map with routes.
- Show route details (distance, difficulty, elevation, duration).
- Start a route and track progression with GPS.
- Compute and grant XP at the end of a route session.

Done when:

- A user can complete a route and earn XP.
- Core data remains usable offline and syncs correctly.

## Phase 2 - Social and Engagement (Week 7-10)

- Add friends and group creation.
- Add group chat and route sharing.
- Launch city/region/country/global leaderboards.
- Add route history and level progression.

Done when:

- Full community loop works (invite, share, hike together).

## Phase 3 - Route Editing (Week 11-13)

- Build route creator/editor tab.
- Edit GPS points and route metadata.
- Add simple moderation (reporting, admin validation).

Done when:

- A user can create and publish a usable route.

## Phase 4 - Production Readiness (Week 14-16)

- Add monitoring, crash reporting, and product analytics.
- Add instrumentation tests and CI/CD pipeline.
- Optimize battery and GPS performance.
- Strengthen security (server-side validation, XP anti-cheat).

Done when:

- Stable build is ready for private beta.

## Phase 5 - Platform Expansion (Post-MVP)

- Build native iOS app in SwiftUI.
- Build React PWA with Three.js web experience.
- Align progression model across all platforms.

Done when:

- Cohesive cross-platform experience (Android/iOS/Web).

## Main Risks and Mitigations

- Unstable GPS: smooth traces and use progression tolerance.
- Complex offline sync: enforce conflict strategy (timestamps + merge rules).
- 3D performance cost: adapt detail level to device capabilities.
- XP cheating: validate distance/time consistency server-side.
