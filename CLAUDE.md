# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project Overview

ScoreIt is an Android app for counting game points, supporting four game types: Universal, Tarot,
Belote, and Coinche. Written in Kotlin, targeting Android API 21+.

## Build Commands

```bash
./gradlew build                    # Build all modules
./gradlew test                     # Run all unit tests
./gradlew :data:test               # Run tests for data module only
./gradlew :data:test --tests "*.BeloteSolverTest"  # Run a single test class
./gradlew assembleDebug            # Build debug APK
./gradlew assembleRelease          # Build release APK
./gradlew clean                    # Clean build outputs
```

Tests are JUnit 4, located only in the `data` module under `data/src/test/`. They cover the Solver
classes (scoring logic for each game type).

## Module Architecture

Four Gradle modules with strict dependency direction:

```
app → data → core
app → cache → core
         cache → data (cache implements data's repository interfaces)
```

- **app** — Android UI layer: Activities, ViewModels, adapters, DI wiring. Package:
  `com.sbgapps.scoreit`
- **data** — Business logic: game models (sealed class hierarchy), Solver scoring engines, UseCases,
  repository interfaces. Package: `com.sbgapps.scoreit.data`
- **cache** — Persistence: repository implementations, file-based game storage via Moshi JSON
  serialization, SharedPreferences. Package: `com.sbgapps.scoreit.cache`
- **core** — Shared base classes and extensions: `BaseViewModel` (State/Effect pattern),
  `BaseActivity`, Kotlin extension functions. Package: `com.sbgapps.scoreit.core`

## Key Patterns

**Game type hierarchy** — `Game` is a sealed class with four data class subtypes (`UniversalGame`,
`TarotGame`, `BeloteGame`, `CoincheGame`). Each has its own `Lap` type and `Solver`. Adding a game
type means adding entries across model, solver, use case, and UI layers.

**MVVM with State/Effect** — `BaseViewModel` in core uses `MutableStateFlow<State>` for UI state and
`MutableSharedFlow<Effect>` for one-time events. ViewModels call `setState()` and `sendEffect()`.
Activities observe via `observeStates()` and `observeEffects()`.

**Solver strategy** — Each game type has a dedicated Solver class (`UniversalSolver`, `TarotSolver`,
`BeloteSolver`, `CoincheSolver`) that computes scores from laps. These are pure logic with no
Android dependencies — the only tested classes.

**DI with Koin** — Three module definitions: `uiModule` (app), `dataModule` (data), `cacheModule` (
cache). Wired together in `ScoreItApp`.

**Moshi polymorphic serialization** — Games are serialized to JSON files using
`PolymorphicJsonAdapterFactory` with a `"type"` discriminator field.

## Tech Stack

- Kotlin 1.9, Coroutines 1.7, Gradle 8.3 (Kotlin DSL)
- AndroidX (lifecycle, fragment, appcompat), Material Design
- Koin 2.1 (DI), Moshi 1.15 (JSON, with KSP codegen), Timber (logging)
- View Binding (no Compose)
- JUnit 4 + MockK for tests

## Version Management

Version is defined in `app/build.gradle.kts` via `versionMajor`, `versionMinor`, `versionPatch`
variables. Version code is computed as `major * 100 + minor * 10 + patch`.

## Dependency Versions

All dependency versions are centralized in `gradle/libs.versions.toml` and referenced as `libs.*` in
build files.
