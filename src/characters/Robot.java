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
    private final float speed = 0.2f;

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
    private int death_max = 150;

    /**
     * Keeps track of frame number in the charge for late direction change.
     */
    private int charge_steps;

    /**
     * Maximum amount of steps that the robot will charge for.
     */
    private int max_charge_distance = 300;

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
        super(m, pos_x, pos_y, v, null);
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

    /**
     * Updates the state and logic of the robot.
     * @param delta Time since the last update call.
     */
    @Override
    public void update(long delta)
    {
        // Do not update if not in the view.
        if(!view.contains(get_bounds()))
            return;

        switch(curr_state)
        {
            case ALIVE:
                // Chase the player.
                if(player_near())
                    curr_state = STATE.CHARGING;
                update_alive(delta);
                break;
            case DEAD:
                update_dead();
                break;
            case CHARGING:
                // Stop when the player got away.
                if(charge_steps > max_charge_distance && !player_near())
                {
                    curr_state = STATE.ALIVE;
                    charge_steps = 0;
                }
                charge_steps++;
                update_charging(delta);
                break;
        }
    }

    /**
     * Draws the robot and the debug info if necessary.
     * @param g The game's graphics context.
     */
    @Override
    public void draw(Graphics g)
    {
        // Do not render if not in the view.
        if(!view.contains(get_bounds()))
            return;

        curr_anim.draw(x - view.x, y - view.y);

        if(curr_state != STATE.DEAD && Character.debug)
            draw_debug(g);
    }

    /**
     * Returns true if the robot is dead and necessary time has
     * passed for the body to be deleted.
     */
    @Override
    public boolean is_dead()
    {
        return curr_state == STATE.DEAD && death_counter >= death_max;
    }

    /**
     * Kills the robot:(
     */
    @Override
    public void die()
    {
        y += height / 2; // Lie on the floor.
        curr_state = STATE.DEAD;
        curr_anim = anim_dead;
    }

    /**
     * Special update method for when the robot is alive,
     * updates the movement, collision detection, basic AI
     * and takes in regard gravity.
     * @param delta Time since the last update call.
     */
    private void update_alive(long delta)
    {
        update_gravity(delta);

        // Walks only horizontally.
        float mov_x = speed * delta * get_direction_modifier();

        if(can_move_to(x + mov_x, y))
        { // No obstruction.
            turn();
            x += mov_x;
        }
        else
            turn();

        update_player_collisions();
    }

    /**
     * Special update method to be used when the robot is chasing
     * after the player.
     * @param delta Time since the last update call.
     */
    private void update_charging(long delta)
    {
        update_gravity(delta);

        float mov_x = speed * 2 * delta * get_direction_modifier();

        if(can_move_to(x + mov_x, y))
        { // No obstruction.
            x += mov_x;
        }
        else // Give up the chase.
            curr_state = STATE.ALIVE;

        // Late direction change when player gets over the robot.
        if(charge_steps % 60 == 0) // Every second?
        {
            if(curr_dir == DIRECTION.LEFT && player.get_x() > x + 10)
                change_direction();
            else if(curr_dir == DIRECTION.RIGHT && player.get_x() < x - 10)
                change_direction();
        }

        update_player_collisions();
    }

    /**
     * Checks for collisions between the robot and the player.
     */
    private void update_player_collisions()
    {
        // Collisions with the player.
        if(weak_hitbox().intersects(player.get_bounds())
        && !player.is_dead())
            die(); // Jumped on from the top.

        if(!is_dead() && main_hitbox().intersects(player.get_bounds()))
            player.die(); // Other collision kills the player.
    }

    /**
     * Updates the robot wrt gravity.
     * @param delta Time since the last update call.
     */
    private void update_gravity(long delta)
    {
        if(can_move_to(x, y + speed * delta))
            y += speed * delta;
        curr_anim.update(delta);
    }

    /**
     * Special update method for when the robot is dead, takes care
     * of the death counter representing the body decay.
     */
    private void update_dead()
    {
        death_counter++;
    }

    /**
     * Draws debug info - hitboxes.
     * @param g The game's graphics context.
     */
    private void draw_debug(Graphics g)
    {
        // Get and adjust the hitboxes.
        Rectangle main = main_hitbox();
        Rectangle weak = weak_hitbox();
        main.setX(main.getX() - view.x);
        main.setY(main.getY() - view.y);
        weak.setX(weak.getX() - view.x);
        weak.setY(weak.getY() - view.y);

        // Remember to backup the color!
        Color tmp = g.getColor();
        g.setColor(Color.red);
        g.draw(main);
        g.setColor(Color.green);
        g.draw(weak);
        g.setColor(tmp);
    }

    /**
     * Turns the robot if needed amount of steps has been walked
     * by changing it's direction and applying the sprite offset,
     * since the sprites differ when flipped by a vertical
     * axis.
     */
    private void turn()
    {
        // Make the step.
        step_count++;

        // Check if the distance walked is enough.
        if(step_count >= max_steps)
        {
            change_direction();
            step_count = 0;
        }
    }

    /**
     * Changes the walking direction of the robot.
     */
    private void change_direction()
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
    }

    /**
     * Returns the sign modifier for movement in a certain
     * direction.
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
    { // The 10 & 20 modifiers make sure the robot is unkillable by jumping
      // from the side.
        return new Rectangle(x + offset_x + 10, y + offset_y, width - 20, 5.f);
    }

    /**
     * Returns true if the player is close to the robot signaling
     * that the robot should start charging.
     */
    private boolean player_near()
    {
        return Math.abs(player.get_x() - x) < 250;
    }
}
