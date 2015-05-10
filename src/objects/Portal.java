package Jose.src.objects;

import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import Jose.src.util.View;

/**
 * Class representing a portal that is used to end the level and
 * start the next one (is possible).
 */
public class Portal
{
    /**
     * Coordinates of the portal.
     */
    private float x, y;

    /**
     * Dimensions of the portal.
     */
    private float width, height;

    /**
     * Rectangle representing the porta, it's dimensions and position
     * won't change, so there is no need to create it on every get_bounds()
     * call.
     */
    private Rectangle portal;

    /**
     * Reference to the current level's view (camera).
     */
    private View view;

    /**
     * Constructor.
     * @param pos_x X axis coordinate.
     * @param pos_y Y axis coordinate.
     */
    public Portal(float pos_x, float pos_y, View v)
    {
        x = pos_x;
        y = pos_y;
        width = 70;
        height = 140;
        portal = new Rectangle(x, y, width, height);
        view = v;
    }

    /**
     * Draws the portal.
     * @param g The game's graphics context.
     */
    public void draw(Graphics g)
    {
        Rectangle tmp_rect = new Rectangle(x - view.x, y - view.y,
                width, height);
        Color tmp = g.getColor(); // Backup.
        g.setColor(Color.pink);
        g.fill(tmp_rect);
        g.setColor(tmp);
    }

    public Rectangle get_bounds()
    {
        return portal;
    }
}
