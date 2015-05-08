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
 * The robot enemy, walks around and when the player is near,
 * charges at him.
 * @author Dzejrou
 */
public class Robot extends Character
{
    /**
     * Enum denoting the Robot's state.
     */
    private enum STATE { ALIVE, DEAD, CHARGING }

    /**
     * Current state of the Robot.
     */
    private STATE curr_state;

    /**
     * Reference to the player object.
     */
    private Player player;

    /**
     * Animations of the Robot.
     */
    private Animation anim_walk_left, anim_walk_right, anim_dead;

    /**
     * Image arrays used to create the animations. 
     */
    private Image[] im_walk_left, im_walk_right, im_dead;

    /**
     * Speed of the Robot (moves only vertically).
     */
    private final float speed = 0.3f;

    /**
     * Amount of steps walked in the current direction.
     */
    private int step_count;

    /**
     * Maximum amount of steps in one direction.
     */
    private final int max_steps = 100;

    /**
     * Robot's walking right sprite is offset by this amount to the left,
     * so the offset_x for hitboxes will need to alternate between 0 and 48
     * when the robot turns.
     */
    private final float sprite_diff = 48.f;

    /**
     * Counter used to check the time the Robot is lying
     * dead on the floor.
     */
    private int death_counter;

    /**
     * Max amount of frames lying dead on the floor.
     */
    private int death_max = 100;

    /**
     * Constructor, set's all necessary attributes and calls Character's constructor.
     * @param m Reference to the level's tile map.
     * @param pos_x X axis starting coordinate.
     * @param pos_y Y axis starting coordinate.
     * @param v Reference to the game's view.
     * @param debug Reference to the debug variable.
     * @param p Reference to the player.
     */
    public Robot(TiledMap m, float pos_x, float pos_y, View v, Player p)
    {
        super(m, pos_x, pos_y, v);
        player = p;
        curr_state = STATE.ALIVE;
        step_count = 0;

        // Load all images.
        try
        {
            im_walk_left = new Image[]{
                    new Image("resources/sprites/enemies/robot/robot_w1.png").
                        getScaledCopy(64.f / 128.f),
                    new Image("resources/sprites/enemies/robot/robot_w2.png").
                        getScaledCopy(64.f / 128.f),
                    new Image("resources/sprites/enemies/robot/robot_w3.png").
                        getScaledCopy(64.f / 128.f)};

            im_walk_right = new Image[]{
                    im_walk_left[0].getFlippedCopy(true, false),
                    im_walk_left[1].getFlippedCopy(true, false),
                    im_walk_left[2].getFlippedCopy(true, false)};

            im_dead = new Image[]{
                new Image("resources/sprites/enemies/robot/robot_dead.png").
                        getScaledCopy(64.f / 128.f)};
        }
        catch(SlickException ex)
        {
            ex.getMessage();
        }

        // Setup the animations.
        int[] durations = {100, 100, 100};
        anim_walk_left = new Animation(im_walk_left, durations, false);
        anim_walk_right = new Animation(im_walk_right, durations, false);

        int[] duration = {100};
        anim_dead = new Animation(im_dead, duration, false);

        curr_anim = anim_walk_left;
        curr_anim.setLooping(true);
        curr_anim.start();

        // Setting other attributes.
        width = 64;
        height = 146;
        offset_x = sprite_diff;
        offset_y = 12.f;
        curr_dir = DIRECTION.LEFT;
    }

    @Override
    /**
     *
     */
    public void update(long delta)
    {
        switch(curr_state)
        {
            case ALIVE:
                update_alive(delta);
                break;
            case DEAD:
                update_dead();
                break;
            case CHARGING:
                break;
        }
    }

    @Override
    /**
     *
     */
    public void draw(Graphics g)
    {
        // Do not render if not in the view.
        if(!view.contains(get_bounds()))
            return;

        curr_anim.draw(x - view.x, y - view.y);

        if(curr_state != STATE.DEAD && Character.debug)
            draw_debug(g);
    }

    @Override
    /**
     *
     */
    public boolean is_dead()
    {
        return curr_state == STATE.DEAD && death_counter >= death_max;
    }

    @Override
    /**
     *
     */
    public void die()
    {
        y += height / 2; // Lie on the floor.
        curr_state = STATE.DEAD;
        curr_anim = anim_dead;
    }

    /**
     *
     */
    private void update_alive(long delta)
    {
        // Gravity.
        if(can_move_to(x, y + speed * delta))
            y += speed * delta;
        curr_anim.update(delta);

        float mov_x = speed * delta * get_direction_modifier();
        if(can_move_to(x + mov_x, y))
        {
            turn();
            x += mov_x;
        }
        else
            turn();

        // Collisions with the player.
        if(weak_hitbox().intersects(player.get_bounds()))
            die(); // Jumped on from the top.

        if(!is_dead() && main_hitbox().intersects(player.get_bounds()))
            player.die(); // Other collision kills the player.
    }

    private void update_dead()
    {
        death_counter++;
    }

    /**
     *
     */
    private void draw_debug(Graphics g)
    {
        Rectangle main = main_hitbox();
        Rectangle weak = weak_hitbox();
        main.setX(main.getX() - view.x);
        main.setY(main.getY() - view.y);
        weak.setX(weak.getX() - view.x);
        weak.setY(weak.getY() - view.y);

        Color tmp = g.getColor();
        g.setColor(Color.red);
        g.draw(main);
        g.setColor(Color.green);
        g.draw(weak);
        g.setColor(tmp);
    }

    /**
     *
     */
    private void turn()
    {
        step_count++;
        if(step_count >= max_steps)
        {
            if(curr_dir == DIRECTION.LEFT)
            {
                offset_x = 0.f;
                x += sprite_diff; // Damn this sprite!
                curr_dir = DIRECTION.RIGHT;
                curr_anim = anim_walk_right;
            }
            else
            {
                x -= sprite_diff;
                offset_x = sprite_diff;
                curr_dir = DIRECTION.LEFT;
                curr_anim = anim_walk_left;
            }
            step_count = 0;
        }
    }

    /**
     *
     */
    private float get_direction_modifier()
    {
        if(curr_dir == DIRECTION.LEFT)
            return -1.f;
        else
            return 1.f;
    }

    /**
     * Returns the main hitbox which kills the player upon
     * contact.
     */
    private Rectangle main_hitbox()
    {
        return new Rectangle(x + offset_x, y + offset_y + 5.f,
                width, height - 5.f);
    }

    /**
     * Returns the secondary hitbox that kills the robot upon
     * contact with the player.
     */
    private Rectangle weak_hitbox()
    {
        return new Rectangle(x + offset_x, y + offset_y, width, 5.f);
    }
}
