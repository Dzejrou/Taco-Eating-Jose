package Jose.src.util;

import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

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
     * Checks the bounds, this might result in a part of the map
     * on the right end not being visible - maps have to align
     * to a multiple of the view's width!
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
     * Moves the view vertically up or down by it's height.
     * @param mod Direction modifier.
     */
    public void move_vertically(int mod)
    {
        // Double height for the new position and the actual height.
        if(y + height * mod + height <= map_height
        && y + height * mod >= 0) // Can move.
            y += height * mod;
    }

    /**
     * Moves the view to a given coordinate.
     * At the moment the vertical movement has been changed
     * to jump by the view's height - move one window up, but
     * the pos_y argument has been left here for the possibility
     * of a vertically oriented level - like a climbing one -
     * where the camera follows the player up and down, instead
     * of left and right.
     * @param pos_x X axis coordinate.
     * @param pos_y Y axis coordinate - NOT USED.
     */
    public void move(float pos_x, float pos_y)
    {
        move_horizontally(pos_x);
    }

    /**
     * Checks if a given hitbox is inside the view and therefore
     * should be updated and/or rendered.
     * @param other Hitbox to be checked (rectangle).
     * @return True if the given hitbos is inside the view,
     *         false otherwise.
     */
    public boolean contains(Shape other)
    {
        return new Rectangle(x, y, width, height).intersects(other);
    }
}
