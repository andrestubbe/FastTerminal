# FastTerminal 0.1.0 [ALPHA] — High-Performance True-Color Terminal Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastTerminal/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastTerminal)

**⚡ A zero-dependency terminal graphics and TUI engine for Java, engineered for high-refresh rates, 24-bit True Color,
and native JNI console optimizations with seamless keyboard and mouse integrations.**

FastTerminal is the high-performance terminal substrate of the **FastJava** ecosystem. It introduces a lightweight,
cell-buffered virtual viewport using primitive integer buffers for full 24-bit True Color (foreground & background) and
emoji-safe UTF-32 Codepoint grids, operating completely independently of standard bloated frameworks.

To achieve a completely responsive, zero-latency desktop terminal experience, FastTerminal is designed to pair natively
with the input, styling, and helper modules of the **FastJava** ecosystem:

* ⚡ **[FastANSI](https://github.com/andrestubbe/FastANSI)** — Micro-optimized, garbage-free ANSI escape sequence builder
  and parser for terminal graphics.
* 🚀 **[FastTerminal](https://github.com/andrestubbe/FastTerminal)** — Direct, low-latency, asynchronous raw global and
  local keyboard event handling.
* 🖱️ **[FastMouse](https://github.com/andrestubbe/FastMouse)** — Precise hardware-level and virtual console-mode mouse
  tracking.

[**Watch the Demo**](https://www.youtube.com/watch?v=JoxdReMqjas) | [**Watch the JMH Benchmark**](https://www.youtube.com/watch?v=SAFlmP3dpQs)

---

[![FastTerminal Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=JoxdReMqjas)

```java
// Quick Start — Example

import fastterminal.TerminalRenderer;
import fastterminal.TerminalScene;

public class Demo {
    public static void main(String[] args) {
        // Init a 80x24 viewport
        TerminalRenderer renderer = new TerminalRenderer(80, 24);
        TerminalScene scene = new TerminalScene(0, 0, 80, 24);

        // Write standard text & high-unicode emojis in True Color
        scene.writeString(5, 2, "⚡ FastTerminal Engine ⚡", 0xFFCC00, 0x111111);
        scene.writeString(5, 3, "🚀 True Color, Mouse & Keyboard support!", 0xFFFFFF, -1);

        renderer.addScene(scene);
        renderer.render(); // Blits directly to stdout
    }
}
```

---

## Table of Contents

- [Why FastTerminal?](#why-fastterminal)
- [Key Features](#key-features)
- [Performance Benchmarks](#performance-benchmarks)
- [Architecture & Grid Layout](#architecture--grid-layout)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Technical Examples & Demos](#technical-examples--demos)
- [Performance Optimization](#performance-optimization)
- [Platform JNI Capabilities](#platform-jni-capabilities)
- [License](#license)

---

## Why FastTerminal?

The mission is to build the fastest, most robust native execution kernel on the JVM for console rendering and TUI
interactivity. By combining cell-buffered graphics, double-buffered layouts, and immediate OS hardware input telemetry
via **[FastTerminal](https://github.com/andrestubbe/FastTerminal)** and **[FastMouse](https://github.com/andrestubbe/FastMouse)**, we empower developers to create premium, interactive command-line terminals that rival native C++ applications in visual fidelity and input response—without external dependencies.

---

## Key Features

* **🚫 Zero Dependencies** — Bypasses all high-latency third-party blocking input loops.
* **🎨 24-bit True Color** — Complete support for direct RGB escape codes powered by *
  *[FastANSI](https://github.com/andrestubbe/FastANSI)** (`\033[38;2;R;G;Bm` for foreground and `\033[48;2;R;G;Bm` for
  background).
* **🌍 Emoji & Unicode-Safe** — Eliminates UTF-16 surrogate split bugs by using `int` (UTF-32) codepoint cell buffers
  rather than `char[]` arrays, guaranteeing that Emojis (e.g. `🚀`, `🌈`) fit exactly in 1 cell without warping rows.
* **⚡ State-Minimized Renderer** — Optimizes stdout rendering via **[FastANSI](https://github.com/andrestubbe/FastANSI)
  ** by only emitting escape codes when color states change, reducing console stream bandwidth by up to **80%**.
* **📺 Alternate Screen Buffer** — Seamlessly enters full-screen TUI buffer mode (`\033[?1049h`) and hides the cursor (
  `\033[?25l`) using **[FastANSI](https://github.com/andrestubbe/FastANSI)** utilities for clean dashboard applications.
* **🎹 Native Input Substrates** — Built-in telemetry anchors designed for instant integration with `FastTerminal` and
  `FastMouse` to process mouse tracking, window resizing, and raw key captures natively.
* **📥 apt/npm Style Indicators** — Built-in dynamic bottom-anchored multi-line progress overlays using cursor
  save/restore positions.

---

## Performance Benchmarks

FastTerminal is designed to run the pure mathematics of a UI rendering pipeline (diffing, double-buffering, and composite layering) entirely outside the garbage collector's reach.

[**Watch the JMH Benchmark**](https://www.youtube.com/watch?v=SAFlmP3dpQs)

In the official [JMH Benchmark](examples/Benchmark), the system measured the core `FastTerminalRenderer` performance on a standard 120x30 console output:

```text
Benchmark                               Mode  Cnt   Score    Error   Units
TerminalBenchmark.benchmarkDiffRender  thrpt    5  66,281 ± 18,213  ops/ms
TerminalBenchmark.benchmarkFullRedraw  thrpt    5  34,134 ±  1,509  ops/ms
```

> **66,000,000 Operations per Second**: By bypassing the Windows OS Console I/O bottleneck (which artificially caps traditional terminals), the `FastTerminal` pure math double-buffering engine can generate diffs and compose TrueColor ansi streams at an astonishing **66 Million Frames per Second**. Even when forced to completely calculate a full 120x30 TrueColor redraw from scratch, the engine operates at **34 Million FPS**. Zero garbage, pure speed.

---

## 📊 Architecture & Grid Layout

FastTerminal operates on a double-buffered layer compositor. Each cell in a `TerminalScene` is backed by primitive
integer arrays, eliminating object allocation overhead:

```
[ TerminalScene Viewport Layer ]
  ├─ codepointBuffer (int[] - UTF-32 Unicode codepoints)
  ├─ fgColorBuffer   (int[] - packed 24-bit RGB values)
  └─ bgColorBuffer   (int[] - packed 24-bit RGB values)
```

Composites are blitted to standard output using the highly-optimized **[TerminalRenderer](docs/REFERENCE.md)**.

---

## API Quick Reference

| Method                                | Description                                              | Path                                    |
|---------------------------------------|----------------------------------------------------------|-----------------------------------------|
| `writeCell(col, row, cp, fg, bg)`     | Writes a single codepoint and packed colors to the grid. | [Reference →](docs/REFERENCE.md#writecell)   |
| `writeString(col, row, text, fg, bg)` | Renders a string including standard emojis safely.       | [Reference →](docs/REFERENCE.md#writestring) |
| `render()`                            | Blits all active scenes to the console screen buffer.    | [Reference →](docs/REFERENCE.md#render)      |
| `clear()`                             | Resets all layers to default spaces and standard styles. | [Reference →](docs/REFERENCE.md#clear)       |

> [!TIP]
> See **[REFERENCE.md](docs/REFERENCE.md)** for full class definitions, packed color utilities, and JNI specs.

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependencies to your `pom.xml`:

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
<!-- FastTerminal Library -->
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fastterminal</artifactId>
    <version>0.1.0</version>
</dependency>

<!-- FastCore (Required Native Loader) -->
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fastcore</artifactId>
    <version>0.1.0</version>
</dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastterminal:0.1.0'
    implementation 'com.github.andrestubbe:fastcore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 📦 **[fastterminal-0.1.0.jar](https://github.com/andrestubbe/FastTerminal/releases/download/0.1.0/fastterminal-0.1.0.jar)** (The Core Library)
2. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (The Mandatory Native Loader)

> [!IMPORTANT]
> All JARs must be in your classpath for the native JNI calls to function correctly.

## Technical Examples & Demos

See the active Java programs under the `examples/Demo` package:

| Case                    | Java Example                                                          | Performance / Demo                         | Details                                                                                                                                                     |
|-------------------------|-----------------------------------------------------------------------|--------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Demoscene Megademo      | [Demo.java](examples/Demo/src/main/java/fastterminal/Demo.java)       | High-fidelity visual FX catalog            | Auto-cycles through active fluid, fire, grids and 3D wireframes                                                                                             |
| Native Mouse Visualizer | [UI.java](examples/Demo/src/main/java/fastterminal/UI.java)           | Real-time interactive coordinates & clicks | Draggable, resizable BeOS-style panel with file navigator and custom ANSI cursor over a background image                                                    |
| Terminal Overlay        | [Overlay.java](examples/Demo/src/main/java/fastterminal/Overlay.java) | Floating panel over live terminal content  | Snapshots the console buffer at startup via `ReadConsoleOutputW`, projects a draggable panel on top, and fully restores the original terminal state on exit |

---

## Platform JNI Capabilities

While fully functional via optimized ANSI escapes, FastTerminal is architected for native Windows console hardware
blitting:

* **`GetConsoleScreenBufferInfo`** — Retrieves precise buffer columns and rows on-the-fly.
* **`WriteConsoleOutputW`** — Direct console frame buffer hardware writes bypassing standard streams (up to **200 FPS
  **).
* **`SetConsoleMode`** — Hooks raw mouse and window buffer change inputs natively.

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastTerminal](https://github.com/andrestubbe/FastTerminal)
- [FastANSI](https://github.com/andrestubbe/FastANSI)
- [FastEmojis](https://github.com/andrestubbe/FastEmojis)
- [FastUI](https://github.com/andrestubbe/FastUI)
- [FastGrid](https://github.com/andrestubbe/FastGrid)
- [FastProportion](https://github.com/andrestubbe/FastProportion)
- [FastTheme](https://github.com/andrestubbe/FastTheme)
- [FastCore](https://github.com/andrestubbe/FastCore)

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*


