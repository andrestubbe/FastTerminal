package fastterminal;

import fastemojis.FastEmojis;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Clean High-Resolution 2880x1920 PNG Grid Generator.
 * Renders ONLY the emoji grid on a pure black background (no text, no borders).
 */
public class PaletteImageGenerator {

    public static void main(String[] args) {
        int width = 2880;
        int height = 1920;

        System.out.println("⚡ Starting Pure Black PNG Emoji Raster Generation...");

        // 1. Initialize BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // 2. Enable Anti-Aliasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 3. Draw Pure Black Background (#000000)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        // 4. Reflectively Load Curated Emojis from FastEmojis
        List<String> emojis = new ArrayList<>();
        try {
            Field[] fields = FastEmojis.class.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                    String value = (String) field.get(null);
                    if (FastEmojis.getWidth(value.codePointAt(0)) == 2) {
                        emojis.add(value);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to reflectively load FastEmojis: " + e.getMessage());
        }

        // 5. Render Emoji Grid
        int startX = 160;
        int startY = 160;
        int gridSpacingX = 180;
        int gridSpacingY = 180;
        int cols = (width - 320) / gridSpacingX;

        // Load standard system emoji font (Segoe UI Emoji)
        g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 110));

        for (int i = 0; i < emojis.size(); i++) {
            String em = emojis.get(i);
            int col = i % cols;
            int row = i / cols;

            int posX = startX + col * gridSpacingX;
            int posY = startY + row * gridSpacingY;

            // Draw the emoji symbol directly
            g.drawString(em, posX, posY + 100);
        }

        g.dispose();

        // 6. Save images
        try {
            File dest1 = new File("../FastEmojis/docs/screenshot.png");
            dest1.getParentFile().mkdirs();
            ImageIO.write(image, "PNG", dest1);
            System.out.println("✅ Saved to: " + dest1.getAbsolutePath());

            File dest2 = new File("../FastTerminal/docs/screenshot.png");
            dest2.getParentFile().mkdirs();
            ImageIO.write(image, "PNG", dest2);
            System.out.println("✅ Saved to: " + dest2.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("❌ Failed to write PNG: " + e.getMessage());
        }
    }
}
