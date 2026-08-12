# Interactive 3D Solar System Simulator

A physics-based 3D Solar System simulation built from scratch using **Java** and **JavaFX**. The project combines a custom gravitational engine with deterministic orbital motion and an interactive "God Mode" sandbox.

![Solar System Preview](src/main/resources/project_screen.png)

## Overview & Features

* **N-Body Physics Engine**: Custom gravity calculations between all physical bodies with an iterative collision resolution system.
* **Mixed Motion Model**:
  * **Planets**: Fully dynamic, gravity-driven movement implementing `Kinematic`.
  * **Moons**: Host-centric parametric orbits for stable satellite motion (`OrbitingBody`).
  * **Stars**: Static, high-mass entities (`AnchoredBody`) that preserve their role even after collisions.
* **God Mode (Creator Panel)**: A slide-out HUD for spawning custom celestial bodies with adjustable mass, distance, and velocity.
* **Complete Session Management**: Full state serialization to disk using a dedicated `SessionPersistenceService`. It captures the physical simulation state, camera transforms, and pause state, restoring everything perfectly on load.
* **Realistic Visuals**:
  * Deep-space HUD styling with a minimal, non-intrusive aesthetic.
  * Procedurally generated Saturn rings (with strict mesh validation) and a skybox background.
  * Real-time orbit trails for visual feedback.

## Architecture & Modules

The project is built on a clean MVC architecture with strong adherence to SOLID and Liskov Substitution principles:

* **Model (`astronomicalsystem.model`)**: Handles N-body physics, iterative perfectly inelastic collisions (conserving momentum), and structural interfaces (`Kinematic`, `OrbitingBody`, `AnchoredBody`).
* **View & Controller (`astronomicalsystem.view` / `controller`)**: Driven by `SimulationRenderer` and `InputHandler`. Implements strict parameter validations (e.g., camera zoom clamping, ring mesh integrity) and uses the Observer pattern for state updates.
* **Session Services (`astronomicalsystem.session`)**: Separated persistence layer (`SessionPersistenceService`, `SessionState`, `CameraState`) ensuring the main simulation loop remains lightweight. Backward compatible with legacy physical-only payloads.
* **Configuration (`astronomicalsystem.config`)**: Centralized `SimulationSettings` acts as the single source of truth for camera limits, render scales, visual radiuses, and physical constants.

## Testing & Quality Assurance

The project is covered by a JUnit Platform test suite that guarantees structural integrity and prevents regressions:
* **Physics & Kinematics**: Covered by `MoonTest` (Liskov-safe orbits) and `AstronomicalSystemCollisionTest` (iterative resolution and anchored body preservation).
* **Persistence**: Covered by `SessionPersistenceServiceTest` and `AstronomicalSystemPersistenceTest` for full state round-tripping.
* **UI & Validation**: Covered by `InputHandlerTest` and `SaturnRingTest`.

Run the suite using Gradle:
./gradlew test

## Performance Profiling

A built-in `SimulationPerformanceMonitor` is available to track frame times and rendering costs. This is an opt-in feature designed for debugging and optimizing large simulations. 
To enable lightweight profiling, run the application with the following system property:
-Dastronomicalsystem.profile=true

## How to Use It

| Action | Input |
| :--- | :--- |
| **Rotate the view** | Left Click + Drag |
| **Move the camera** | Right Click + Drag or Arrow Keys |
| **Zoom in/out** | Scroll Wheel or W / S |
| **System Menu** | P key (Resume, Save, Load, Quit) |
| **God Mode HUD** | C key (Spawn custom planets) |
| **Inspect a planet** | Left Click on it |
| **Play/Pause** | Spacebar |
| **Reset camera** | R key |

## Tech Stack & Execution

* **Language**: Java 21+
* **Framework**: JavaFX 21
* **Build Tool**: Gradle

### Getting Started

1. Ensure you have **JDK 21** or newer installed.
2. Clone the repository.
3. Build and test the project: `./gradlew build`
4. Run the main class: `astronomicalsystem.Launcher` (or `astronomicalsystem.SolarSystem3D` depending on your IDE setup).

---

## License & Copyright

**Copyright © 2026 [D13GOOOO]. All Rights Reserved.**

This is a **proprietary** project.

* You are not allowed to copy, distribute, or modify this code for commercial use without explicit written permission from the author.
* This code is shared here for demonstration and portfolio purposes.

**Contact**: diego.guerini.it@gmail.com