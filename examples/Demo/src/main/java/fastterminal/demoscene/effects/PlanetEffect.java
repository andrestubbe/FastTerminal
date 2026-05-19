package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🪐 3D Planet Shader effect.
 * Projects 2D grid coordinates onto a 3D hemisphere, computes rotational textures,
 * and applies real-time diffuse Lambertian bump-shading from a moving light source.
 */
public class PlanetEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double rotation = 0.0;
    private double lightAngle = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        rotation = frameIndex * 0.02;
        lightAngle = frameIndex * 0.04;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double radius = Math.min(width, height) * 0.4;
        double aspect = 2.0; // Aspect ratio adjustment

        // Moving 3D Light vector
        double lx = Math.cos(lightAngle);
        double ly = Math.sin(lightAngle * 0.5);
        double lz = 1.0; // Pointing slightly from the front
        double len = Math.sqrt(lx * lx + ly * ly + lz * lz);
        lx /= len; ly /= len; lz /= len;

        for (int y = 0; y < height; y++) {
            double dy = (y - centerY) * aspect;

            for (int x = 0; x < width; x++) {
                double dx = x - centerX;

                // Sphere intersection check
                double distSq = dx * dx + dy * dy;
                if (distSq <= radius * radius) {
                    // 1. Calculate 3D surface normal (z points outwards)
                    double sz = Math.sqrt(radius * radius - distSq);
                    double nx = dx / radius;
                    double ny = dy / radius;
                    double nz = sz / radius;

                    // 2. Lambertian Diffuse Light calculation (Dot Product)
                    double dot = nx * lx + ny * ly + nz * lz;
                    double diffuse = Math.max(0.0, dot);

                    // Add a tiny ambient light to see the dark side
                    double lightIntensity = 0.05 + 0.95 * diffuse;

                    // 3. Spherical coordinates mapping for texture (longitude/latitude)
                    double u = Math.atan2(nz, nx) + rotation;
                    double v = Math.acos(ny);

                    // Procedural continental noise texture using harmonic sines
                    double noise = Math.sin(u * 6.0) * Math.cos(v * 6.0)
                                 + 0.5 * Math.sin(u * 12.0) * Math.cos(v * 12.0);

                    int r, g, b;
                    if (noise > 0.15) {
                        // Continent land mass (premium emerald/forest green)
                        r = (int) (34 * lightIntensity);
                        g = (int) (197 * lightIntensity);
                        b = (int) (94 * lightIntensity);
                    } else if (noise > 0.0) {
                        // Shallows/beaches (golden sand)
                        r = (int) (234 * lightIntensity);
                        g = (int) (179 * lightIntensity);
                        b = (int) (8 * lightIntensity);
                    } else {
                        // Oceanic deep water (sleek deep blue)
                        r = (int) (29 * lightIntensity);
                        g = (int) (78 * lightIntensity);
                        b = (int) (216 * lightIntensity);
                    }

                    int color = (r << 16) | (g << 8) | b;
                    
                    // High specular highlight core
                    char glyph = (diffuse > 0.8) ? '█' : (diffuse > 0.55) ? '▓' : (diffuse > 0.3) ? '▒' : '░';
                    canvas.writeCell(x, y, glyph, color, 0x010103);
                } else {
                    // Soft distant background stars
                    if ((x * 17 + y * 23) % 43 == 0) {
                        canvas.writeCell(x, y, '·', 0x222233, 0x000000);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🪐 Rotating 3D Planet Shader";
    }
}
