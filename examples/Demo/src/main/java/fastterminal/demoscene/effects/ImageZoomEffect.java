package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @class ImageZoomEffect
 * @brief High-Resolution Realistic Photo Zoom & Pan Effect.
 * 
 * Employs high-density half-block characters ('▄') to double the vertical resolution,
 * projecting 24-bit photo pixels with real-time panning and zoom scaling.
 * Incorporates procedural textures as a reliable fallback when local disk assets cannot be loaded.
 */
public class ImageZoomEffect implements DemosceneEffect {

    private int width;
    private int height;
    private BufferedImage[] images = new BufferedImage[3];
    private int currentImageIndex = 0;
    private int prevImageIndex = 0;
    private double fadeAmount = 1.0;
    private double time = 0.0;

    /**
     * @brief Initializes view sizes and attempts to load project textures.
     * 
     * Loads local artwork or automatically initializes high-detail synthetic textures on JIT failure.
     * 
     * @param width Terminal screen width.
     * @param height Terminal screen height.
     */
    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        String[] filenames = { "cyberpunk_city.png", "synthwave_sunset.png", "space_nebula.png" };

        // Try to load the images from multiple standard project directories
        for (int i = 0; i < filenames.length; i++) {
            try {
                File imgFile = new File(filenames[i]);
                if (!imgFile.exists()) {
                    imgFile = new File("examples/Demo/" + filenames[i]);
                }
                if (!imgFile.exists()) {
                    imgFile = new File("../" + filenames[i]);
                }
                if (imgFile.exists()) {
                    images[i] = ImageIO.read(imgFile);
                }
            } catch (Exception ignored) {
            }
        }

        // Fallback highly detailed synthetic procedural images in case file loading fails
        for (int i = 0; i < 3; i++) {
            if (images[i] == null) {
                images[i] = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
                for (int y = 0; y < 512; y++) {
                    for (int x = 0; x < 512; x++) {
                        int r = 0, g = 0, b = 0;
                        if (i == 0) { // Cyberpunk City procedural
                            r = (int) (128 + 127 * Math.sin(x * 0.03 + y * 0.02));
                            g = (int) (128 + 127 * Math.cos(y * 0.03 - x * 0.01));
                            b = (int) (128 + 127 * Math.sin((x + y) * 0.02));
                        } else if (i == 1) { // Synthwave Sunset procedural
                            r = (int) (200 + 55 * Math.sin(y * 0.02));
                            g = (int) (50 + 50 * Math.sin(x * 0.01));
                            b = (int) (120 + 80 * Math.cos(y * 0.015));
                        } else { // Cosmic Nebula procedural
                            r = (int) (80 + 70 * Math.cos((x - 256) * 0.01));
                            g = (int) (20 + 20 * Math.sin(y * 0.02));
                            b = (int) (180 + 75 * Math.sin((x + y) * 0.01));
                        }
                        images[i].setRGB(x, y, ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
                    }
                }
            }
        }
    }

    /**
     * @brief Computes camera glides and controls image cross-fade timings.
     * 
     * @param time Total elapsed time in seconds.
     * @param deltaTime Elapsed time in seconds since last frame.
     */
    @Override
    public void update(double time, double deltaTime) {
        this.time = time * 0.36; // Slowed down for smooth cinematic glide

        // Automatically cycle between the photos every 5.0 seconds
        int index = (int) ((time / 5.0) % 3);
        if (images[index] != null && index != currentImageIndex) {
            prevImageIndex = currentImageIndex;
            currentImageIndex = index;
            fadeAmount = 0.0; // Trigger a smooth cross-fade transition!
        }

        // Slowly increment the transition fade factor over 0.333 seconds (40 frames at 120 FPS)
        if (fadeAmount < 1.0) {
            fadeAmount += deltaTime / 0.333;
            if (fadeAmount > 1.0) {
                fadeAmount = 1.0;
            }
        }
    }

    private int sampleBilinear(BufferedImage img, double rx, double ry) {
        int imgW = img.getWidth();
        int imgH = img.getHeight();

        int x0 = (int) Math.floor(rx);
        int y0 = (int) Math.floor(ry);

        double fx = rx - Math.floor(rx);
        double fy = ry - Math.floor(ry);

        int x0_c = Math.max(0, Math.min(imgW - 1, x0));
        int y0_c = Math.max(0, Math.min(imgH - 1, y0));
        int x1_c = Math.max(0, Math.min(imgW - 1, x0 + 1));
        int y1_c = Math.max(0, Math.min(imgH - 1, y0 + 1));

        int c00 = img.getRGB(x0_c, y0_c);
        int c10 = img.getRGB(x1_c, y0_c);
        int c01 = img.getRGB(x0_c, y1_c);
        int c11 = img.getRGB(x1_c, y1_c);

        double w00 = (1.0 - fx) * (1.0 - fy);
        double w10 = fx * (1.0 - fy);
        double w01 = (1.0 - fx) * fy;
        double w11 = fx * fy;

        int r = (int) (((c00 >> 16) & 0xFF) * w00 + ((c10 >> 16) & 0xFF) * w10 + ((c01 >> 16) & 0xFF) * w01 + ((c11 >> 16) & 0xFF) * w11);
        int g = (int) (((c00 >> 8) & 0xFF) * w00 + ((c10 >> 8) & 0xFF) * w10 + ((c01 >> 8) & 0xFF) * w01 + ((c11 >> 8) & 0xFF) * w11);
        int b = (int) ((c00 & 0xFF) * w00 + (c10 & 0xFF) * w10 + (c01 & 0xFF) * w01 + (c11 & 0xFF) * w11);

        return (r << 16) | (g << 8) | b;
    }

    /**
     * @brief Projects, bilinear maps, cross-fades, and flushes subpixels to double vertical resolution.
     * 
     * @param canvas Double-buffer render target.
     */
    @Override
    public void render(FastTerminalScene canvas) {
        BufferedImage imageCurr = images[currentImageIndex];
        BufferedImage imagePrev = images[prevImageIndex];
        
        int imgW = imageCurr.getWidth();
        int imgH = imageCurr.getHeight();

        // 1. Compute dynamic camera zoom and circular pan path
        double zoom = 1.0 + 3.0 * (Math.sin(time) + 1.0) / 2.0; // Zoom factor oscillates between 1.0x and 4.0x

        // Mathematically compute exact viewport bounds to prevent edge overshooting at any zoom level
        double viewportHalfW = (imgW / 2.0) / zoom;
        double viewportHalfH = (imgH / 2.0) / zoom;

        // Maximum pan offsets allowed so viewport fits perfectly within [0, imgW] and [0, imgH]
        double maxPanX = imgW / 2.0 - viewportHalfW;
        double maxPanY = imgH / 2.0 - viewportHalfH;

        double panX = (imgW / 2.0) + maxPanX * Math.sin(time * 0.7);
        double panY = (imgH / 2.0) + maxPanY * Math.cos(time * 0.5);

        // 2. High-density half-block rendering (double resolution vertical mapping)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Top half-pixel vertical index
                double rxTop = panX + (x - width / 2.0) * (imgW / (double) width) / zoom;
                double ryTop = panY + (2 * y - height) * (imgH / (double) (2 * height)) / zoom;

                // Bottom half-pixel vertical index
                double rxBot = panX + (x - width / 2.0) * (imgW / (double) width) / zoom;
                double ryBot = panY + (2 * y + 1 - height) * (imgH / (double) (2 * height)) / zoom;

                // Extract packed 24-bit RGB values from current active image
                int colorTopCurr = sampleBilinear(imageCurr, rxTop, ryTop);
                int colorBotCurr = sampleBilinear(imageCurr, rxBot, ryBot);

                int colorTop, colorBot;

                // If currently transitioning, smoothly blend (lerp) RGB channels with previous image
                if (fadeAmount < 1.0) {
                    int colorTopPrev = sampleBilinear(imagePrev, rxTop, ryTop);
                    int colorBotPrev = sampleBilinear(imagePrev, rxBot, ryBot);

                    // Blend Top
                    int rCurrT = (colorTopCurr >> 16) & 0xFF;
                    int gCurrT = (colorTopCurr >> 8) & 0xFF;
                    int bCurrT = colorTopCurr & 0xFF;

                    int rPrevT = (colorTopPrev >> 16) & 0xFF;
                    int gPrevT = (colorTopPrev >> 8) & 0xFF;
                    int bPrevT = colorTopPrev & 0xFF;

                    int rT = (int) (rPrevT + (rCurrT - rPrevT) * fadeAmount);
                    int gT = (int) (gPrevT + (gCurrT - gPrevT) * fadeAmount);
                    int bT = (int) (bPrevT + (bCurrT - bPrevT) * fadeAmount);
                    colorTop = (rT << 16) | (gT << 8) | bT;

                    // Blend Bottom
                    int rCurrB = (colorBotCurr >> 16) & 0xFF;
                    int gCurrB = (colorBotCurr >> 8) & 0xFF;
                    int bCurrB = colorBotCurr & 0xFF;

                    int rPrevB = (colorBotPrev >> 16) & 0xFF;
                    int gPrevB = (colorBotPrev >> 8) & 0xFF;
                    int bPrevB = colorBotPrev & 0xFF;

                    int rB = (int) (rPrevB + (rCurrB - rPrevB) * fadeAmount);
                    int gB = (int) (gPrevB + (gCurrB - gPrevB) * fadeAmount);
                    int bB = (int) (bPrevB + (bCurrB - bPrevB) * fadeAmount);
                    colorBot = (rB << 16) | (gB << 8) | bB;
                } else {
                    colorTop = colorTopCurr;
                    colorBot = colorBotCurr;
                }

                // Write half-block character: Foreground represents bottom pixel, Background represents top pixel
                canvas.writeCell(x, y, '▄', colorBot, colorTop);
            }
        }
    }

    /**
     * @brief Returns the visual user-friendly name of the effect.
     * @return String effect name label.
     */
    @Override
    public String getName() {
        return "Realistic 24-bit Photo Zoom & Pan";
    }
}
