# FastTerminal — Technical Roadmap & TODO List

This document acts as our development roadmap and architectural design system for building a zero-dependency, ultra-fast, true-color, and emoji-enabled terminal rendering engine for Java.

---

## 🛠️ Part 1: Current Bugs & Architectural Fixes
Below are the analyzed flaws of the initial implementation and how we will resolve them.

### 1. `TerminalScene` — Dirty-Flag Semantics
*   **❌ The Bug**: `update()` sets `dirty = true`, but `render()` immediately clears it to `false`. This causes scenes to render only when external log events fire, while any background animations (like a title spinner or real-time ticker) freeze.
*   **✔ The Fix**: `update()` must *not* set `dirty = true`. The `dirty` state indicates whether the renderer needs to pull changes, not that the scene has just re-calculated itself.
    ```java
    public void update() {
        if (this.updater != null) {
            this.clear();
            this.updater.run();
            // REMOVE: this.dirty = true;
        }
    }
    ```
    Dirty propagation will be triggered externally via timers, keypresses, or callbacks.

### 2. CLILogger / Renderers — Buffer Overwrite
*   **❌ The Bug**: When updating a line of text in the character buffer, if the new line is shorter than the old one, the trailing characters from the old line remain visible.
*   **✔ The Fix**: Prior to drawing new content to a line in a text buffer, fill the line range with empty space characters:
    ```java
    Arrays.fill(buffer, bufferStart, bufferStart + this.getWidth(), ' ');
    ```

### 3. `TerminalRenderer` — Composite Buffer Lifecycle
*   **❌ The Bug**: `TerminalRenderer` does a `System.arraycopy(...)` but never clears the `compositeBuffer` between frames, resulting in ghost characters and rendering artifacts.
*   **✔ The Fix**: Clear the entire `compositeBuffer` with spaces at the beginning of each `render()` pass.
    ```java
    public void render() {
        this.clear(); // Clear before painting
        System.out.print("\033[1;1H");
        ...
    }
    ```

### 4. `TerminalRenderer` — Alternate Screen & Cursor Resets
*   **❌ The Bug**: Cursor reset via `\033[1;1H` works, but without clearing the screen or using an alternate buffer, artifacts persist in unused space.
*   **✔ The Fix**: Use alternate screen buffer ANSI sequences (`\033[?1049h`) to take over the terminal in full-screen mode, hide the cursor (`\033[?25l`), and properly clear the viewport.

### 5. `CLIScene` — Resize-Handling & Listener Leaks
*   **❌ The Bug**: Re-adding scenes and listeners on resize causes potential race-conditions and memory leaks if listeners are not cleanly detached beforehand.
*   **✔ The Fix**: Securely detach and clean up listeners before any recreation occurs:
    ```java
    Log.removeListener(logListener);
    ```

### 6. `TerminalScene.dispose()` Null-Safety
*   **❌ The Bug**: Setting `charBuffer = null` inside `dispose()` results in a immediate `NullPointerException` if `insertScene()` is invoked right after.
*   **✔ The Fix**: Establish rigorous state checks and ensure scenes are removed from the active renderer list during cleanup.

---

## 🚀 Part 2: The Path to Independence, Speed, & True Color
We are focusing on direct OS native hooks to achieve zero latency, full UTF-8 capabilities, alternate screen buffers, and high-performance JNI integration.

### 📋 1. Zero-Dependency Dimension Queries
*   **Terminal Dimensions**: Retrieve screen buffer info natively using `GetConsoleScreenBufferInfo` on Windows (via JNI) and `ioctl(TIOCGWINSZ)` on Linux/macOS.
*   **Keyboard Input**: Integrate directly with `FastKeyboard` to handle asynchronous raw mode input.
*   **Console Raw Mode**: Toggle echo and raw modes directly using native JNI calls:
    *   **Windows**: `SetConsoleMode(hIn, ENABLE_WINDOW_INPUT | ENABLE_MOUSE_INPUT)`
    *   **Linux**: `tcsetattr`

### 🎨 2. True Color (24-bit RGB) Support
Instead of being limited to 16 basic colors, we will support full 24-bit True Color using standard ANSI escape codes:
*   **Foreground Color**: `\033[38;2;R;G;Bm`
*   **Background Color**: `\033[48;2;R;G;Bm`
*   *Example*: `System.out.print("\033[38;2;255;120;0mHello 24-bit Orange!\033[0m");`

### 🌐 3. Full Emoji & Unicode Support (Codepoints)
Standard Java `char` values are UTF-16, meaning emojis occupy 2 or 4 chars. If we do standard character-by-character length checking, emojis break and warp lines.
*   **✔ Solution**: Transition our internal text representation and buffer arrays from `char[]` to **Codepoint-based arrays (`int[]`)** or UTF-8 byte streams.

### ⚡ 4. Alternate Screen Buffer & WriteConsoleOutputW
To achieve game-like performance (60–120 FPS):
*   **ANSI Fullscreen (Alternate Screen Buffer)**:
    *   `\033[?1049h` — Enter alternate screen buffer (hides existing terminal prompt history).
    *   `\033[?25l` — Hide cursor.
    *   `\033[?1049l` — Exit alternate screen buffer (restores prompt context).
*   **Windows JNI Fast-Path (`WriteConsoleOutputW`)**:
    *   Instead of converting buffers to Java strings and printing to `System.out`, we will use the native Windows console API `WriteConsoleOutputW`. This allows copying our entire double-buffered grid directly to the console hardware buffer in a single native call, yielding 10x-20x higher throughput.

### 📥 5. Dynamic Progress Bars (apt / curl style)
For downloads, installers, and live pipelines, we will implement dynamic bottom-anchored multiline progress indicators using ANSI cursor preservation:
*   `\033[s` — Save cursor position.
*   `\033[row;colH` — Move to progress bar row.
*   `\033[2K` — Clear current line.
*   Update live progress (True Color + Emojis).
*   `\033[u` — Restore cursor to original position for seamless logging.

---

## 🏗️ Part 3: Architecture & Class Structure

Our unified `FastTerminal` module will have a streamlined, modular layout:

```
FastTerminal
 ├── FastTerminalNative (JNI wrapper)
 │    ├── getTerminalSize()         --> returns [cols, rows]
 │    ├── setRawMode(boolean active) --> configures terminal flags
 │    └── writeConsoleOutput(...)   --> Windows Console API direct blit
 ├── FastTerminalRenderer
 │    ├── frontBuffer (int[] codepoints)
 │    ├── backBuffer (int[] codepoints)
 │    ├── diff()                    --> compares buffers for minimum writes
 │    └── render()                  --> outputs changes
 └── FastTerminalScene
      ├── update()
      └── draw()
```

---

## 📝 TODO Checklist

### Phase 1: Cleanup & Restructuring
*   [ ] Remove legacy demo files (`CLILogger.java`, `CLITitle.java`).
*   [ ] Move core terminal classes (`TerminalRenderer.java`, `TerminalScene.java`, `CLIScene.java`) into the new Mavenized project package (`fastterminal`).
*   [ ] Refactor Maven `pom.xml` to include `FastCore` dependency and target Java 17+.

### Phase 2: Native JNI Core
*   [ ] Implement native C++ functions in `native/` for `GetConsoleScreenBufferInfo` and console mode setup.
*   [ ] Integrate JNI bindings in `FastTerminalNative.java` to allow querying terminal dimensions dynamically.
*   [ ] Build compilation script (`compile.bat`) aligned with Java 17+ and MSVC compiler.

### Phase 3: Codepoints & True Color
*   [ ] Refactor `TerminalScene` buffer representation to use `int[]` for safe UTF-32/Unicode Codepoint handling.
*   [ ] Build high-level True Color formatting builders for foreground and background.
*   [ ] Implement Double Buffering logic in `TerminalRenderer` to support diff-based ANSI rendering.

### Phase 4: Dynamic Views & Demos
*   [ ] Create a standalone demo displaying high-refresh rate live widgets, true-color patterns, and emojis.
*   [ ] Create a curl/npm-like progress bar demonstration that updates the bottom of the screen dynamically while terminal logs print above.
