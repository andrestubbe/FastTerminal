# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

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

## v0.1.2 [ALPHA] - FastASCII Method Patch
- **Dependency Update**: Bumped FastASCII dependency to 0.1.1 to resolve missing writeAscii symbol during compilation.

## v0.1.1 [ALPHA] - FastASCII & FastANSI Upgrade
- **FastASCII Integration**: Upgraded internal dependencies to use FastASCII 0.1.0 and FastANSI 0.1.1.
- **Glyph Density Extracted**: FastGlyphDensity logic natively delegated to FastASCII.

## [0.1.0] - YYYY-MM-DD

### Added
- Project initialization
- Core rendering engine foundation
- Color system implementation
- System integration with Win32 API
