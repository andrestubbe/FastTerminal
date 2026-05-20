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

    private int sampleArea(BufferedImage img, double rx, double ry, double areaW, double areaH) {
        if (areaW <= 1.0 && areaH <= 1.0) {
            return sampleBilinear(img, rx, ry);
        }

        int imgW = img.getWidth();
        int imgH = img.getHeight();

        int x0 = Math.max(0, (int) Math.floor(rx - areaW / 2.0));
        int y0 = Math.max(0, (int) Math.floor(ry - areaH / 2.0));
        int x1 = Math.min(imgW - 1, (int) Math.ceil(rx + areaW / 2.0));
        int y1 = Math.min(imgH - 1, (int) Math.ceil(ry + areaH / 2.0));

        if (x1 < x0 || y1 < y0) return sampleBilinear(img, rx, ry);

        long rSum = 0, gSum = 0, bSum = 0;
        int count = 0;

        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                int c = img.getRGB(x, y);
                rSum += (c >> 16) & 0xFF;
                gSum += (c >> 8) & 0xFF;
                bSum += c & 0xFF;
                count++;
            }
        }

        if (count == 0) return 0;
        return ((int)(rSum / count) << 16) | ((int)(gSum / count) << 8) | (int)(bSum / count);
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

        // ── Aspect-ratio correction ─────────────────────────────────────────────
        // Terminal cells are NOT square. A typical monospaced cell is ~8 px wide
        // and ~16 px tall, giving a cell aspect ratio of 0.5 (width/height).
        // We use half-block '▄' so we get 2 vertical pixels per cell row, making
        // the effective pixel grid: width × (2*height).
        // The effective pixel-grid aspect is:  (width * cellAspect) / (2 * height)
        // where cellAspect ≈ 0.5, so effectively width / (2 * 2 * height) ... no,
        // let's be precise:
        //   physicalW = width  * fontW   (pixels)
        //   physicalH = height * fontH   (pixels), but half-block gives 2 rows/cell
        //   => physicalH_effective = (2*height) * (fontH/2) = height * fontH
        // So the effective screen aspect ratio is (width*fontW) / (height*fontH).
        // With fontW=8, fontH=16 → cellAspect = 8/16 = 0.5.
        final double CELL_ASPECT = 0.5; // fontW / fontH, typical for most terminals

        // How many image pixels correspond to one terminal column / one half-row:
        //   scaleX = imgW / (width  * CELL_ASPECT / imgAspect)  ... cover mode
        //   scaleY = imgH / (2*height)
        // For a "cover" fill (no black bars, image always fills screen):
        double imgAspect  = (double) imgW / imgH;
        double scrAspect  = (width * CELL_ASPECT) / (double) (2 * height); // effective screen aspect

        // pixels-per-column and pixels-per-half-row at zoom=1:
        double baseScaleX, baseScaleY;
        if (imgAspect > scrAspect) {
            // Image is wider than screen → match height, crop sides
            baseScaleY = (double) imgH / (2 * height);
            baseScaleX = baseScaleY / CELL_ASPECT;
        } else {
            // Image is taller (or equal) → match width, crop top/bottom
            baseScaleX = (double) imgW / (width * CELL_ASPECT);
            baseScaleY = baseScaleX * CELL_ASPECT;
        }

        // 1. Compute dynamic camera zoom and circular pan path
        double zoom = 1.0 + 3.0 * (Math.sin(time) + 1.0) / 2.0; // 1× … 4×

        // Effective image pixels visible in the viewport at this zoom level
        double viewportHalfW = (width  * CELL_ASPECT * baseScaleX) / (2.0 * zoom); // half-width in img px
        double viewportHalfH = (2.0 * height * baseScaleY)         / (2.0 * zoom); // half-height in img px

        // Clamp max pan so the viewport never leaves the image
        double maxPanX = Math.max(0, imgW / 2.0 - viewportHalfW);
        double maxPanY = Math.max(0, imgH / 2.0 - viewportHalfH);

        double panX = (imgW / 2.0) + maxPanX * Math.sin(time * 0.7);
        double panY = (imgH / 2.0) + maxPanY * Math.cos(time * 0.5);

        // 2. High-density half-block rendering (double vertical resolution)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Map terminal cell (x, y) → image coordinates, correcting for cell aspect
                double screenX = (x - width  / 2.0) * CELL_ASPECT; // in "square pixel" units
                double screenYTop = (2 * y     - height);           // top    half-pixel row
                double screenYBot = (2 * y + 1 - height);           // bottom half-pixel row

                double rxTop = panX + screenX  * baseScaleX / zoom;
                double ryTop = panY + screenYTop * baseScaleY / zoom;
                double rxBot = panX + screenX  * baseScaleX / zoom;
                double ryBot = panY + screenYBot * baseScaleY / zoom;

                // Hard clamp to image bounds so we never sample outside
                rxTop = Math.max(0, Math.min(imgW - 1.001, rxTop));
                ryTop = Math.max(0, Math.min(imgH - 1.001, ryTop));
                rxBot = Math.max(0, Math.min(imgW - 1.001, rxBot));
                ryBot = Math.max(0, Math.min(imgH - 1.001, ryBot));

                // Size of one cell's footprint in the source image
                double areaW = baseScaleX / zoom;
                double areaH = baseScaleY / zoom;

                int colorTopCurr = sampleArea(imageCurr, rxTop, ryTop, areaW, areaH);
                int colorBotCurr = sampleArea(imageCurr, rxBot, ryBot, areaW, areaH);

                int colorTop, colorBot;

                if (fadeAmount < 1.0) {
                    int colorTopPrev = sampleArea(imagePrev, rxTop, ryTop, areaW, areaH);
                    int colorBotPrev = sampleArea(imagePrev, rxBot, ryBot, areaW, areaH);

                    int rCurrT = (colorTopCurr >> 16) & 0xFF, gCurrT = (colorTopCurr >> 8) & 0xFF, bCurrT = colorTopCurr & 0xFF;
                    int rPrevT = (colorTopPrev >> 16) & 0xFF, gPrevT = (colorTopPrev >> 8) & 0xFF, bPrevT = colorTopPrev & 0xFF;
                    colorTop = (((int)(rPrevT + (rCurrT - rPrevT) * fadeAmount)) << 16)
                             | (((int)(gPrevT + (gCurrT - gPrevT) * fadeAmount)) << 8)
                             |  ((int)(bPrevT + (bCurrT - bPrevT) * fadeAmount));

                    int rCurrB = (colorBotCurr >> 16) & 0xFF, gCurrB = (colorBotCurr >> 8) & 0xFF, bCurrB = colorBotCurr & 0xFF;
                    int rPrevB = (colorBotPrev >> 16) & 0xFF, gPrevB = (colorBotPrev >> 8) & 0xFF, bPrevB = colorBotPrev & 0xFF;
                    colorBot = (((int)(rPrevB + (rCurrB - rPrevB) * fadeAmount)) << 16)
                             | (((int)(gPrevB + (gCurrB - gPrevB) * fadeAmount)) << 8)
                             |  ((int)(bPrevB + (bCurrB - bPrevB) * fadeAmount));
                } else {
                    colorTop = colorTopCurr;
                    colorBot = colorBotCurr;
                }

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
