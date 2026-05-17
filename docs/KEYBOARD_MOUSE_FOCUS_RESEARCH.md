# 🧠 Research: Focus & Hover Integration with FastMouse & FastKeyboard

This document analyzes how we can integrate the global hardware events captured by **FastMouse** (via Raw Input `WM_INPUT`) and **FastKeyboard** (via Raw Input `WM_INPUT`) so they are **constrained strictly by the terminal window's focus and mouse-hover states**.

---

## 1. The Architectural Challenge

*   **Global Capture**: Both `FastMouse` and `FastKeyboard` listen to hardware events globally in dedicated native background threads. 
*   **The Focus Issue**: If a user clicks or presses a key while working in a web browser or another text editor, the global Raw Input listener will still capture and execute those keyboard shortcuts or mouse delta scrolls inside our JVM terminal application.
*   **The User Requirement**:
    *   No terminal shortcut or mouse event should execute if the terminal is **not in focus** (inactive window).
    *   If the mouse pointer is **not hovering directly over the terminal window**, no shortcuts/events should be processed.

---

## 2. The Win32 API Native Solution

To solve this completely, we need to map our active console window to its native OS window handle (`HWND`) and check two states in real-time when an event arrives:
1.  Is our terminal window currently in the foreground (`GetForegroundWindow()`)?
2.  Is the mouse cursor currently hovering within the boundaries of our terminal window (`GetCursorPos()` + `WindowFromPoint()`)?

### 🛠️ Key Win32 Functions Needed:
*   **`GetConsoleWindow()`**: Returns the `HWND` of the console window associated with the calling Java process.
*   **`GetForegroundWindow()`**: Returns the handle of the window currently in the foreground (active user focus).
*   **`GetCursorPos(&point)`**: Retrieves the current mouse pointer coordinates in absolute screen space.
*   **`WindowFromPoint(point)`**: Determines which window is physically sitting directly underneath that screen coordinate (taking overlapping windows into account).

---

## 3. Native JNI Bridge Design

We can expose these checks directly via JNI inside our JNI wrapper, or bind them to a lightweight class:

```cpp
#include <windows.h>
#include <jni.h>

extern "C" {
    // 1. Returns our console HWND
    JNIEXPORT jlong JNICALL Java_fastterminal_FastTerminal_getConsoleHWND(JNIEnv* env, jclass clazz) {
        return (jlong)GetConsoleWindow();
    }

    // 2. Checks if our console window is the active focused window in Windows
    JNIEXPORT jboolean JNICALL Java_fastterminal_FastTerminal_isTerminalFocused(JNIEnv* env, jclass clazz) {
        HWND hwndConsole = GetConsoleWindow();
        if (hwndConsole == NULL) return JNI_FALSE;
        return (GetForegroundWindow() == hwndConsole) ? JNI_TRUE : JNI_FALSE;
    }

    // 3. Checks if the mouse cursor is hovering over our terminal window
    JNIEXPORT jboolean JNICALL Java_fastterminal_FastTerminal_isMouseOverTerminal(JNIEnv* env, jclass clazz) {
        HWND hwndConsole = GetConsoleWindow();
        if (hwndConsole == NULL) return JNI_FALSE;

        POINT pt;
        if (GetCursorPos(&pt)) {
            HWND hwndUnderMouse = WindowFromPoint(pt);
            // Verify if the window under the mouse is our terminal or one of its child controls
            if (hwndUnderMouse == hwndConsole || IsChild(hwndConsole, hwndUnderMouse)) {
                return JNI_TRUE;
            }
        }
        return JNI_FALSE;
    }
}
```

---

## 4. Java Event-Filtering Integration

By wrapping these simple native calls, we can implement high-performance, reactive filtering in the Java listeners for both **FastMouse** and **FastKeyboard** without modifying their core libraries.

### ⌨️ FastKeyboard Reactive Filter
```java
// Setup FastKeyboard listener
FastKeyboard keyboard = FastKeyboard.open();

keyboard.startListening((deviceHandle, vKey, makeCode, isPressed, isE0, timestamp, keyChar) -> {
    // A. Verify window holds keyboard focus
    if (!FastTerminal.isTerminalFocused()) {
        return; // Ignore keystroke completely!
    }

    // B. Verify mouse is hovering over our terminal window (optional check for specific hover-shortcuts)
    if (!FastTerminal.isMouseOverTerminal()) {
        return; // Ignore shortcut!
    }

    // C. Safely process keyboard shortcuts
    if (vKey == 0x50 && isPressed) { // 'P' key down
        System.out.println("Shortcut triggered: Action executed!");
    }
});
```

### 🖱️ FastMouse Reactive Filter
```java
// Setup FastMouse listener
FastMouse mouse = FastMouse.open();

mouse.startListening(new FastMouseListener() {
    @Override
    public void onMouseMove(long deviceHandle, int deltaX, int deltaY) {
        // Discard mouse movements if the cursor is outside our terminal window boundaries
        if (!FastTerminal.isMouseOverTerminal()) {
            return; 
        }
        // Handle in-bounds cursor movement
    }

    @Override
    public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
        // Discard mouse clicks if not hovering over the terminal
        if (!FastTerminal.isMouseOverTerminal()) {
            return;
        }
        // Handle in-bounds mouse button action
    }

    @Override
    public void onMouseWheel(long deviceHandle, int delta) {
        // Discard scrolling if cursor is in another application
        if (!FastTerminal.isMouseOverTerminal()) {
            return;
        }
        // Handle in-bounds terminal grid scrolling
    }
});
```

---

## 5. Summary of Benefits

1.  **Overlapping Window Accuracy**: Since `WindowFromPoint` checks window layers, if a browser overlaps the terminal, the hover check correctly returns `false` when hovering over the browser, avoiding phantom clicks.
2.  **Performance Efficiency**: Native calls like `GetForegroundWindow()` and `GetCursorPos()` take **sub-microsecond execution time** on Windows, meaning we can run these checks instantly inside the high-frequency JNI event threads without any frame rate drops.
3.  **Clean Separation of Concerns**: We do not need to rewrite the global JNI raw inputs in `FastMouse` or `FastKeyboard`. They continue to run as dedicated low-level hardware observers, while the terminal layer acts as a reactive gatekeeper.
