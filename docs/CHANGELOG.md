# FastAnimation Version Changelog

## [0.1.2] — 2026-08-21

### Added
- **Deterministic Step API**: Added `FastAnimation.step(float deltaMs)` and `Animation.update(float deltaMs)` enabling 100% deterministic, fixed-rate timeline simulation for offline rendering and physics stepping.
- **Manual Heartbeat Mode**: Added `HeartbeatMode.MANUAL` to completely bypass asynchronous background daemon threads during offline frame-by-frame export.
- **FastGPU Vulkan Compute Particle Demo**: Added 100,000 particle Vulkan compute demo (`ParticleGPUDemo`) demonstrating genuine GLSL SPIR-V compute kernels for 3D matrices, orbits, and harmonic turbulence.

---

## [0.1.1] — 2026-08-19

### Changed
- **FastExecution Integration**: Migrated daemon heartbeat loops to `FastExecution` (`NATIVE_MM` and `NATIVE_VSYNC`), removing duplicate loop/scheduler code.
- **Timestamp Initialization**: Reset `lastTime` on engine wake-up to prevent delta spikes.

---

## [0.1.0] — 2026-04-30

### Added
- First public release of the FastAnimation timeline engine.
- Pure Java, zero-allocation animation heartbeat.
- Support for chained tweens and parallel timelines.
- Integrated Demo showing performance-oriented animation cycles.
- Full Blueprint standardization.
