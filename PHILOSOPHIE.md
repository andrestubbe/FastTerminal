# The Philosophy of FastTerminal

> [!IMPORTANT]
> **"Zero Latency. Zero Dependency. Codepoint Safety. State-Minimized ANSI Blits."**

FastTerminal is built on the principle that modern Java TUI (Terminal User Interface) design requires a **native-first, high-throughput** substrate that standard console APIs and large blocking layouts fail to deliver.

## Core Tenets

### 1. Zero-Dependency Independence
Bypass bloated terminal layouts. Instead of introducing layers of buffering, complex command parsers, and blocking threads, FastTerminal directly targets the physical console boundaries via state-of-the-art ANSI sequences and JNI hooks.

### 2. State-Minimized Compositing
Terminal stream bandwidth is highly latency-sensitive. Standard redraw passes can cause terminal flickering and excessive CPU usage. FastTerminal’s compositing architecture compares cells before printing, emitting 24-bit True Color codes only when colors actually change. This reduces stdout load by up to **80%**.

### 3. Unicode & Codepoint Integrity
We treat the terminal grid as a grid of dynamic 32-bit Unicode Codepoints (`int`) rather than character elements (`char`). This ensures that complex symbols and modern emojis (which occupy 2 or 4 standard Java characters) are treated as single atomic blocks, preventing layout warping and indexing bugs.

### 4. Zero-Allocation Compositing
Compositing multiple layers of terminal text should not trigger Garbage Collector runs. All coordinates, color parameters, and text representations inside FastTerminal use flat primitive buffers (`int[]`), completely avoiding object allocation during render calls.

### 5. Hardware-Aware Console Access
On Windows platforms, standard output streams introduce implicit buffering bottlenecks. FastTerminal is designed with direct JNI hooks into the Win32 console buffer (`WriteConsoleOutputW`), allowing direct memory blits from Java arrays directly into the terminal hardware at **120+ FPS**.

---

**⚡ FastTerminal — Powering the next generation of Native Java TUIs.**
