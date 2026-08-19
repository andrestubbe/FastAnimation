# FastAnimation Version Changelog

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
