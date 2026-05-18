package fastterminal;

import fastmouse.FastMouse;
import fastmouse.FastMouseListener;
import fastmouse.MouseDevice;

import java.util.List;

/**
 * Clean Standalone Mouse Event Stream Monitor.
 * Intercepts raw hardware input streams and outputs clean, scrolling,
 * colored logs on mouse movement, buttons, and wheel scrolls.
 */
public class MouseDedicatedDemo {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("⚡ FASTMOUSE STANDALONE EVENT STREAM MONITOR ⚡");
        System.out.println("======================================================================");
        System.out.println("This diagnostic program prints clean, scrolling raw hardware logs.");
        System.out.println("Use this to verify hardware level mouse movements, clicks, and scroll events.");
        System.out.println("======================================================================\n");

        // 1. Initialize and list hardware
        System.out.println("Initializing FastMouse JNI subsystem...");
        FastMouse mouse = FastMouse.open();
        List<MouseDevice> devices = mouse.getConnectedDevices();

        System.out.println("\n🔌 Connected Mouse Hardware:");
        if (devices.isEmpty()) {
            System.out.println("  [WARNING] No connected raw input mouse devices detected.");
        } else {
            for (MouseDevice dev : devices) {
                System.out.printf("  • Device ID: 0x%08X | Buttons: %d | Name: %s\n", 
                        dev.getHandle(), dev.getButtonCount(), dev.getName());
            }
        }

        System.out.println("\n🚀 Starting active event stream listener...");
        System.out.println("Move your mouse, press buttons, or scroll your wheel inside this console window.");
        System.out.println("Press Ctrl+C to stop and exit.\n");

        // 2. Start listener
        mouse.startListening(new FastMouseListener() {
            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
                System.out.printf("\u001b[32m[MOVE]\u001b[0m Device: 0x%08X | Delta: (%+4d, %+4d) | Absolute screen coords: (%4d, %4d)\n",
                        deviceHandle, deltaX, deltaY, absoluteX, absoluteY);
            }

            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                String btnName = switch (buttonId) {
                    case 0 -> "LEFT";
                    case 1 -> "RIGHT";
                    case 2 -> "MIDDLE";
                    default -> "BUTTON_" + buttonId;
                };
                String state = isPressed ? "\u001b[31mPRESSED\u001b[0m" : "\u001b[34mRELEASED\u001b[0m";
                System.out.printf("\u001b[33m[BUTTON]\u001b[0m Device: 0x%08X | Key: %-6s | State: %s\n",
                        deviceHandle, btnName, state);
            }

            @Override
            public void onMouseWheel(long deviceHandle, int delta) {
                System.out.printf("\u001b[35m[WHEEL]\u001b[0m Device: 0x%08X | Delta Scroll: %s%d\n",
                        deviceHandle, (delta > 0 ? "+" : ""), delta);
            }
        });

        // 3. Keep main thread alive
        try {
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("Diagnostic terminated.");
        }
    }
}
