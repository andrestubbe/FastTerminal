# FastTerminal API Reference

This document outlines the detailed API contracts, class structures, rendering pipelines, and JNI integrations of the **FastTerminal** engine.

---

## 1. Class: `TerminalScene`
Represents a layer/viewport grid within the console. It encapsulates dynamic codepoint and RGB color buffers using primitive arrays.

### Constructors
*   `public TerminalScene(int x, int y, int width, int height)`
    Creates a scene at coordinate `(x, y)` with the specified dimensions. Initializes cell buffers to spaces (' ') and default style codes (-1).

### Methods
*   `public void writeCell(int col, int row, int codepoint, int fg, int bg)`
    *   **Description**: Modifies a single cell's Unicode codepoint and 24-bit True Colors.
    *   **fg / bg**: Int-packed RGB colors (e.g. `0xFFCC00`) or `-1` for terminal default color.
*   `public void writeString(int startCol, int row, String text, int fg, int bg)`
    *   **Description**: Safely writes a Java `String` at a row index.
    *   **Unicode Safety**: Internally checks surrogate character boundaries, converting UTF-16 characters to 32-bit codepoints. Supports emojis cleanly without warping line layouts.
*   `public void clear()`
    *   **Description**: Resets the scene buffer to space (' ') and standard styles.
*   `public int[] getCodepointBuffer()`
    *   **Description**: Returns the raw backing array of codepoints.
*   `public int[] getFgBuffer()` / `public int[] getBgBuffer()`
    *   **Description**: Returns the raw packed foreground and background color arrays.
*   `public void setUpdater(Runnable updater)`
    *   **Description**: Attaches an execution closure called before each render loop pass to update dynamic values (like animated grids or indicators).

---

## 2. Class: `TerminalRenderer`
Main compositor engine that coordinates multi-scene compositing and blits to standard output.

### Constructors
*   `public TerminalRenderer(int width, int height)`
    Initializes a composite back-buffer grid of specified dimensions.

### Methods
*   `public void addScene(TerminalScene scene)`
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

---

## 4. Class: `Log` / `LogListener`
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
