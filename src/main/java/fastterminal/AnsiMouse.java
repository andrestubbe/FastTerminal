package fastterminal;

import fastmouse.FastMouseListener;
import java.io.IOException;
import java.io.InputStream;

/**
 * 🌌 AnsiMouse - Asynchronous Virtual Terminal Mouse Tracker (SGR-1006).
 * Spawns a dedicated low-priority background thread to parse exact character-cell 
 * coordinates directly from the terminal host's standard input stream.
 */
public class AnsiMouse {
    private final FastMouseListener listener;
    private final Thread readerThread;
    private volatile boolean running = false;
    
    private AnsiMouse(FastMouseListener listener) {
        this.listener = listener;
        this.readerThread = new Thread(this::readLoop, "AnsiMouse-Reader");
        this.readerThread.setDaemon(true);
        this.readerThread.setPriority(Thread.MAX_PRIORITY); // Maximum priority for lag-free mouse rendering
    }
    
    /**
     * Opens mouse tracking and registers a listener callback.
     */
    public static AnsiMouse open(FastMouseListener listener) {
        AnsiMouse mouse = new AnsiMouse(listener);
        mouse.start();
        return mouse;
    }
    
    private void start() {
        if (running) return;
        running = true;
        
        // 1. Configure JNI Virtual Terminal raw input state
        try {
            FastTerminal.setAnsiRawMode(true);
        } catch (Throwable t) {
            System.err.println("[AnsiMouse] Warning: Failed to configure Win32 raw mode: " + t.getMessage());
        }
        
        // 2. Write standard ANSI escape sequences to request mouse coordinates
        // \u001b[?1003h = Track all mouse movement/hover
        // \u001b[?1006h = SGR extended format support
        System.out.print("\u001b[?1003h\u001b[?1006h");
        System.out.flush();
        
        // 3. Boot input stream polling thread
        readerThread.start();
    }
    
    /**
     * Gracefully stops the reading thread and restores standard terminal modes.
     */
    public void close() {
        if (!running) return;
        running = false;
        
        // 1. Disable SGR mouse tracking
        System.out.print("\u001b[?1003l\u001b[?1006l");
        System.out.flush();
        
        // 2. Restore console input mode flags
        try {
            FastTerminal.setAnsiRawMode(false);
        } catch (Throwable ignored) {}
        
        // 3. Interrupt standard input blocking read call
        readerThread.interrupt();
    }
    
    private void readLoop() {
        InputStream in = System.in;
        byte[] buffer = new byte[4096];
        
        int state = 0; // 0=idle, 1=ESC, 2='[', 3='<', 4=parsing button, 5=parsing col, 6=parsing row
        int button = 0;
        int col = 0;
        int row = 0;
        
        int lastCellX = -1;
        int lastCellY = -1;
        
        while (running) {
            try {
                int read = in.read(buffer);
                if (read <= 0) {
                    if (read == -1) break; // EOF
                    continue;
                }
                
                for (int i = 0; i < read; i++) {
                    byte b = buffer[i];
                    
                    switch (state) {
                        case 0:
                            if (b == 27) { // ESC (0x1B)
                                state = 1;
                            }
                            break;
                        case 1:
                            if (b == '[') {
                                state = 2;
                            } else {
                                state = 0;
                                if (b == 27) state = 1;
                            }
                            break;
                        case 2:
                            if (b == '<') {
                                state = 3;
                                button = 0;
                                col = 0;
                                row = 0;
                            } else {
                                state = 0;
                                if (b == 27) state = 1;
                            }
                            break;
                        case 3: // Parsing button ID
                            if (b >= '0' && b <= '9') {
                                button = button * 10 + (b - '0');
                            } else if (b == ';') {
                                state = 4;
                            } else {
                                state = 0;
                                if (b == 27) state = 1;
                            }
                            break;
                        case 4: // Parsing 1-based column coordinate
                            if (b >= '0' && b <= '9') {
                                col = col * 10 + (b - '0');
                            } else if (b == ';') {
                                state = 5;
                            } else {
                                state = 0;
                                if (b == 27) state = 1;
                            }
                            break;
                        case 5: // Parsing 1-based row coordinate
                            if (b >= '0' && b <= '9') {
                                row = row * 10 + (b - '0');
                            } else if (b == 'M' || b == 'm') {
                                boolean isPressed = (b == 'M');
                                int cellX = col - 1;
                                int cellY = row - 1;
                                
                                // Dispatch mouse hover / movement if cell boundaries changed
                                if (cellX != lastCellX || cellY != lastCellY) {
                                    lastCellX = cellX;
                                    lastCellY = cellY;
                                    listener.onMouseMove(0, 0, 0, cellX, cellY);
                                }
                                
                                // Parse and dispatch clicks and scrolling
                                if (button == 35) {
                                    // Pure mouse motion, already dispatched via onMouseMove
                                } else if (button == 64) {
                                    listener.onMouseWheel(0, 1);
                                } else if (button == 65) {
                                    listener.onMouseWheel(0, -1);
                                } else {
                                    // SGR Button Layout: 0=Left, 1=Middle, 2=Right
                                    // FastMouse Listener expects: 0=Left, 1=Right, 2=Middle
                                    int mappedButton = -1;
                                    int rawBtn = button & 3;
                                    if (rawBtn == 0) mappedButton = 0; // Left
                                    else if (rawBtn == 1) mappedButton = 2; // Middle
                                    else if (rawBtn == 2) mappedButton = 1; // Right
                                    
                                    if (mappedButton != -1) {
                                        listener.onMouseButton(0, mappedButton, isPressed);
                                    }
                                }
                                state = 0;
                            } else {
                                state = 0;
                                if (b == 27) state = 1;
                            }
                            break;
                    }
                }
            } catch (IOException e) {
                if (!running) break;
            }
        }
    }
}
