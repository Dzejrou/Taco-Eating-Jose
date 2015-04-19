package Jose.src;

import org.newdawn.slick.tiled.TiledMap;

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

    public View(TiledMap map, float x, float y, float width, float height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        map_width = map.getWidth() * map.getTileWidth();
        map_height = map.getHeight() * map.getTileHeight();
    }

    public void move_horizontally(float pos_x)
    {
        if(pos_x + width / 2 + width > map_width )
            pos_x = map_width - width; // Hit the right wall of the world.
        else if(pos_x - width / 2 <= 0)
            pos_x = 0; // Hit the left wall of the world.
        else
            x = pos_x - width / 2; // Center the view around pos_x.    
    }

    public void move_vertically(float offset)
    { // TODO:
        float tmp_y = y + offset;
        if(tmp_y < 0 || tmp_y + height > map_height) // Do not move it past the edge.
            return;

        y = tmp_y;    
    
    }

    public void move(float pos_x, float pos_y)
    {
        // To move the view, move the map in the opposite direction.
        move_horizontally(pos_x);
        //move_vertically(pos_y);
    }
}
