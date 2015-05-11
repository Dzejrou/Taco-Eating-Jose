package Jose.src.characters;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.Animation;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.Color;

import Jose.src.util.View;

/**
 * The boo enemy, ghost that only moves when the player
 * is turned away from him.
 * @author Dzejrou
 */
public class Boo extends Character
{
    /**
     * Enum denoting the Boo's state.
     */
    private enum STATE { ACTIVE, INACTIVE, DEAD }

    /**
     * Current state of the Boo.
     */
    private STATE curr_state;

    /**
     * Reference to the player object.
     */
    private Player player;

    /**
     * Boo's sprites.
     */
    private Image im_active_right, im_active_left, im_inactive_right,
            im_inactive_left;

    /**
     * Reference to the current sprite of the Boo.
     */
    private Image curr_im;

    /**
     * Speed of the boo, same horizontal and vertical.
     */
    private final float speed = 0.3f;

    /**
     * Constructor, set's all necessary attributes and calls Character's constructor.
     * @param m Reference to the level's tile map.
     * @param pos_x X axis starting coordinate.
     * @param pos_y Y axis starting coordinate.
     * @param v Reference to the game's view.
     * @param p Reference to the player.
     */
    public Boo(TiledMap m, float pos_x, float pos_y, View v, Player p)
    {
        super(m, pos_x, pos_y, v, null);
        player = p;

        try
        {
            // Scale the images!
            im_active_left = new Image("resources/sprites/enemies/boo/boo_active.png").
                getScaledCopy(64.f / 256.f);

            im_active_right = im_active_left.getFlippedCopy(true, false);

            im_inactive_left = new Image("resources/sprites/enemies/boo/boo_inactive.png").
                getScaledCopy(64.f / 256.f);

            im_inactive_right = im_inactive_left.getFlippedCopy(true, false);
        }
        catch(SlickException ex)
        {
            System.out.println(ex.getMessage());
        }

        // Set the starting sprite and state.
        curr_state = STATE.ACTIVE;
        curr_im = im_inactive_left;
        curr_dir = DIRECTION.LEFT;
        
        // Set dimensions.
        width = 64.f;
        height = 59.f; // Minus 5 for the weak hitbox.
    }

    /**
     * Updates the boo.
     * @param delta Time passed since the last update call.
     */
    @Override
    public void update(long delta)
    {
        // Do not update if the boo is not in the view.
        if(!view.contains(get_bounds()))
            return;

        // Check if the player looks at the boo.
        if(curr_state == STATE.ACTIVE && player.looks_at(x))
            deactivate();
        else if(curr_state == STATE.INACTIVE && !player.looks_at(x))
            activate();
    
        // Movement and collision.
        float plr_x = player.get_x();
        float plr_y = player.get_y();

        // Move if active.
        if(curr_state == STATE.ACTIVE)
        {
            float mov_x;
            float mov_y;

            if(plr_x < x)
                mov_x = -1.f;
            else if(plr_x > x)
                mov_x = 1.f;
            else
                mov_x = 0.f; // Same horizontal coordinate.

            // Note: The +- 10 stops stuttering of the boo when on the same
            // y coordinate as the player.
            if(plr_y + 10< y)
                mov_y = -1.f;
            else if(plr_y - 10 > y)
                mov_y = 1.f;
            else
                mov_y = 0.f; // Same vertical coordinate.

            // No tile collision detection, the Boo can move through walls.
            // (It's a ghost, DUH.)
            x += mov_x * speed * delta;
            y += mov_y * speed * delta;
        }

        // Collisions with the player.
        if(weak_hitbox().intersects(player.get_bounds())
        && !player.is_dead())
            die(); // Jumped on from the top.

        if(!is_dead() && main_hitbox().intersects(player.get_bounds()))
            player.die(); // Other collision kills the player.

        change_direction();
    }

    /**
     * Changes the direction of the boo and his sprite.
     * This will make the boo stare at the player even when
     * it is inactive.
     */
    private void change_direction()
    {
        float plr_x = player.get_x();
        float plr_y = player.get_y();

        if(plr_x < x)
        {
            if(curr_state == STATE.ACTIVE)
                curr_im = im_active_left;
            else
                curr_im = im_inactive_left;
        }
        else
        {
            if(curr_state == STATE.ACTIVE)
                curr_im = im_active_right;
            else
                curr_im = im_inactive_right;
        }
    }

    /**
     * Draws the boo.
     * @param g Reference to the game's graphics context.
     */
    @Override
    public void draw(Graphics g)
    {
        // Do not render if the boo is not in the view.
        if(!view.contains(get_bounds()))
            return;

        curr_im.draw(x - view.x, y - view.y);
        
        if(debug)
            draw_debug(g);
    }

    /**
     * Changes the state to active.
     * Also changes the sprite.
     */
    private void activate()
    {
        curr_state = STATE.ACTIVE;

        if(curr_dir == DIRECTION.LEFT)
            curr_im = im_active_left;
        else
            curr_im = im_active_right;
    }

    /**
     * Changes the state to inactive.
     * Also changes the sprite.
     */
    private void deactivate()
    {
        curr_state = STATE.INACTIVE;

        if(curr_dir == DIRECTION.LEFT)
            curr_im = im_inactive_left;
        else
            curr_im = im_inactive_right;
    }

    /**
     * Returns the main hitbox which kills the player upon
     * contact.
     * @return Hitbox rectangle.
     */
    private Rectangle main_hitbox()
    {
        return new Rectangle(x, y + 5.f, width, height + 5.f);
    }

    /**
     * Returns the secondary hitbox that kills the boo upon
     * contact with the player.
     * The constants on x axis and width are used so that the
     * weak hitbox is in the middle and is less wide than the
     * main hitbox rectangle, so the player cannot kill the boo
     * while jumping upwards (will hit the main hitbox first).
     * @return Weak spot rectangle.
     */
    private Rectangle weak_hitbox()
    {
        return new Rectangle(x + 10.f, y, width - 20.f, 5.f);
    }

    /**
     * Draws hitboxes in the debug mode.
     * @param g The game's graphics context.
     */
    private void draw_debug(Graphics g)
    {
        // Adjust hitboxes to view.
        Rectangle main = main_hitbox();
        Rectangle weak = weak_hitbox();
        main.setX(main.getX() - view.x);
        main.setY(main.getY() - view.y);
        weak.setX(weak.getX() - view.x);
        weak.setY(weak.getY() - view.y);

        // Draw hitboxes.
        Color tmp = g.getColor();
        g.setColor(Color.red);
        g.draw(main);
        g.setColor(Color.green);
        g.draw(weak);
        g.setColor(tmp);
    }

    /**
     * Used to check if this character died.
     * @return True if the boo died, false otherwise.
     */
    public boolean is_dead()
    {
        return curr_state == STATE.DEAD;
    }

    /**
     * Kills the boo.
     */
    public void die()
    {
        curr_state = STATE.DEAD;
    }
}
