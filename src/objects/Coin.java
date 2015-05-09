package Jose.src.objects;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;

import Jose.src.util.View;

/**
 * Class representing a coin in the game, can be collected by the
 * player to increase his score.
 * @author Dzejrou
 */
public class Coin
{
    /**
     * Position coordinates.
     */
    private float x, y;

    /**
     * Boolean value determining if this object should be destroyed.
     */
    private boolean to_be_destroyed;

    /**
     * Color of this coin.
     */
    private Color color;

    /**
     * Reference to the current level's view.
     */
    private View view;

    /**
     * Radius of the circle representing the coin.
     */
    private final float radius = 20.f;

    /**
     * Monetary value of the coin.
     */
    private int value;

    /**
     * Constructor, set's all necessary attributes.
     */
    public Coin(float pos_x, float pos_y, int val, View v, Color col)
    {
        x = pos_x;
        y = pos_y;
        value = val;
        view = v;
        color = col;
    }

    /**
     * Returns true if the object is supposed to be destroyed,
     * false otherwise.
     */
    public boolean is_destroyed()
    {
        return to_be_destroyed;
    }

    /**
     * Marks this object for destruction.
     */
    public void destroy()
    {
        to_be_destroyed = true;
    }

    /**
     * Draws this object.
     * @param g The game's graphics context.
     */
    public void draw(Graphics g)
    {
        // Don't draw outside of the view.
        if(!view.contains(new Circle(x, y, radius)))
            return;

        Color tmp = g.getColor();
        g.setColor(color);
        g.fill(new Circle(x - view.x, y - view.y, radius));
        g.setColor(tmp);
    }

    /**
     * Returns the monetary value of this coin.
     */
    public int get_value()
    {
        return value;
    }

    /**
     * Returns the bounding circle of this coin (for collisions).
     */
    public Circle get_bounds()
    {
        return new Circle(x, y, radius);
    }
}
