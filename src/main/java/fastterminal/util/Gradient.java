package fastterminal.util;

import fastterminal.FastTerminalScene;

/**
 * @class Gradient
 * @brief High-performance, zero-allocation 24-bit True Color color interpolation engine.
 * 
 * Provides static linear color interpolation (lerp) operations across cell buffer layouts,
 * supporting horizontal, vertical, and diagonal gradient space spans.
 */
public final class Gradient {

    private Gradient() {
        // Prevent instantiation
    }

    /**
     * @brief Linearly interpolates (lerp) between two 24-bit RGB colors by a fraction t [0.0, 1.0].
     * 
     * @param colorStart Starting packed RGB color.
     * @param colorEnd Ending packed RGB color.
     * @param t Interpolation fraction factor bounded within [0.0, 1.0].
     * @return Packed 24-bit RGB interpolated value.
     */
    public static int interpolate(int colorStart, int colorEnd, double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        
        int rS = (colorStart >> 16) & 0xFF;
        int gS = (colorStart >> 8) & 0xFF;
        int bS = colorStart & 0xFF;

        int rE = (colorEnd >> 16) & 0xFF;
        int gE = (colorEnd >> 8) & 0xFF;
        int bE = colorEnd & 0xFF;

        int r = (int) (rS + (rE - rS) * t);
        int g = (int) (gS + (gE - gS) * t);
        int b = (int) (bS + (bE - bS) * t);

        return (r << 16) | (g << 8) | b;
    }

    /**
     * @brief Applies a horizontal linear gradient to background cells in a scene region.
     * 
     * @param scene Target scene layer.
     * @param startX Starting column coordinate.
     * @param startY Starting row coordinate.
     * @param width Span width in columns.
     * @param height Span height in rows.
     * @param colorStart Starting packed RGB color.
     * @param colorEnd Ending packed RGB color.
     */
    public static void applyHorizontalBg(FastTerminalScene scene, int startX, int startY, int width, int height, int colorStart, int colorEnd) {
        int sceneW = scene.getWidth();
        int sceneH = scene.getHeight();
        int[] bg = scene.getBgBuffer();
        
        for (int r = 0; r < height; r++) {
            int row = startY + r;
            if (row < 0 || row >= sceneH) continue;
            
            for (int c = 0; c < width; c++) {
                int col = startX + c;
                if (col < 0 || col >= sceneW) continue;
                
                double t = width > 1 ? (double) c / (width - 1) : 0.0;
                bg[row * sceneW + col] = interpolate(colorStart, colorEnd, t);
            }
        }
        scene.setDirty(true);
    }

    /**
     * @brief Applies a vertical linear gradient to background cells in a scene region.
     * 
     * @param scene Target scene layer.
     * @param startX Starting column coordinate.
     * @param startY Starting row coordinate.
     * @param width Span width in columns.
     * @param height Span height in rows.
     * @param colorStart Starting packed RGB color.
     * @param colorEnd Ending packed RGB color.
     */
    public static void applyVerticalBg(FastTerminalScene scene, int startX, int startY, int width, int height, int colorStart, int colorEnd) {
        int sceneW = scene.getWidth();
        int sceneH = scene.getHeight();
        int[] bg = scene.getBgBuffer();
        
        for (int r = 0; r < height; r++) {
            int row = startY + r;
            if (row < 0 || row >= sceneH) continue;
            
            double t = height > 1 ? (double) r / (height - 1) : 0.0;
            int color = interpolate(colorStart, colorEnd, t);
            
            for (int c = 0; c < width; c++) {
                int col = startX + c;
                if (col < 0 || col >= sceneW) continue;
                
                bg[row * sceneW + col] = color;
            }
        }
        scene.setDirty(true);
    }

    /**
     * @brief Applies a diagonal linear gradient to background cells in a scene region.
     * 
     * @param scene Target scene layer.
     * @param startX Starting column coordinate.
     * @param startY Starting row coordinate.
     * @param width Span width in columns.
     * @param height Span height in rows.
     * @param colorStart Starting packed RGB color.
     * @param colorEnd Ending packed RGB color.
     */
    public static void applyDiagonalBg(FastTerminalScene scene, int startX, int startY, int width, int height, int colorStart, int colorEnd) {
        int sceneW = scene.getWidth();
        int sceneH = scene.getHeight();
        int[] bg = scene.getBgBuffer();
        int maxDist = (width - 1) + (height - 1);
        
        for (int r = 0; r < height; r++) {
            int row = startY + r;
            if (row < 0 || row >= sceneH) continue;
            
            for (int c = 0; c < width; c++) {
                int col = startX + c;
                if (col < 0 || col >= sceneW) continue;
                
                double t = maxDist > 0 ? (double) (c + r) / maxDist : 0.0;
                bg[row * sceneW + col] = interpolate(colorStart, colorEnd, t);
            }
        }
        scene.setDirty(true);
    }

    /**
     * @brief Applies a horizontal linear gradient to foreground cells in a scene region.
     * 
     * @param scene Target scene layer.
     * @param startX Starting column coordinate.
     * @param startY Starting row coordinate.
     * @param width Span width in columns.
     * @param height Span height in rows.
     * @param colorStart Starting packed RGB color.
     * @param colorEnd Ending packed RGB color.
     */
    public static void applyHorizontalFg(FastTerminalScene scene, int startX, int startY, int width, int height, int colorStart, int colorEnd) {
        int sceneW = scene.getWidth();
        int sceneH = scene.getHeight();
        int[] fg = scene.getFgBuffer();
        
        for (int r = 0; r < height; r++) {
            int row = startY + r;
            if (row < 0 || row >= sceneH) continue;
            
            for (int c = 0; c < width; c++) {
                int col = startX + c;
                if (col < 0 || col >= sceneW) continue;
                
                double t = width > 1 ? (double) c / (width - 1) : 0.0;
                fg[row * sceneW + col] = interpolate(colorStart, colorEnd, t);
            }
        }
        scene.setDirty(true);
    }
    public static void applyVerticalFg(FastTerminalScene scene, int startX, int startY, int width, int height, int colorStart, int colorEnd) {
        int sceneW = scene.getWidth();
        int sceneH = scene.getHeight();
        int[] fg = scene.getFgBuffer();
        for (int r = 0; r < height; r++) {
            int row = startY + r;
            if (row < 0 || row >= sceneH) continue;
            double t = height > 1 ? (double) r / (height - 1) : 0.0;
            int color = interpolate(colorStart, colorEnd, t);
            for (int c = 0; c < width; c++) {
                int col = startX + c;
                if (col < 0 || col >= sceneW) continue;
                fg[row * sceneW + col] = color;
            }
        }
        scene.setDirty(true);
    }

    public static void applyDiagonalFg(FastTerminalScene scene, int startX, int startY, int width, int height, int colorStart, int colorEnd) {
        int sceneW = scene.getWidth();
        int sceneH = scene.getHeight();
        int[] fg = scene.getFgBuffer();
        int maxDist = (width - 1) + (height - 1);
        for (int r = 0; r < height; r++) {
            int row = startY + r;
            if (row < 0 || row >= sceneH) continue;
            for (int c = 0; c < width; c++) {
                int col = startX + c;
                if (col < 0 || col >= sceneW) continue;
                double t = maxDist > 0 ? (double) (c + r) / maxDist : 0.0;
                fg[row * sceneW + col] = interpolate(colorStart, colorEnd, t);
            }
        }
        scene.setDirty(true);
    }

    public static void applyRadialBg(FastTerminalScene scene, int startX, int startY, int width, int height, int colorStart, int colorEnd) {
        int sceneW = scene.getWidth();
        int sceneH = scene.getHeight();
        int[] bg = scene.getBgBuffer();
        double cx = width / 2.0;
        double cy = height / 2.0;
        double maxDist = Math.sqrt(cx * cx + cy * cy);
        
        for (int r = 0; r < height; r++) {
            int row = startY + r;
            if (row < 0 || row >= sceneH) continue;
            for (int c = 0; c < width; c++) {
                int col = startX + c;
                if (col < 0 || col >= sceneW) continue;
                double dist = Math.sqrt((c - cx) * (c - cx) + (r - cy) * (r - cy));
                double t = maxDist > 0 ? dist / maxDist : 0.0;
                bg[row * sceneW + col] = interpolate(colorStart, colorEnd, t);
            }
        }
        scene.setDirty(true);
    }

    public static void applyConicBg(FastTerminalScene scene, int startX, int startY, int width, int height, int colorStart, int colorEnd) {
        int sceneW = scene.getWidth();
        int sceneH = scene.getHeight();
        int[] bg = scene.getBgBuffer();
        double cx = width / 2.0;
        double cy = height / 2.0;
        
        for (int r = 0; r < height; r++) {
            int row = startY + r;
            if (row < 0 || row >= sceneH) continue;
            for (int c = 0; c < width; c++) {
                int col = startX + c;
                if (col < 0 || col >= sceneW) continue;
                double angle = Math.atan2(r - cy, c - cx);
                // Map angle from [-PI, PI] to [0, 1]
                double t = (angle + Math.PI) / (2 * Math.PI);
                bg[row * sceneW + col] = interpolate(colorStart, colorEnd, t);
            }
        }
        scene.setDirty(true);
    }
}
