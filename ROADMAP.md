# FastAnimation Roadmap 🗺️

**Vision:** To provide an ultra-fast, zero-allocation timeline and animation engine for Java, heavily leveraging native Windows timers for sub-millisecond precision and VSync perfection.

## 🟢 v0.1.0: Initial Release
- [x] **Zero-allocation timeline engine**: Foundation for all tweens.
- [x] **Chaining and Parallel execution**: Support for complex sequences.
- [x] **FastTween integration**: Seamless value interpolation.
- [x] **Standardized Maven/Batch build system**: BluePrint compliance.

## 🟢 v0.2.0: Optimization Phase & Native Hooking (Current)
- [x] **Battle of the Timers**: Implementation of the high-precision backend comparison.
- [x] **FastDWM native heartbeat integration**: "The Master Pulse" using `timeSetEvent` and DirectX VSync.
- [x] **O(N) HashSet Cleanup**: Optimized garbage collection handling for massive scale.
- [x] **10 Million Tween Scale Test**: Verified architecture stability under extreme load.

## 🟡 v0.5.0: Platform & Logic Expansion
- [ ] **Frame-skipping logic**: Handle high-load scenarios gracefully without slowing timeline.
- [ ] **Path-based animations**: Follow curves and complex splines.
- [ ] **CSS-like easing curves**: Customizable Bezier easing.

## 🔴 v1.0.0: Production Hardening
- [ ] **Timeline scrubbing and reverse playback**: Full timeline traversal control.
- [ ] **Full Stability Audit**: Long-run stress testing in production UI environments.

---
**Focus:** Performance is our USP. We optimize where Java stops.
