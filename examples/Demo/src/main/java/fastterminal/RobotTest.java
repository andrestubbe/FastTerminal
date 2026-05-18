package fastterminal;

import fastmouse.FastMouse;
import fastmouse.FastMouseListener;
import fastterminal.ui.Button;

import java.awt.Robot;
import java.awt.event.InputEvent;

/**
 * Automated Native Robot Integration Test for FastTerminal.
 * Simulates physical mouse hovers and click events to verify TUI elements programmatically.
 */
public class RobotTest {

    private static volatile int mouseCellX = -1;
    private static volatile int mouseCellY = -1;
    private static volatile boolean mouseClicked = false;
    private static volatile boolean mousePressedState = false;
    private static volatile boolean actionTriggered = false;

    public static void main(String[] args) {
        System.out.println("=== STARTING AUTOMATED TUI ROBOT INTEGRATION TEST ===");

        // 1. Query native console window metadata
        int[] winInfo = null;
        try {
            winInfo = FastTerminal.getConsoleWindowInfo();
        } catch (Throwable t) {
            System.err.println("[ERROR] Failed to query console window info: " + t.getMessage());
        }

        if (winInfo == null || winInfo[4] <= 0 || winInfo[5] <= 0) {
            System.err.println("[ABORT] Could not retrieve valid Win32 window handle. Are you running inside Windows Terminal/ConPTY?");
            System.err.println("        Robot testing requires standard conhost environment (standard CMD/PowerShell window).");
            return;
        }

        int clientX = winInfo[2];
        int clientY = winInfo[3];
        int fontW = winInfo[4];
        int fontH = winInfo[5];

        System.out.printf("[INFO] Window Client Offset: (%d, %d) | Font Size: %dx%d\n", clientX, clientY, fontW, fontH);

        // 2. Initialize TUI structures and active target button
        FastTerminalRenderer renderer = new FastTerminalRenderer(80, 25);
        FastTerminalScene canvas = new FastTerminalScene(0, 0, 80, 25);
        renderer.addScene(canvas);

        int buttonX = 15;
        int buttonY = 10;
        int buttonW = 20;
        int buttonH = 3;

        Button testButton = new Button(buttonX, buttonY, buttonW, buttonH, "TEST BUTTON", () -> {
            actionTriggered = true;
            System.out.println("[ACTION] Callback executed successfully!");
        });

        // 3. Register native mouse listeners
        FastMouse mouse = FastMouse.open();
        mouse.startListening(new FastMouseListener() {
            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
                int relX = absoluteX - clientX;
                int relY = absoluteY - clientY;
                mouseCellX = relX / fontW;
                mouseCellY = relY / fontH;
            }

            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                if (buttonId == 0) {
                    mousePressedState = isPressed;
                    if (isPressed) {
                        mouseClicked = true;
                    }
                }
            }

            @Override
            public void onMouseWheel(long deviceHandle, int delta) {}
        });

        // 4. Start concurrent TUI render and update dispatch loop
        Thread loopThread = new Thread(() -> {
            while (true) {
                canvas.clear();
                
                // Process hover state updates
                testButton.handleMouseMove(mouseCellX, mouseCellY);

                // Process click/release dispatches
                if (mouseClicked) {
                    testButton.handleMouseClick(mouseCellX, mouseCellY, true);
                    mouseClicked = false;
                } else if (!mousePressedState) {
                    testButton.handleMouseClick(mouseCellX, mouseCellY, false);
                }

                // Render components and print screen telemetry info
                testButton.render(canvas);
                canvas.writeString(5, 2, String.format("Mouse Cell: (%d, %d) | Hovered: %b | Pressed: %b", 
                        mouseCellX, mouseCellY, testButton.isHovered(), testButton.getNormalBg() == 0xCA8A04), 0xEAB308, -1);
                renderer.render();

                try {
                    Thread.sleep(1000 / 60); // 60 FPS viewport refresh
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        loopThread.setDaemon(true);
        loopThread.start();

        // Allow some time for window stabilization
        sleep(1000);

        try {
            Robot robot = new Robot();
            robot.setAutoDelay(50);

            // 5. Calculate screen coordinates of the TUI button center
            int screenTargetX = clientX + (buttonX + buttonW / 2) * fontW;
            int screenTargetY = clientY + (buttonY + buttonH / 2) * fontH;

            System.out.printf("[ROBOT] Moving pointer to TUI Button center: screen (%d, %d)...\n", screenTargetX, screenTargetY);
            robot.mouseMove(screenTargetX, screenTargetY);
            sleep(800); // Give mouse move listeners time to process absolute updates

            // 6. Assert Hover State
            boolean hoverPassed = testButton.isHovered();
            if (hoverPassed) {
                System.out.println("🥇 [ASSERT SUCCESS] Hover state is TRUE!");
            } else {
                System.err.println("❌ [ASSERT FAILED] Button is not in hover state!");
            }

            // 7. Assert Click Action
            System.out.println("[ROBOT] Simulating mouse click...");
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            sleep(200);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            sleep(800); // Give action handlers time to execute callback

            if (actionTriggered) {
                System.out.println("🥇 [ASSERT SUCCESS] Button click callback was triggered!");
            } else {
                System.err.println("❌ [ASSERT FAILED] Click callback was not triggered!");
            }

            // Move cursor back to safety
            robot.mouseMove(clientX, clientY);

            // 8. Output Final Report
            if (hoverPassed && actionTriggered) {
                System.out.println("\n🎉 ALL AUTOMATED TUI ROBOT TESTS PASSED FLAWLESSLY! 🎉");
            } else {
                System.err.println("\n🛑 SOME ROBOT INTEGRATION TESTS FAILED!");
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Exception occurred during Robot test execution: " + e.getMessage());
            e.printStackTrace();
        }

        sleep(1000);
        System.exit(0);
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}
