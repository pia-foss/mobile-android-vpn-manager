# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android VPN Manager is a modular Android SDK/library (AAR) that provides VPN capabilities for Android clients. It supports OpenVPN and WireGuard protocols and is published to GitHub Packages Maven repository as `com.kape.android:vpnmanager`.

## Build Commands

```bash
# Build release AAR
./gradlew clean vpnmanager:assemble

# Run all tests
./gradlew test --parallel

# Run tests for a single module
./gradlew :vpnmanager:test

# Run a single test class
./gradlew test --tests="*ClassName"

# Lint check
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat

# Publish to local Maven
./gradlew publishMavenPublicationToMavenLocal
```

**Build outputs:**
- AAR: `vpnmanager/build/outputs/aar/`
- Test app APK: `testapp/build/outputs/apk/`

**macOS requirement:** `flock` and `sha256sum` must be installed (`brew install flock coreutils`).

## Module Architecture

```
:vpnmanager                         # Main aggregator + implementation
├── :vpnmanager:vpnmanagerapi       # Public API interfaces and data models
├── :vpnmanager:targetprovider      # Optimal server selection (latency-based)
├── :vpnmanager:vpnservicemanager   # Android OS VPN service management
│   └── :vpnservicemanager:vpnprotocol            # Protocol abstraction layer
│       ├── :vpnprotocol:openvpn                  # Pure Kotlin OpenVPN impl
│       └── :vpnprotocol:wireguard                # WireGuard (Go + CMake native)
:testapp                            # Example Android application
```

## Key Architecture Patterns

**Clean Architecture layers** (all modules follow this):
`Presenters → Controllers → Use Cases → Data Sources / Externals`

**Entry points:**
- `VPNManagerBuilder` — Builder for initializing the SDK; client provides `Context`, permissions dependency, debug logging, and byte count callback
- `VPNManagerAPI` — Main public interface; all client interaction goes through this

**State management:**
- Single source of truth via `Cache` datasource pattern (see `data/models/State.kt`)
- `VPNManagerConnectionStatus` is a sealed class covering: `Connecting`, `Connected`, `Disconnecting`, `Disconnected`, `Error`, `Reconnecting`, and `Paused`

**Async model:** Coroutines throughout; client-facing callbacks use `kotlin.Result<T>`. Two coroutine contexts exist — a module-internal context and a client-callback context.

**Server iteration:** `StartConnectionController` iterates through the server list with fallback logic; `TargetProvider` runs latency checks to select the optimal server before connecting.

**Protocol selection:** The `vpnprotocol` layer is protocol-agnostic; the concrete OpenVPN/WireGuard implementations are injected. WireGuard uses a native Go library built via CMake.

## Important Models

| File | Purpose |
|---|---|
| `vpnmanagerapi/.../VPNManagerAPI.kt` | Public interface |
| `vpnmanagerapi/.../VPNManagerConnectionStatus.kt` | Connection state machine |
| `vpnmanager/.../data/models/ServerList.kt` | Server definitions (IP, port, transport, ciphers, DNS) |
| `vpnmanager/.../data/models/State.kt` | Internal state (single source of truth) |

## Toolchain

- Kotlin 2.2.0, AGP 8.11.1, Java 17
- Min SDK 21, Target/Compile SDK 34
- Ktor 3.2.1 (HTTP), Kotlin Serialization 1.9.0, Spongy Castle (crypto)
- Tests: JUnit 4, Robolectric 4.10.3, MockK 1.13.9, kotlinx-coroutines-test
- Ktlint 11.5.1 with trailing commas enabled (`.editorconfig`)

## CI/CD

Four GitHub Actions workflows in `.github/workflows/`:
- `lint-android.yaml` — ktlintCheck
- `test-android.yaml` — `./gradlew test`
- `build-android.yaml` — builds APK and AAR
- `push.yaml` — orchestrates all three on push

## Publishing

Artifacts publish to `https://maven.pkg.github.com/pia-foss/mobile-android-vpn-manager/`. Requires environment variables `GITHUB_USERNAME` and `GITHUB_TOKEN`.
