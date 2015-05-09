package Jose.src.objects;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import Jose.src.util.View;
import Jose.src.characters.Character;

/**
 * Class representing a movable platform that can carry the
 * player on itself.
 * @author Dzejrou
 */
public class Platform
{
    /**
     * Coordinates of the top left corner of the platform.
     */
    protected float start_x, start_y;

    /**
     * Image of one block in the plarform.
     */
    protected Image block;

    /**
     * Number of blocks that the platform consists of.
     */
    protected int block_count;

    /**
     * Dimensions of one block.
     */
    protected int block_width, block_height;

    /**
     * Reference to the game's view.
     */
    protected View view;

    /**
     * Speed of the platform when moving.
     */
    protected float speed_x, speed_y;

    /**
     * Keeps track of the number of steps this platform has taken.
     */
    protected int step_count;

    /**
     * Maximum amount of steps before this platform dissapears.
     */
    protected int max_step_count;

    /**
     * Constructor, sets all necessary attributes and loads the
     * sprite image.
     * @param pos_x Starting X axis coordinate.
     * @param pos_y Starting Y axis coordinate.
     * @param count Number of blocks that create the platform.
     * @param v Reference to the game's view.
     * @param max Maximum number of steps before direction change.
     * @param mode Mode of movement - vertical/horizontal.
     */
    public Platform(float pos_x, float pos_y, int count, View v, int max,
            String mode)
    {
        // Setup attributes.
        start_x = pos_x;
        start_y = pos_y;
        block_count = count;
        view = v;
        max_step_count = max;
        step_count = 0;

        // Load the image.
        try
        {
            block = new Image("resources/sprites/platform.png");
        }
        catch(SlickException ex)
        {
            System.out.println(ex.getMessage());
            System.exit(1);
        }

        // For the case of rectangular platforms.
        block_width = block.getWidth();
        block_height = block.getHeight();

        // Set the speeds.
        switch(mode)
        {
            case "vertical": // Start movement UP.
                speed_x = 0.f;
                speed_y = -3.f;
                break;
            case "horizontal": // Start movement LEFT.
                speed_x = 3.f;
                speed_y = 0.f;
                break;
            default:
                System.out.println("Wrong platform mode: " + mode);
                System.exit(1);
        }
    }

    /**
     * Draws the platform (all blocks).
     * @param g Reference to the game's graphics context.
     */
    public void draw(Graphics g)
    {
        for(int i = 0; i < block_count; i++)
        {
            block.draw(start_x + i * block_width - view.x,
                    start_y - view.y);
        }

        if(Character.debug)
        {
            // Adjust the hitboxes to the view.
            Rectangle tmp_main = get_bounds();
            Rectangle tmp_carry = get_carry_bounds();
            tmp_main.setX(tmp_main.getX() - view.x);
            tmp_main.setY(tmp_main.getY() - view.y);
            tmp_carry.setX(tmp_carry.getX() - view.x);
            tmp_carry.setY(tmp_carry.getY() - view.y);

            // Remember, backup the color!
            Color tmp = g.getColor();
            g.setColor(Color.blue);
            g.draw(tmp_main);
            g.setColor(Color.green);
            g.draw(tmp_carry);
            g.setColor(tmp);
        }
    }

    /**
     * Updates the platform (movement).
     */
    public void update()
    { // Updated even if out of view, so that it can simulate the
      // movement.
        step_count++;
        if(step_count >= max_step_count)
        { // Turn back.
            speed_x *= -1.f;
            speed_y *= -1.f;
            step_count = 0;
        }

        // Not using delta to make this always go
        // the same distance.
        start_x += speed_x;
        start_y += speed_y;
    }

    /**
     * Returns the collision hitbox of this platform.
     */
    public Rectangle get_bounds()
    {
        return new Rectangle(start_x, start_y, block_count * block_width,
                block_height);
    }

    /**
     * Returns a rectangle which when stood on carries the player with the
     * platform.
     */
    public Rectangle get_carry_bounds()
    {
        return new Rectangle(start_x, start_y - block_height,
                block_count * block_width, block_height);
    }

    /**
     * Returns the horizontal speed, used to carry the player.
     */
    public float speed_x()
    {
        return speed_x;
    }

    /**
     * Returns the vertical speed, used to carry the player.
     */
    public float speed_y()
    {
        return speed_y;
    }
}
