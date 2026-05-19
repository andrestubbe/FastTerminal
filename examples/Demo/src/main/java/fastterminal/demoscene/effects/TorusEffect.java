package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🍩 3D Torus / Rotating Donut Shader effect.
 * Projects coordinate grid onto a 3D torus, computes surface normals,
 * and performs real-time directional specular light calculations.
 */
public class TorusEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double rotationX = 0.0;
    private double rotationZ = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        rotationX = frameIndex * 0.04;
        rotationZ = frameIndex * 0.02;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // Fill with deep galactic backdrop
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                canvas.writeCell(x, y, ' ', 0, 0x03030A);
            }
        }

        double R1 = 1.0; // torus ring radius
        double R2 = 2.0; // torus center radius
        double K2 = 5.0; // camera distance depth
        double aspect = 2.0;

        // Precompute sines and cosines
        double cosA = Math.cos(rotationX), sinA = Math.sin(rotationX);
        double cosB = Math.cos(rotationZ), sinB = Math.sin(rotationZ);

        // Z-Buffer for painter's depth sorting inside cells
        double[] zBuffer = new double[width * height];

        // 3D Torus math sweeps
        for (double theta = 0; theta < 2 * Math.PI; theta += 0.08) {
            double cosTheta = Math.cos(theta), sinTheta = Math.sin(theta);

            for (double phi = 0; phi < 2 * Math.PI; phi += 0.03) {
                double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);

                // 3D coordinates before rotation
                double circleX = R2 + R1 * cosTheta;
                double circleY = R1 * sinTheta;

                // 3D rotations
                double x3 = circleX * (cosB * cosPhi + sinA * sinB * sinPhi) - circleY * cosA * sinB;
                double y3 = circleX * (sinB * cosPhi - sinA * cosB * sinPhi) + circleY * cosA * cosB;
                double z3 = K2 + cosA * circleX * sinPhi + circleY * sinA;
                double ooz = 1.0 / z3; // depth multiplier

                // Perspective screen project
                int xp = (int) (width / 2.0 + 30.0 * ooz * x3 * aspect);
                int yp = (int) (height / 2.0 + 15.0 * ooz * y3);

                if (xp >= 0 && xp < width && yp >= 0 && yp < height) {
                    int idx = yp * width + xp;

                    if (ooz > zBuffer[idx]) {
                        zBuffer[idx] = ooz;

                        // Calculate normal vector and specular dot product reflection
                        double L = cosPhi * cosTheta * sinB - cosA * cosTheta * sinPhi - sinA * sinTheta 
                                 + cosB * (cosA * sinTheta - cosTheta * sinA * sinPhi);

                        if (L > 0) {
                            // Specular lighting shading factor
                            double shade = L / 1.414;
                            shade = Math.max(0.0, Math.min(1.0, shade));

                            // Bright neon orange / golden ring coloring
                            int r = (int) (245 * shade);
                            int g = (int) (158 * shade);
                            int b = (int) (11 * shade);
                            int color = (r << 16) | (g << 8) | b;

                            char glyph = (shade > 0.8) ? '█' : (shade > 0.5) ? '▓' : (shade > 0.3) ? '▒' : '░';
                            canvas.writeCell(xp, yp, glyph, color, 0x03030A);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🍩 specular 3D Torus Ring";
    }
}
