# Changelog

Notable changes of every release. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the versions follow
[Semantic Versioning](https://semver.org/spec/v2.0.0.html). Releases `2.0.0-alpha.*` built the
OpenGL rewrite step by step; `1.x` is the former JavaFX application.

## [Unreleased]

### Added

- This changelog.
- Continuous integration on GitHub Actions: every push runs the unit and integration tests on
  Linux with JDK 21, the OpenGL ones on Mesa's software renderer, and publishes the coverage on
  Codecov; badges of build and coverage in the README.
- Coverage of the integration tests, which exercise the OpenGL adapters.

### Fixed

- The README counts the 679 tests of release 2.1.0 (646 unit, 33 integration) instead of 573.
- The build carries the version in preparation instead of `2.0-SNAPSHOT`.
- The Gradle wrapper is executable on Linux and macOS.

## [2.1.0] - 2026-11-15

Maps, rings and a sky of stars.

### Added

- The Sun and the eight planets are wrapped in maps. The maps of the Sun, Venus, Jupiter, Saturn,
  Uranus and Neptune (Solar System Scope, CC BY 4.0) come with the application; the Earth (NASA
  Blue Marble from GIBS), Mercury and Mars (NASA Solar System Treks) are downloaded in the
  background at the first launch, with retries, and kept in `~/.astronomicalsystem/maps`. A body
  whose map is not available yet is drawn as a plain lit sphere.
- Launched bodies are dressed by their kind: protoplanets with the map of the Moon, asteroids and
  debris with the map of Vesta (both from NASA Solar System Treks).
- The Sun and the planets turn about their true poles at their true rates, from the rotational
  elements of the IAU.
- A sky of 4000 stars behind everything, generated from a fixed seed.
- Saturn's C ring, B ring, Cassini Division and A ring, each with its optical depth, lit by
  reflected or transmitted light, hidden behind the planet and dark in its shadow.
- Pictures of the Earth and of Saturn in the README; the other two are taken again with the maps.

## [2.0.0] - 2026-11-14

First release of the OpenGL simulator: everything of the `2.0.0-alpha` releases below.

### Changed

- The README describes the new application, its low-level graphics and the physics behind it,
  with pictures and references.

### Removed

- The JavaFX application of `1.x`, with its creator of custom bodies, comets, performance monitor,
  settings and saved games. Saves of `1.x` cannot be loaded.

## [2.0.0-alpha.11] - 2026-11-14

Impacts.

### Added

- Collisions are detected along the whole time step (swept spheres), so fast bodies no longer pass
  through a planet between two steps.
- Keys `1`, `2` and `3` launch an asteroid, a planetesimal or a protoplanet at the followed body.
- Impact outcomes after Leinhardt and Stewart: slow encounters merge, faster ones leave the largest
  remnant and debris fragments that conserve mass, momentum and centre of mass.
- A close-up camera that comes down to two radii of the followed body, with bodies drawn as spheres
  lit by the Sun and a limb-darkened Sun.
- Impact effects: a fireball sized by the energy of the impact and a plume of embers that fall back
  to the surface; time slows down by itself when the followed body is hit.
- Slower speeds of time, down to under a minute per second, kept smooth by shorter steps.

## [2.0.0-alpha.10] - 2026-11-14

Controls help, provenance, labels, following a body and predicted paths.

### Added

- A line of the head-up display lists the controls.
- The head-up display tells whether the initial positions come from JPL Horizons or from Keplerian
  elements.
- The bodies are named on screen.
- Keys `.` and `,` choose the body the camera follows, or the barycentre; the head-up display tells
  which.
- Every body shows its path over the next year, predicted by running a copy of the simulation.

## [2.0.0-alpha.9] - 2026-11-08

Time controls and a head-up display.

### Added

- Keys `+` and `-` double and halve the speed of time, the space bar pauses it.
- A head-up display with the date and the speed of time, written with a font baked by
  `stb_truetype`.

## [2.0.0-alpha.8] - 2026-11-07

Bodies drawn with a camera the user can turn and zoom.

### Added

- A perspective camera computed on the CPU in double precision; the bodies are drawn as discs with
  a colour and a minimum size for each kind.
- Dragging turns the camera, the wheel zooms, and the window can be resized.

## [2.0.0-alpha.7] - 2026-11-06

An OpenGL window with the real-time simulation.

### Added

- The `app` module, on LWJGL 3 and OpenGL 3.3 core: a window and a fixed-step loop that runs the
  simulation in real time, independent of the frame rate.
- The simulation starts from the positions of today.

## [2.0.0-alpha.6] - 2026-11-02

Offline ephemerides with a fallback.

### Added

- A catalogue of the Sun and the eight planets with Keplerian elements propagated to any date and
  shifted to the barycentre.
- Initial positions from JPL Horizons, or from the Keplerian elements when Horizons cannot be
  reached or answers something unreadable.

## [2.0.0-alpha.5] - 2026-11-01

A Horizons client over HTTP.

### Added

- Queries to the JPL Horizons API with a timeout, and errors that tell an unreachable service from
  an unreadable answer.

## [2.0.0-alpha.4] - 2026-10-29

The `ephemeris` module.

### Added

- A parser of Horizons responses into barycentric state vectors at a Julian date.

## [2.0.0-alpha.3] - 2026-10-27

Keplerian orbits.

### Added

- Orbital elements turned into state vectors, with Kepler's equation solved by Newton's method from
  Danby's starting value.

## [2.0.0-alpha.2] - 2026-10-27

Deterministic collisions.

### Added

- Overlapping bodies merge, conserving mass, momentum and volume.

## [2.0.0-alpha.1] - 2026-10-27

The physics core.

### Added

- The `core` module: immutable vectors, validated bodies in astronomical units, days and solar
  masses, Newtonian N-body gravity, the Velocity Verlet integrator and an observable simulation.

## [1.0.0] - 2026-02-21

The JavaFX application, kept in the history under the tags `v1.0.0` and `legacy-javafx`.

[Unreleased]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.1.0...develop
[2.1.0]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.0.0...2.1.0
[2.0.0]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.0.0-alpha.11...2.0.0
[2.0.0-alpha.11]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.0.0-alpha.10...2.0.0-alpha.11
[2.0.0-alpha.10]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.0.0-alpha.9...2.0.0-alpha.10
[2.0.0-alpha.9]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.0.0-alpha.8...2.0.0-alpha.9
[2.0.0-alpha.8]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.0.0-alpha.7...2.0.0-alpha.8
[2.0.0-alpha.7]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.0.0-alpha.6...2.0.0-alpha.7
[2.0.0-alpha.6]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.0.0-alpha.5...2.0.0-alpha.6
[2.0.0-alpha.5]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.0.0-alpha.4...2.0.0-alpha.5
[2.0.0-alpha.4]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.0.0-alpha.3...2.0.0-alpha.4
[2.0.0-alpha.3]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.0.0-alpha.2...2.0.0-alpha.3
[2.0.0-alpha.2]: https://github.com/D13GOOOO/AstronomicalSystem/compare/2.0.0-alpha.1...2.0.0-alpha.2
[2.0.0-alpha.1]: https://github.com/D13GOOOO/AstronomicalSystem/compare/legacy-javafx...2.0.0-alpha.1
[1.0.0]: https://github.com/D13GOOOO/AstronomicalSystem/releases/tag/v1.0.0
