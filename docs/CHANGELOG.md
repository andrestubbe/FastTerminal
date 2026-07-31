# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.1.11] - 2026-07-31

### Added
- **Swing Debug Console (`fastterminal.swing.Console`)**: Added built-in dark-themed Swing debugging console with thread-safe `println()` methods and customizable window bounds (`x`, `y`, `width`, `height`).

## [0.1.9] - 2026-07-29

### Fixed
- **Double-Width Emoji Continuation Rendering**: Fixed continuation marker (`-99`) cell processing in `FastTerminalRenderer.renderAbsolute()`, preventing redundant space character output for surrogate-pair emojis and eliminating row wrapping / lower line shift glitches.

## [0.1.8] - 2026-07-26

### Added
- **Native JNI FastStyle Engine Integration**: Fully integrated `FastStyle` bitmask cell formatting (`UNDERLINE`, `BOLD`, `ITALIC`, `STRIKETHROUGH`, `BLINK`, `INVERT`) into the native C++ double-buffering renderer (`fastterminal.cpp` / `fastterminal.dll`).
- **JNI Signature Expansion**: Expanded `renderAnsiNative` JNI contract to pass `compositeStyles` and `prevStyles` primitive byte arrays via zero-allocation `GetPrimitiveArrayCritical` pointers.
- **Native ANSI SGR Style Emitter**: Implemented high-performance inline C++ `emitStyle` sequence generator supporting state-minimized ANSI SGR escape codes (`\033[4m` for native terminal underlines, `\033[1m` for bold, `\033[3m` for italic, etc.).

## [0.1.7] - 2026-07-22

### Added
- **FastStyle Text Formatting Substrate**: Added zero-allocation bitmask cell style support (`FastStyle.NONE`, `BOLD`, `ITALIC`, `UNDERLINE`, `STRIKETHROUGH`, `BLINK`, `INVERT`) across scenes, compositors, and stream renderers.
- **Style-Aware Cell Buffers**: Added `byte[] styleBuffer` in `FastTerminalScene` and `FastTerminalRenderer` with O(1) cache-friendly bitmask diffing.
- **Scene Overloads**: Added `writeCell(col, row, cp, fg, bg, style)` and `writeString(col, row, text, fg, bg, style)` for direct styled text rendering.
- **Documentation & Platform Specs**: Integrated comprehensive Platform Support matrix and Documentation links in `README.md`.

## [0.1.6] - 2026-07-19

### Added
- **CellConsumer Integration**: Implemented `fastansi.CellConsumer` on `BufferedScene` and refactored layout/styling engines like `Gradient` to draw directly onto generic `CellConsumer` targets.
- **Partial Cell Color Updates**: Added support for partial color updates (updating only fg or bg) in `BufferedScene.writeCell` by preserving existing packed values.
- **Writability Safety**: Supported passing `-2` as a codepoint to `FastTerminalScene` and `BufferedScene` to update cell colors without overwriting character glyphs.

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
