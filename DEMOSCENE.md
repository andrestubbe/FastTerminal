# FastTerminal Demoscene Megademo Blueprint & Catalog 🌌

This document catalogs all **35 high-performance procedural demoscene effects** planned for the **FastTerminal** ecosystem. These effects are optimized for the zero-copy True-Color cell compositing engine and are designed to achieve a solid **120 FPS** with minimized ANSI bandwidth.

---

## 🌊 1. Procedural Fluid & Physics Simulation

These effects model physical systems, gas heat dissipation, particle collisions, or grid-based cellular automata.

| Effect | Aesthetics / Look | Technical Implementation |
| :--- | :--- | :--- |
| 🔥 **Doom Fire** | Classic retro flame, organic red/orange/yellow gradients. | 2D integer heat array with bottom-row ignition, upwards wind-shift, and randomized cooling decay. |
| 🟫 **Sand Simulation** | Fall-and-slide dynamics, physical grains piling up. | 2D cellular automaton simulating gravity and diagonal sliding rules for sand grains. |
| 🟥 **ASCII Fluid Simulation** | High-velocity fluid ripples, Navier-Stokes visual flow. | Grid-adapted lightweight Navier-Stokes solver using vector field interpolation. |
| 🟦 **Water Ripple** | Smooth expanding waves, color refraction. | 2D heightmap convolution kernel propagating ripples from cursor clicks. |
| 🌫️ **Fog Noise Field** | Eerie animated clouds, organic density fields. | 2D coherent noise (Perlin/Simplex) with a shifting time-offset. |

---

## 🧊 2. Space & 3D Math Projection

These effects project real 3D vector coordinates onto the 2D terminal cell grid using standard trigonometric rotation matrices and depth ordering.

| Effect | Aesthetics / Look | Technical Implementation |
| :--- | :--- | :--- |
| 🌌 **Warp Starfield** | Stretching stars, high-speed space travel. | 3D space coordinates projected onto 2D viewport, speed-based vector elongation (motion blur). |
| 🧊 **ASCII Voxel Cube** | Solid 3D rotating cube rendered with solid block glyphs. | 3D Euler rotations, painter's depth-sorting algorithm, and character-shading gradients. |
| 🟪 **Neon Wireframe Sphere** | Glowing orbital planet skeleton. | Dynamic spherical latitude/longitude point coordinate rotations. |
| 🟩 **Tron Grid** | Endless prospective virtual horizon. | Perspective projection mapping of infinite horizontal and vertical lines sliding downwards. |
| 🟦 **3D Heightmap Terrain** | Retro flying voxel flight simulator. | Perlin noise heights combined with 3D projection, depth rendering, and dynamic sunlight shading. |

---

## 🌀 3. Plasma & Complex Shader Waves

Mathematical interferences, coordinate transformations, and polar mapping that simulate complex fragment shaders.

| Effect | Aesthetics / Look | Technical Implementation |
| :--- | :--- | :--- |
| 🌈 **Sinusoidal Plasma** | Organic morphing waves, HSL rainbow patterns. | Composite formula `sin(x) + sin(y) + sin(dist)` mapped to HSL-to-RGB color conversions. |
| 🟡 **Sunburst Shader** | Intense radial explosion of dynamic rays. | Polar coordinate angle calculation (`atan2(y, x)`) modulated by time and sine waves. |
| 🟨 **Solar Eclipse Shader** | Golden eclipse glow breaking through a black disk. | Radial circular step-masking, dynamic noise corona, and color-glow blending. |
| 🟫 **Wormhole Zoom** | Tunnel vortex pulling the viewport inwards. | Combined polar coordinate scaling (`log(radius) + time`) and rotation offset. |
| 🌀 **Swirl Distortion** | Spiraling screen distortion. | Coordinate wrapping: `theta = atan2(y,x) + intensity * dist`. |
| 🌫️ **Volumetric Light Rays** | God-rays passing through virtual apertures. | Raymarched lighting vectors computing occlusion shadows over cell grids. |
| 🟧 **ASCII Black Hole** | Gravitational bending, event horizon glow. | Inverse coordinate distortion warping the cell space toward the singularity center. |

---

## 🧬 4. Glitch, Math Curves & Generative Art

Dynamic path-tracking, trigonometric ribbons, recursive equations, and glitch aesthetics.

| Effect | Aesthetics / Look | Technical Implementation |
| :--- | :--- | :--- |
| 💊 **Matrix Rain** | Bright green vertical drops, glowing lead glyphs. | Column timer state arrays, dynamic tail decay, and randomized UTF-8 katakana/unicode drops. |
| 🟪 **Lissajous Ribbons** | Glowing neon bows, oscilloscope patterns. | Parametric equation tracing `x = A * sin(a*t + delta)`, `y = B * sin(b*t)` with a decay tail. |
| 🟦 **Kaleidoscope** | Geometric symmetry, infinite mirrors. | Reflecting single octant coordinate calculations into 6 or 8 segment vectors. |
| 🌪️ **Particle Swarm** | Hundreds of elements orbiting attractors. | Verlet-integration particle dynamics with gravitational attraction points. |
| 🟩 **Electric Arc** | Crackling neon blue lightning strikes. | Randomized walk between start/end points combined with distance glow calculations. |
| 🟥 **Glitch Breaker** | Horizontal splitting, screen tearing. | Randomized block shifts along the X-axis triggered on noise threshold. |
| 🌈 **Color Cycler** | Static procedural background, shifting colors. | Palette rotation through color look-up tables without re-rendering characters. |
| 🟩 **Digital Heartbeat** | Glowing EKG cardiac monitoring spikes. | Time-shifting decay envelopes combined with localized high-frequency noise. |
| 🔷 **Mandelbrot Zoom** | Endless zoom into complex fractals. | Escape-time algorithm `Z = Z^2 + C` running iteration checks per cell coordinate. |
| 🔷 **Hexagon Grid Flow** | Futuristic honeycomb cellular dashboards. | Offset fractional coordinate grids drawing clean hexagons via box-drawing unicode characters. |
| 🧵 **Ribbon Trails** | Waving satin sheets in space. | Interpolating multiple bezier lines with fading alpha shades. |

---

## 🛠️ Megademo Core Directory Architecture Plan

We will organize this megademo suite into a dedicated package namespace under `examples/Demo`:

```
examples/Demo/src/main/java/fastterminal/
 ├── demoscene/
 │    ├── Megademo.java              <-- Master Orchestrator (Auto-cycles every 60s)
 │    ├── DemosceneEffect.java       <-- Common Base Interface
 │    ├── effects/
 │    │    ├── PlasmaEffect.java
 │    │    ├── DoomFireEffect.java
 │    │    ├── MatrixRainEffect.java
 │    │    ├── WarpStarfieldEffect.java
 │    │    └── [Other Effects...]
```

This ensures extreme modularity, zero heap-allocation overhead in main loops, and absolute ease of adding new effects.
