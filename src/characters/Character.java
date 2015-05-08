package Jose.src.characters;

import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Graphics;

import java.util.List;
import java.util.ArrayList;

import Jose.src.util.View;

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
     * Offset for the bounding box, since it does not always have to be located
     * in the top left corner of the sprite.
     */
    protected float offset_x, offset_y;

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
     * Reference to the current level's map.
     */
    protected TiledMap map;

    /**
     * Enum that denotes the three possible directions.
     */
    protected enum DIRECTION { RIGHT, LEFT, NONE }

    /**
     * Current direction of the character.
     */
    protected DIRECTION curr_dir;

    /**
     * Reference to the game's view object.
     */
    protected View view;

    /**
     * Reference to the main boolean object, true if the debug mode is on,
     * false otherwise.
     */
    private Boolean debug;

    /**
     * Constructor that spawns a new character on a given coordinates and
     * "binds" them to a level map (collision checking etc).
     * @param m Reference to the map of the current level.
     * @param pos_x Starting X axis coordinate.
     * @param pos_y Starting Y axis coordinate.
     */
    public Character(TiledMap m, float pos_x, float pos_y, Boolean d)
    {
        // Setup the common attributes.
        map = m;
        x   = pos_x;
        y   = pos_y;
        debug = d;

        // Generate the array holding all solid tiles.
        solid_tiles = new ArrayList<Rectangle>();
        int tile_width = m.getTileWidth();
        int tile_height = m.getTileHeight();
        for(int i = 0; i < m.getWidth(); ++i)
            for(int j = 0; j < m.getHeight(); ++j)
            {
                // The "false" means default value if the "solid" attribute is not present.
                if("true".equals(m.getTileProperty(m.getTileId(i, j, 1), "solid", "false")))
                {
                    solid_tiles.add(new Rectangle(i * tile_width, j * tile_height, tile_width, tile_height));
                }
            }
    }

    /**
     * Updates all logic (movement, collisions ...) of the character.
     * @param delta Time elapsed from the last update call.
     */
    public abstract void update(long delta);

    /**
     * Draws the character (and possible debug info).
     * @param g Reference to the game's graphics context.
     */
    public abstract void draw(Graphics g);

    /**
     * Returns the bounding rectangle of this character, used for
     * collision detection.
     * @return New instance of the class Rectangle that has the same
     *         position and size as the character.
     */
    protected Rectangle get_bounds()
    {
        return new Rectangle(x + offset_x, y + offset_y, width, height);
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
        // Level bounds.
        if(x < 0 - width || x > map.getWidth() * map.getTileWidth()
        || y < 0)
            return true;

        // Get the bounds of the character.
        Rectangle tmp = get_bounds();
        tmp.setX(x + offset_x); // Offset to the center from the coordinate.
        tmp.setY(y + offset_y);

        // Checks solid tile collisions.
        for(Rectangle bounds : solid_tiles)
        {
            if(bounds.intersects(tmp))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value of the method is_solid with y coordinate
     * pushed slightly below the character, used for gravity.
     */
    public boolean on_solid_ground()
    {
        return is_solid(x, y + .1f);
    }

    /**
     * Simple negation of the method is_solid, used for better code readability
     * in some places in the code.
     * @param x X axis coordinate.
     * @param y Y axis coordinate.
     */
    public boolean can_move_to(float x, float y)
    {
        return !is_solid(x, y);
    }
}
