# FastTerminal Roadmap 🗺️

**Vision:** To provide the fastest possible native primitives for terminal rendering by aggressively bypassing bottlenecks in standard Java.

## 🟢 v0.1.0: Initial Release (Current)
- [x] **Core Native Engine**: Basic JNI implementation.
- [x] **Blueprint Standards**: README, Reference, and Philosophy integration.
- [x] **Rendering**: Double-Buffering, Diff-Rendering, Dirty-Rectangles, Partial Flush
- [x] **Color**: 24-bit Gradients (linear, vertical, diagonal), Paletten, Themes, Alpha-Compositing
- [x] **System**: Native Win32 JNI Resize-Events & swapchain `resize()`
- [ ] **Basic Performance Suite**: Initial benchmarks vs standard Java

## 🟡 v0.2.0: Input & Layout
- [ ] **Input**: Maus-Events, Key-Events, Modifier-Keys, Drag-Tracking
- [ ] **Layout**: Flex-Layout, Grid-Layout, Anchors, Auto-Resize
- [ ] **Widgets**: Buttons, Panels, Textbox, Scrollview, Progressbar
- [ ] **Text**: UTF-8 Parser, Emoji Width Fixes, Word-Wrap, Text-Shaping

## 🟠 v0.5.0: Platform & Logic Expansion
- [ ] **ARM NEON Port**: Parity for Apple Silicon/Mobile.
- [ ] **Advanced Features**: Multi-threaded paths and complex batch operations.

## 🔴 v1.0.0: Production Hardening
- [ ] **Debug**: FPS-Overlay, Memory-Overlay, Scene-Inspector
- [ ] **Full Stability Audit**: Long-run stress testing
- [ ] **Enterprise Support**: NUMA-awareness and Large Pages support

---
**Focus:** Performance is our USP. We optimize where Java stops.
