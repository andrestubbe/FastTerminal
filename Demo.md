# FastTerminal — High-Performance Demos & Interactive Mouse Renderer

Here are 5 technically beautiful, minimalistic, and brutal-fast demonstration templates designed for our independent, zero-dependency, True-Color, and Emoji-enabled `FastTerminal` system. Following that is a 6th interactive mouse-move rectangle demo.

---

## 🌈 Demo 1: True-Color Rainbow Wave
*   **Zweck**: Zeigt 24-bit-Farben + Animation + Fullscreen.
*   **Effekt**: Eine horizontale Regenbogen-Welle läuft über den ganzen Bildschirm, wie ein GPU-Shader im Terminal.
*   **Features**:
    *   24-bit ANSI Foreground & Background
    *   Locked 60 FPS update rate
    *   Fullscreen Alternate Buffer
    *   Codepoint-safe emoji alignment

```
🌈████████████████████████████████████████████████████████████████
██████████████████████████████████████████████████████████████████
```

---

## 📦 Demo 2: Multi-Line Download Progress (apt-style)
*   **Zweck**: Zeigt, wie du 4–5 Zeilen reservierst, live aktualisierst und Text darüberschreibst.
*   **Effekt**: Exakt wie `apt-get install`, `npm install`, oder `curl -#`.
*   **Features**:
    *   ANSI Cursor-save (`\033[s`) & restore (`\033[u`)
    *   Individual line overrides using `\033[2K`
    *   24-bit True Color indicator blocks
    *   Multi-threading safe updates

```
📦 Downloading packages...
  → core-utils     [███████-----] 67%
  → fast-renderer  [██████████--] 92%
  → ui-automation  [██----------] 12%
```

---

## 💚 Demo 3: Emoji-Matrix Rain
*   **Zweck**: Zeigt Emojis + High-FPS + Random Streams.
*   **Effekt**: Matrix-Regen, aber unter Verwendung von Emojis oder Unicode-Glyphen statt Katakana.
*   **Features**:
    *   Codepoint-Renderer (avoiding multi-char emoji line splits)
    *   60 FPS rendering
    *   Random spawn streams
    *   True-Color green gradient fades

```
💚  🟢   💚  🟢  💚   🟢
   💚   🟢   💚   🟢
💚   🟢   💚   🟢   💚
```

---

## 📊 Demo 4: System Monitor (htop-style)
*   **Zweck**: Zeigt, wie du ein echtes TUI-Dashboard baust.
*   **Effekt**: Live CPU bars, memory utilization, and thread tracking.
*   **Features**:
    *   True-Color progress gauges
    *   Multi-panel layout
    *   Precise absolute ANSI cursor positioning
    *   Zero flicker refresh

```
CPU: ████████████░░░░░░░░  47%
RAM: ████████░░░░░░░░░░░  32%
NET: ↑ 12.3 MB/s   ↓ 3.1 MB/s
```

---

## 🎨 Demo 5: FastTerminal Paint
*   **Zweck**: Zeigt Tastatur-Events + Raw-Mode + Zeichnen.
*   **Effekt**: Du zeichnest farbige Pixel im Terminal mit den Pfeiltasten und änderst Farben per Tastendruck.
*   **Features**:
    *   Raw-Keyboard handling via FastKeyboard
    *   Echo disabled
    *   Non-blocking event loop
    *   100% dependency-free

```
Use arrows to paint. Press 'c' to change color.
```

---

## 🖱️ Demo 6: Mouse-Move Rectangle
*   **Zweck**: Ein farbiges Rechteck folgt der Mausbewegung im Fullscreen-Modus.
*   **Effekt**: Du siehst den kompletten Terminal-Screen und bewegst mit der Maus flimmerfrei ein farbiges Rechteck.
*   **Features**:
    *   Alternate Screen Buffer ON (`\033[?1049h`), Cursor hidden (`\033[?25l`)
    *   ANSI mouse movement tracking enabled (`\033[?1003h`, modern SGR mouse mode `\033[?1006h`)
    *   No terminal scrolling, fully-managed grid view
    *   Event payload format: `\033[<eventType>;<x>;<y>M`

```
┌──────────────────────────────────────────────┐
│                                              │
│        ████████████████████████████          │
│        █                                    █│
│        █   ← Das Rechteck folgt der Maus    █│
│        █                                    █│
│        ████████████████████████████          │
│                                              │
└──────────────────────────────────────────────┘
```

### Technical Workflow for Demo 6:
1.  **Fullscreen Mode**: Enter Alternate Buffer, clear, and position cursor at `(1, 1)`.
2.  **ANSI Mouse Tracking**: Send the escape sequence to activate pointer move events:
    *   `\033[?1003h` — Enable all tracking.
    *   `\033[?1006h` — SGR format tracking (standard support across terminal clients).
3.  **Raw Input**: Establish raw terminal attributes (no stdout buffering, direct echo disabling).
    *   *Windows JNI*: Use `SetConsoleMode` with `ENABLE_MOUSE_INPUT | ENABLE_WINDOW_INPUT` or read via `ReadConsoleInputW` to capture standard mouse move struct coordinates.
    *   *Posix JNI*: Configure termios and read raw stdin stream chunks.
4.  **Blit and Double Buffer**: Paint the rectangle bounding box centered around the coordinates of the mouse event.
