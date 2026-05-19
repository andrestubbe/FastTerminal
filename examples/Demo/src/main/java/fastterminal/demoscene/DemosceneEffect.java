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
     */
    void update(long frameIndex);
    
    /**
     * Draws the calculated state directly onto the scene viewport.
     */
    void render(FastTerminalScene canvas);
    
    /**
     * Returns the human-readable premium title of this demoscene effect.
     */
    String getName();
}
