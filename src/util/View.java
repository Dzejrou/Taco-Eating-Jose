package Jose.src.util;

import org.newdawn.slick.tiled.TiledMap;

/**
 * The view class represent's a camera following the player,
 * allows camera movement and restriction for update of characters,
 * since only those in the view need to be updated.
 * @author Dzejrou
 */
public class View
{
    /**
     * Variables describing the view's position
     * and sizes.
     */
    public float x, y, width, height;

    /**
     * Variables holding the size of the current
     * level's map.
     */
    private float map_width, map_height;

    /**
     * View class constructor, sets all atributes and calculates the map
     * size.
     * @param map Reference to the level's tile map.
     * @param x X axis coordinate of the view.
     * @param y Y axis coordinate of the view.
     * @param width Width of the view.
     * @param height Height of the view.
     */
    public View(TiledMap map, float x, float y, float width, float height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        map_width = map.getWidth() * map.getTileWidth();
        map_height = map.getHeight() * map.getTileHeight();
    }

    /**
     * Moves the view horizontally to a given coordinate.
     * @param pos_x X axis coordinate.
     */
    public void move_horizontally(float pos_x)
    {
        if(pos_x + width / 2 + width > map_width )
            pos_x = map_width - width; // Hit the right wall of the world.
        else if(pos_x - width / 2 <= 0)
            pos_x = 0; // Hit the left wall of the world.
        else
            x = pos_x - width / 2; // Center the view around pos_x.    
    }

    /**
     * Moves the view to a given coordinate.
     * @param pos_x X axis coordinate.
     * @param pos_y Y axis coordinate - NOT USED.
     */
    public void move(float pos_x, float pos_y)
    {
        move_horizontally(pos_x);
        // Possible TODO: move_horizontally.
    }
}
