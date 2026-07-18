# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.1.5] - 2026-07-19

### Added
- **Optimized Hybrid-BufferedScene**: Redesigned `BufferedScene` using a cache-friendly 2-array layout (`int[]` codepoints and `long[]` packed colors) to reduce read memory bandwidth by 33%.
- **Pre-clipped Loop Blitter**: Shifted bounds checks out of inner rendering loops in `BufferedScene` to enable maximum compiler optimization and JIT loop unrolling.
- **Native Compatibility**: Implemented JNI-compatible fast-unpacking inside the blitter, allowing seamless drawing onto `FastTerminalScene`'s 3-array layout without breaking the native C++ renderer.

## [0.1.4] - 2026-07-18

### Added
- **Native Event-driven Resize Watcher**: Implemented native Windows buffer size listener using a separate CONIN$ handle, avoiding input loss and polling.
- **ResizeListener API**: Added `addResizeListener(ResizeListener)` and `removeResizeListener(ResizeListener)` on `FastTerminal`.
- **Javadocs**: Documented all resize-related JNI and Java API methods.

## [0.1.3] - 2026-07-12

### Added
- Initial JNI implementation
- Automated compile scripts
- Native Windows API integration
- Double-Buffering and Diff-Rendering
- Dirty-Rectangles and Partial Flush
- 24-bit Gradients (linear, vertical, diagonal)
- Paletten, Themes, Alpha-Compositing
- Native Win32 JNI Resize-Events & swapchain `resize()`

### Planned
- Input handling (Maus-Events, Key-Events, Modifier-Keys, Drag-Tracking)
- Layout system (Flex-Layout, Grid-Layout, Anchors, Auto-Resize)
- Widgets (Buttons, Panels, Textbox, Scrollview, Progressbar)
- Text processing (UTF-8 Parser, Emoji Width Fixes, Word-Wrap, Text-Shaping)
- Performance optimizations (SIMD-Blitting, Native Line-Drawing, GPU-Terminal-Mode)

## v0.1.1 [ALPHA] - FastASCII & FastANSI Upgrade
- **FastASCII Integration**: Upgraded internal dependencies to use FastASCII 0.1.1 and FastANSI 0.1.1.
- **Glyph Density Extracted**: FastGlyphDensity logic natively delegated to FastASCII.

## [0.1.0] - YYYY-MM-DD

### Added
- Project initialization
- Core rendering engine foundation
- Color system implementation
- System integration with Win32 API
