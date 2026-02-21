# Interactive 3D Solar System Simulator (V2)

A physics-based 3D Solar System simulation built from scratch using **Java** and **JavaFX**. This project combines a custom N-Body physics engine with accurate orbital mechanics and a dynamic "God Mode" sandbox to create a professional and interactive space environment.

![Solar System Preview](src/main/resources/project_screen.png)

## What's inside

* **N-Body Physics Engine**: A custom-coded engine that calculates real-time gravitational forces between all objects.
* **Mixed Kinematic Model**:
  * **Planets**: Fully dynamic gravity-based movement.
  * **Moons**: Parametric deterministic orbits to ensure infinite stability around moving hosts, eliminating numerical drift.
* **God Mode (Creator Panel)**: A slide-out HUD that allows the real-time injection of custom celestial bodies with adjustable mass, distance, and velocity.
* **Quick Save/Load System**: Full state serialization to disk. Save your orbital configuration and resume it at any time.
* **Realistic Visuals**:
  * Unified **Deep-Space HUD** design (NASA-inspired) with a minimalist, non-intrusive aesthetic.
  * Procedurally generated Saturn's rings and immersive skybox.
  * Real-time orbit trails to visualize complex gravitational interactions.
* **Collision Physics**: Conservation of momentum applied when bodies collide and merge.

## How to use it

| Goal | Input |
| :--- | :--- |
| **Rotate the view** | Left Click + Drag |
| **Move the camera** | Right Click + Drag (or Arrow Keys) |
| **Zoom in/out** | Scroll Wheel (or W / S keys) |
| **System Menu** | **P** Key (Resume, Save, Load, Quit) |
| **God Mode HUD** | **C** Key (Spawn custom planets) |
| **Inspect a planet** | Left Click on it (Opens Telemetry) |
| **Play/Pause** | Spacebar |
| **Reset camera** | R key |

## Tech Stack

* **Language**: Java 17+
* **Framework**: JavaFX (3D API)
* **Core Concepts**: MVC Architecture, Observer Pattern, Binary Serialization, and N-Body Gravity Algorithms.

## Getting Started

1. Ensure you have **JDK 17** (or newer) installed.
2. Configure the **JavaFX SDK** in your IDE or project structure.
3. Clone the repository.
4. Run the main class: `SolarSystem3D.java`.

---

## License & Copyright

**Copyright © 2026 [D13GOOOO]. All Rights Reserved.**

This is a **proprietary** project.

* You are not allowed to copy, distribute, or modify this code for commercial use without explicit written permission from the author.
* This code is shared here for demonstration and portfolio purposes.

**Contact**: [diego.guerini.it@gmail.com]