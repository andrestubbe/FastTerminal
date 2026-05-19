package fastterminal.demoscene;

import fastterminal.FastTerminalScene;

/**
 * Common modular interface for all procedurally generated high-performance demoscene effects.
 */
public interface DemosceneEffect {
    
    /**
     * Initializes the effect state with the given grid dimensions.
     */
    void init(int width, int height);
    
    /**
     * Updates internal physics, mathematical states, or particle matrices.
     * 
     * @param time Total elapsed time in seconds since the effect started.
     * @param deltaTime Elapsed time in seconds since the last frame.
     */
    void update(double time, double deltaTime);
    
    /**
     * Draws the calculated state directly onto the scene viewport.
     */
    void render(FastTerminalScene canvas);
    
    /**
     * Returns the human-readable premium title of this demoscene effect.
     */
    String getName();
}
