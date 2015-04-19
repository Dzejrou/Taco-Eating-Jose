package Jose.src.characters;

import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.Animation;

import java.util.List;
import java.util.ArrayList;

/**
 * Abstract class that is to be extended by all characters in the game.
 * @author Dzejrou
 */
public abstract class Character
{
    /**
     * Position of the character, x represents the X axis, y represents the
     * Y axis.
     */
    protected float x, y;

    /**
     * Width and height of the character.
     */
    protected float width, height;

    /**
     * Special instance of animation all characters need to have, used
     * for rendering and collision detection.
     */
    protected Animation curr_anim;

    /**
     * List of solid block in the current level's map.
     */
    protected List<Rectangle> solid_tiles;

    /**
     * Constructor that spawns a new character on a given coordinates and
     * "binds" them to a level map (collision checking etc).
     * @param m Reference to the map of the current level.
     * @param pos_x Starting X axis coordinate.
     * @param pos_y Starting Y axis coordinate.
     */
    public Character(TiledMap m, float pos_x, float pos_y)
    {
        x   = pos_x;
        y   = pos_y;

        solid_tiles = new ArrayList<Rectangle>();
        int tile_width = m.getTileWidth();
        int tile_height = m.getTileHeight();
        for(int i = 0; i < m.getWidth(); ++i)
        {
            for(int j = 0; j < m.getHeight(); ++j)
            {
                if("true".equals(m.getTileProperty(m.getTileId(i, j, 0), "solid", "false")))
                {
                    solid_tiles.add(new Rectangle(i * tile_width, j * tile_height, tile_width, tile_height));
                }
            }
        }
    }

    /**
     * Returns the bounding rectangle of this character, used for
     * collision detection.
     * @return New instance of the class Rectangle that has the same
     *         position and size as the character.
     */
    protected Rectangle get_bounds()
    {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Method that checks if this character intersects the bounding
     * rectangle of another character.
     * @param other The other character.
     * @return True if the two characters intersect, false otherwise.
     */
    public boolean intersects(Character other)
    {
        return get_bounds().intersects(other.get_bounds());
    }

    /**
     * Method that checks if the tile containing a given point in space
     * is solid.
     * @param x X axis coordinate.
     * @param y Y axis coordinate.
     * @return True if the tile is solid, false otherwise. 
     */
    protected boolean is_solid(float x, float y)
    {
        // Width and height are set from the current animation frame via
        // the get_bounds() method.
        Rectangle tmp = get_bounds();
        tmp.setX(x);
        tmp.setY(y);

        for(Rectangle bounds : solid_tiles)
        {
            if(bounds.intersects(tmp))
                return true;
        }
        return false;
    }
}
