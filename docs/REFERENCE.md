# FastTerminal API Reference

This document outlines the detailed API contracts, class structures, rendering pipelines, and JNI integrations of the **FastTerminal** engine.

---

## 1. Class: `FastTerminalScene`
Represents a layer/viewport grid within the console. It encapsulates dynamic codepoint and RGB color buffers using primitive arrays.

### Constructors
*   `public FastTerminalScene(int x, int y, int width, int height)`
    Creates a scene at coordinate `(x, y)` with the specified dimensions. Initializes cell buffers to spaces (' ') and default style codes (-1).

### Methods
*   `public void writeCell(int col, int row, int codepoint, int fg, int bg)`
    *   **Description**: Modifies a single cell's Unicode codepoint and 24-bit True Colors.
*   `public void writeCell(int col, int row, int codepoint, int fg, int bg, int style)`
    *   **Description**: Modifies a single cell's codepoint, 24-bit True Colors, and `FastStyle` bitmask (e.g. `FastStyle.UNDERLINE | FastStyle.BOLD`).
*   `public void writeString(int startCol, int row, String text, int fg, int bg)`
    *   **Description**: Safely writes a Java `String` at a row index.
*   `public void writeString(int startCol, int row, String text, int fg, int bg, int style)`
    *   **Description**: Safely writes a Java `String` at a row index with a `FastStyle` bitmask.
*   `public byte[] getStyleBuffer()`
    *   **Description**: Returns the raw backing array of cell style bitmasks.
*   `public void clear()`
    *   **Description**: Resets the scene buffer to space (' ') and standard styles.
*   `public int[] getCodepointBuffer()`
    *   **Description**: Returns the raw backing array of codepoints.
*   `public int[] getFgBuffer()` / `public int[] getBgBuffer()`
    *   **Description**: Returns the raw packed foreground and background color arrays.
*   `public void setUpdater(Runnable updater)`
    *   **Description**: Attaches an execution closure called before each render loop pass to update dynamic values (like animated grids or indicators).

---

## 2. Class: `FastTerminalRenderer`
Main compositor engine that coordinates multi-scene compositing and blits to standard output.

### Constructors
*   `public FastTerminalRenderer(int width, int height)`
    Initializes a composite back-buffer grid of specified dimensions.

### Methods
*   `public void addScene(FastTerminalScene scene)`
    *   **Description**: Adds a layer viewport to the compositing pipeline. Scenes added later render on top of earlier scenes.
*   `public void render()`
    *   **Description**: Composites all dirty scenes into a unified screen buffer and writes standard bytes to `System.out`.
    *   **Optimization**: Contains a state-minimizing ANSI encoder. Colors are compared cell-by-cell; ANSI RGB foreground/background sequences are only appended to the byte buffer when colors change.
*   `public void clear()`
    *   **Description**: Clears the composite buffer before layers are composited to avoid ghosting.

---

## 3. Class: `FastTerminal` (JNI native layer)
Provides direct operating system bindings to handle low-level terminal control.

### Static Native Methods
*   `public static native int[] getTerminalSize()`
    *   **Signature (C++)**: `JNIEXPORT jintArray JNICALL Java_fastterminal_FastTerminal_getTerminalSize(JNIEnv* env, jclass clazz)`
    *   **Description**: Directly queries console dimensions natively. On Windows, uses `GetConsoleScreenBufferInfo` from the Win32 API.
*   `public static native void setRawMode(boolean enable)`
    *   **Description**: Configures raw console input mode, bypassing stdout buffering, input line parsing, and local key echo.
*   `renderAnsiNative` (JNI Double-Buffering Engine)
    *   **Signature (C++)**: `JNIEXPORT jint JNICALL Java_fastterminal_FastTerminalRenderer_renderAnsiNative(JNIEnv* env, jclass clazz, jintArray compositeCPArray, jintArray compositeFgArray, jintArray compositeBgArray, jbyteArray compositeStyleArray, jintArray prevCPArray, jintArray prevFgArray, jintArray prevBgArray, jbyteArray prevStyleArray, jbyteArray outBufferArray, jint width, jint height, jboolean forceFullRedraw, jboolean diffRenderingEnabled, jboolean dirtyRectanglesEnabled)`
    *   **Description**: High-performance C++ double-buffering renderer blitting codepoints, 24-bit RGB, and `FastStyle` bitmasks directly to standard output.
*   `public static void addResizeListener(ResizeListener l)`
    *   **Description**: Registers a callback to receive immediate, event-driven terminal resize notifications.
*   `public static void removeResizeListener(ResizeListener l)`
    *   **Description**: Deregisters a listener.

---

## 4. Class: `fastterminal.swing.Console`
Dark-themed Swing debugging console window.

*   `public static void println(String message)`
    *   **Description**: Thread-safe static helper to output log messages directly into the GUI console window.
*   `public Console(int x, int y, int width, int height)`
    *   **Description**: Creates and displays a Swing debugging console at specified screen bounds `(x, y)` and size `(width, height)`.

---

## 5. Class: `Log` / `LogListener`
High-speed thread-safe event logger supporting reactive rendering.

*   `Log.addListener(LogListener l)` / `Log.removeListener(LogListener l)`
    Registers callbacks that trigger terminal refresh cascades whenever dynamic logging events fire.
*   `Log.info(String message)`
    Appends text to the thread-safe logs and broadcasts updates to terminal scenes.

---

## 5. Packed RGB Format
FastTerminal uses standard **24-bit packed RGB integers**:

```
Bit range: [31 - 24]  [23 - 16]  [15 - 8]  [7 - 0]
Field:     [ Unused ] [  Red   ] [ Green ] [ Blue]
```

### Color Constants & Converters
*   `Default Foreground / Background`: `-1` (triggers `\033[39m` / `\033[49m` default terminal resets).
*   `Hex Colors`: `0xFF0000` (Red), `0x00FF00` (Green), `0x0000FF` (Blue), `0xFFCC00` (Orange/Yellow).
