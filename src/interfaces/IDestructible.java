package Jose.src.interfaces;

/**
 * Interface used by coins and platforms with limited
 * life span.
 * @author Dzejrou
 */
public interface IDestructible
{
    /**
     * Returns true if the object is supposed to be destroyed,
     * false otherwise.
     */
    boolean is_destroyed();

    /**
     * Marks this object for destruction.
     */
    void destroy();
}
