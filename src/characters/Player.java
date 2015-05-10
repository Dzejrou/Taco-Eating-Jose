package Jose.src.characters;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Animation;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.Color;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import Jose.src.objects.Coin;
import Jose.src.objects.Platform;
import Jose.src.util.View;

/**
 * Player specialization of the Character class.
 * @author Dzejrou
 */
public class Player extends Character
{
    /**
     * Enum that denotes the player's states.
     */
    private enum STATE { MOVING, JUMPING, DEBUG, STANDING, FALLING, DEAD }

    /**
     * Holds a reference to the current state of the player.
     */
    private STATE curr_state;

    /**
     * Animations for the different states.
     */
    private Animation anim_left, anim_right, anim_jump_right,
            anim_jump_left, anim_stand, anim_hit;

    /**
     * Image arrays used to create the animations.
     */
    private Image[] im_left, im_right, im_jump_right, im_jump_left,
            im_stand, im_hit;

    /**
     * Reference to the game's input context.
     */
    private Input input;

    /**
     * Horizontal and vertical speeds.
     */
    private float speed_x, speed_y;

    /**
     * Current distance passed of the jump, used to determine if the player
     * should start falling.
     */
    private float jump_distance;

    /**
     * Maximum jump distance, after this is passed, start falling.
     */
    private final float max_jump_height = 0.5f;

    /**
     * Default jumping speed, used to reset the speed, which is decreased
     * periodically while jumping to achieve an arc affected by gravity.
     */
    private final float default_jump_speed = 0.5f;

    /**
     * Added every jump frame to the jump distance until it passes the
     * maximum jump height.
     */
    private final float jump_diff = 0.01f;

    /**
     * List containing all of the current level's coins.
     */
    private List<Coin> coins;

    /**
     * Stores the score of the players.
     */
    private int score;

    /**
     * Constructor, sets all necessary attributes of the player and creates
     * animations.
     * @param m Reference to the level's map.
     * @param pos_x X axis coordinate of the spawn point.
     * @param pos_y Y axis coordinate of the spawn point.
     * @param i Reference to the game's input context.
     * @param v Reference to the game's view.
     * @param c List holding the coins in the map.
     */
    public Player(TiledMap m, float pos_x, float pos_y, Input i, View v,
            List<Coin> c, List<Platform> p)
    {
        // Set all attributes and call Character's constructor.
        super(m, pos_x, pos_y, v, p);
        input = i;
        jump_distance = 0.f;
        speed_x = 0.3f;
        speed_y = default_jump_speed;
        curr_state = STATE.STANDING;
        curr_dir = DIRECTION.NONE;
        score = 0;
        coins = c;

        // Load all images into animation arrays.
        try
        { // Those sprites are 208x208, downscaling to 64x64.
            im_right = new Image[]{
                new Image("resources/sprites/player/plr_w1.png").getScaledCopy(64.f/208.f),
                new Image("resources/sprites/player/plr_w2.png").getScaledCopy(64.f/208.f),
                new Image("resources/sprites/player/plr_w3.png").getScaledCopy(64.f/208.f),
                new Image("resources/sprites/player/plr_w4.png").getScaledCopy(64.f/208.f)};

            im_left = new Image[]{
                im_right[0].getFlippedCopy(true, false),
                im_right[1].getFlippedCopy(true, false),
                im_right[2].getFlippedCopy(true, false),
                im_right[3].getFlippedCopy(true, false)};

            im_stand = new Image[]{
                new Image("resources/sprites/player/plr_stand.png").getScaledCopy(64.f/208.f)};

            im_hit = new Image[]{
                new Image("resources/sprites/player/plr_hit.png").getScaledCopy(64.f/208.f)};

            im_jump_right = new Image[]{
                new Image("resources/sprites/player/plr_jump.png").getScaledCopy(64.f/208.f)};

            im_jump_left = new Image[]{im_jump_right[0].getFlippedCopy(true, false)};
        }
        catch(SlickException ex)
        {
            System.out.println(ex.getMessage());
        }

        // Set the animations.
        int[] durations = {100, 100, 100, 100};
        anim_right = new Animation(im_right, durations, false); // false == do not auto update
        anim_left = new Animation(im_left, durations, false);

        // Update should not be called on those, so it doesn't matter
        // they have only one image.
        int[] duration = {300};
        anim_jump_right = new Animation(im_jump_right, duration, false);
        anim_jump_left = new Animation(im_jump_left, duration, false);
        anim_stand = new Animation(im_stand, duration, false);
        anim_hit = new Animation(im_hit, duration, false);

        // Initial animation setup.
        curr_anim = anim_stand;
        curr_anim.setLooping(true);
        curr_anim.start();

        // Bounding box setup.
        width = 96;
        height = 108;
        offset_x = 16; // Bounding box is in the middle of the player sprite.
        offset_y = 20; // And a bit below of the top of the head.
    }

    /**
     * Main update method, calls state, movement and animation updates and
     * moves the game's view if necessary.
     * @param delta Time difference from the last update call.
     */
    @Override
    public void update(long delta)
    {
        // Debug mode handling.
        if(input.isKeyDown(input.KEY_H))
            Character.debug = false;
        else if(input.isKeyDown(input.KEY_G))
            Character.debug = true;
        if(Character.debug)
            handle_debug_input();

        // Update the logic of the player.
        update_state(delta);
        update_movement(delta);

        // Did the player fall into lava/water?
        for(Rectangle b : lethal_tiles)
        {
            if(b.intersects(get_bounds()))
                die(); // RIP.
        }

        // Move the view up/down if necessary.
        if(y <= view.y)
            view.move_vertically(-1);
        else if(y >= view.y + view.height)
            view.move_vertically(1);

        // Collect coins!
        update_coins();

        // Update platforms.
        for(Platform p : platforms)
        {
            p.update();

            // Is the player standing on platform?
            if(get_bounds().intersects(p.get_carry_bounds())
            && curr_state != STATE.JUMPING && curr_state != STATE.FALLING)
            { // Jump/fall would get modified.
                // If can't move, just don't.
                if(can_move_to(x + p.speed_x(), y))
                    x += p.speed_x();

                // If can't move, die:)
                if(can_move_to(x, y + p.speed_y()))
                    y += p.speed_y();
                else if(p.speed_y() < 0) // UP.
                    die(); // Squeezed between platform and wall.
            }
        }
        
        // Update the rendering resources of the player.
        view.move(x, y);
        curr_anim.update(delta);
    }
    
    /**
     * Updates the player's state based on his input.
     * @param delta Time difference from the last update call.
     */
    private void update_state(long delta)
    {
        if(curr_state != STATE.FALLING /* Fall down. */
        && curr_state != STATE.JUMPING
        && curr_state != STATE.DEBUG
        && can_move_to(x, y + speed_y * delta) && !can_climb())
        {
            curr_state = STATE.FALLING;
        }

        switch(curr_state)
        {
            case STANDING:
                // Account for possible movement key presses.
                change_direction();

                if(input.isKeyDown(input.KEY_SPACE))
                    jump();
                break;
            case MOVING:
                // Change the direction if necessary.
                change_direction();

                // Stopped moving.
                if(curr_dir == DIRECTION.LEFT && !input.isKeyDown(input.KEY_A)
                || curr_dir == DIRECTION.RIGHT /* Direction must match. */
                && !input.isKeyDown(input.KEY_D))
                    land(); // Should probably rename this.

                // Preserve the direction.
                if(input.isKeyDown(input.KEY_SPACE))
                    jump();
                break;
            case JUMPING:
                jump_distance += jump_diff; // Record the passed distance.
                speed_y -= jump_diff; // Slow down the ascending speed.

                // If the player hits the distance ceiling, start falling to
                // the ground.
                if(!input.isKeyDown(input.KEY_SPACE)
                || jump_distance > max_jump_height)
                    fall();

                // Changing direction mid-air.
                change_direction();
                break;
            case FALLING:
                // Ladders will prohibit falling.
                if(can_climb())
                    land();

                // Changing direction while falling.
                change_direction();
                break;
        }
    }

    /**
     * Updates the player's position depending on his state and collision
     * detection.
     * @param delta Time difference since the last update call.
     */
    private void update_movement(long delta)
    {
        // mov_x and mov_y are the deltas of the player's position.
        float mov_x = 0.f;
        float mov_y = 0.f;

        // Modifier changes the sign of the position differences.
        float modifier = get_direction_modifier();

        // Calculate the values of the position differences mov_c and mov_y.
        switch(curr_state)
        {
            case STANDING:
                // TODO: Moving platforms?
                break;
            case MOVING:
                mov_x += speed_x * delta * modifier;
                break;
            case JUMPING:
                mov_y += speed_y * delta * -1;
                mov_x += speed_x * delta * modifier;
                break;
            case FALLING:
                mov_y += speed_y * delta;
                // Allow small amout of maneuverability.
                mov_x += speed_x * delta * modifier / 2;
                break;
        }

        // Ladders.
        if(curr_state != STATE.DEAD && can_climb())
        {
            if(input.isKeyDown(input.KEY_W))
                mov_y += speed_y * delta * -1;
            else if(input.isKeyDown(input.KEY_S))
                mov_y += speed_y * delta;
        }

        // Collision detection etc.
        apply_movement(mov_x, mov_y);
    }

    /**
     * Movement to the right means increasing the X axis, movement to the
     * left means decreasing the X axis and similarly for the Y axis changes,
     * this method calculated the sign of the position difference variable for
     * the update_movement method.
     */
    private float get_direction_modifier()
    {
        if(curr_dir == DIRECTION.LEFT)
            return -1.f;
        else if(curr_dir == DIRECTION.RIGHT)
            return 1.f;
        else
            return 0.f; // Will jump/fall straight up/down.
    }

    /**
     * Checks if the given movement is possible and if so, applies
     * it to the player.
     * @param mov_x X axis position difference.
     * @param mov_y Y axis position difference.
     */
    private void apply_movement(float mov_x, float mov_y)
    {
        if(can_move_to(x + mov_x, y + mov_y))
        { // No collisions.
            x += mov_x;
            y += mov_y;
        }
        else if(can_move_to(x + mov_x, y))
            // Only one direction move possible.
            x += mov_x;
        else if(can_move_to(x, y + mov_y)
                && mov_y > 0) // Don't slide.
            y += mov_y;
        else if(curr_state == STATE.JUMPING && mov_x != 0)
            fall(); // Slide the wall.

        if(curr_state == STATE.JUMPING && !can_move_to(x, y + mov_y))
            fall(); // Hit ceiling.
        
        if(curr_state == STATE.FALLING && !can_move_to(x, y + mov_y))
            land(); // Obstructed.
    }

    /**
     * Checks if the direction of the player should change and changes it
     * if needed with the appropriate animation and state.
     */
    private void change_direction()
    {
        // Avoid stuttering when both W and A are pressed.
        if(input.isKeyDown(input.KEY_A)
        && input.isKeyDown(input.KEY_D))
            return;

        if(curr_dir != DIRECTION.RIGHT
        && input.isKeyDown(input.KEY_D))
        {
            if(curr_state == STATE.STANDING)
                curr_state = STATE.MOVING;
            curr_dir = DIRECTION.RIGHT;
            curr_anim = anim_right;
        }
        else if(curr_dir != DIRECTION.LEFT
        && input.isKeyDown(input.KEY_A))
        {
            if(curr_state == STATE.STANDING)
                curr_state = STATE.MOVING;
            curr_dir = DIRECTION.LEFT;
            curr_anim = anim_left;
        }
    }

    /**
     * Makes the player fall by changing his state and vertical speed.
     */
    private void fall()
    {
        curr_state = STATE.FALLING;
        speed_y = default_jump_speed * 2 / 3; // Slower falls.
    }

    /**
     * Makes the player jump by changing his state and vertical speed.
     */
    private void jump()
    {
        // This will match animation with
        // the previous movement.
        if(curr_dir == DIRECTION.LEFT)
            curr_anim = anim_left;
        else if(curr_dir == DIRECTION.RIGHT)
            curr_anim = anim_right;
        else
            curr_anim = anim_stand; // Vertical jump

        curr_state = STATE.JUMPING;
        speed_y = default_jump_speed;
        jump_distance = 0f; // Just to be sure.
    }

    /**
     * Makes the player land by changing his state, also used to stop
     * movement.
     * TODO: Possible rename.
     */
    private void land()
    {
        jump_distance = 0.f;
        curr_dir = DIRECTION.NONE;
        curr_anim = anim_stand;
        curr_state = STATE.STANDING;
    }

    /**
     * Method that is used to draw the player's current
     * animation and all other necessary sprites and/or
     * text.
     * @param g The game's graphics context.
     */
    @Override
    public void draw(Graphics g)
    {

        // RIP 6 hours of my life.
        //curr_anim.draw(x + view.x, y - view.y);

        // Draws the sprite.
        curr_anim.draw(x - view.x, y - view.y);

        // Draw the coins.
        for(Coin c : coins)
            c.draw(g);

        // Draw player's score.
        Color tmp = g.getColor();
        g.setColor(Color.black);
        g.drawString("Score: " + score, view.width - 100, 30);
        g.setColor(tmp);

        // Draw the platforms.
        for(Platform p : platforms)
            p.draw(g);

        /* DEBUG */
        if(Character.debug)
            draw_debug_info(g);
    }

    /**
     * Draws all necessary debug info to the screen along
     * with the black background box and solid tile bounding boxes.
     * @param g Reference to the game's graphics context.
     */
    private void draw_debug_info(Graphics g)
    {
        // Draw collision boxes of solid tiles.
        Color tmp = g.getColor(); // Color backup.
        g.setColor(Color.blue);
        for(Rectangle b : solid_tiles)
        {
            g.drawRect(b.getX() - view.x, b.getY() - view.y,
                    b.getWidth(), b.getHeight());
        }

        // Draw collision boxes of ladders.
        g.setColor(Color.magenta);
        for(Rectangle b : climbable_tiles)
        {
            g.drawRect(b.getX() - view.x, b.getY() - view.y,
                    b.getWidth(), b.getHeight());
        }
        g.setColor(tmp);

        // Draws the debug background.
        tmp = g.getColor();
        g.setColor(Color.black);
        g.fillRect(0, 0, 380, 100);
        g.setColor(tmp);

        // Drawing the debug text.
        g.drawString("Player: [" + x + ", " + y + "]", 10, 30);
        g.drawString("View: [" + view.x + ", " + view.y + " | " + view.width + ", " + view.height + "]",10,50);
        g.drawString("State: " + curr_state, 10, 70);
        g.drawString("Direction: " + curr_dir, 170, 70);

        // Drawing the debug bounds.
        Rectangle tmp_bounds = get_bounds();
        tmp_bounds.setX(tmp_bounds.getX() - view.x);
        tmp_bounds.setY(tmp_bounds.getY() - view.y);
        tmp = g.getColor();
        g.setColor(Color.red);
        g.draw(tmp_bounds);
        g.setColor(tmp);
    }

    /**
     * Handles special input while in debug mode, like movement,
     * teleportation and the debug state toggling.
     */
    private void handle_debug_input()
    {
        if(input.isKeyDown(input.KEY_P))
        { // Get to the start.
            x = 100;
            y = 400;
            view.x = 0;
            view.y = 0;
        }
        // 4 directional debug movement.
        if(input.isKeyDown(input.KEY_K))
            y += 10;
        if(input.isKeyDown(input.KEY_I))
            y -= 10;
        if(input.isKeyDown(input.KEY_L))
            x += 10;
        if(input.isKeyDown(input.KEY_J))
            x -= 10;

        // Special debug state, no gravity mode.
        if(input.isKeyDown(input.KEY_N))
            curr_state = STATE.DEBUG;
        if(input.isKeyDown(input.KEY_M))
            curr_state = STATE.STANDING;
    }

    /**
     * Returns true if the player looks at a given point in the map,
     * returns false otherwisei (horizontal check only).
     *
     * @param pos_x X axis coordinate of the target point.
     */
    public boolean looks_at(float pos_x)
    {
        // Boo will only attack if the player turns their back to them.
        if(curr_dir == DIRECTION.NONE)
            return true;

        // Direction in which the point is located matches
        // the direction the player faces => player looks there.
        return pos_x > x && curr_dir == DIRECTION.RIGHT
            || pos_x <= x && curr_dir == DIRECTION.LEFT;
    }

    /**
     * Returns the player's X axis coordinate.
     */
    public float get_x()
    {
        return x;
    }

    /**
     * Returns the player's Y axis coordinate.
     */
    public float get_y()
    {
        return y;
    }

    /**
     * Returns true if the player is dead, false otherwise.
     */
    @Override
    public boolean is_dead()
    {
        return curr_state == STATE.DEAD;
    }

    /**
     * Kills the character.
     */
    @Override
    public void die()
    {
        // Be like a god in the debug mode!
        if(!Character.debug)
            curr_state = STATE.DEAD;
    }

    /**
     * Returns true if the player can climb a ladder
     * at the player's current position, false otherwise.
     */
    private boolean can_climb()
    {
        for(Rectangle r : climbable_tiles)
        {
            if(r.intersects(get_bounds()))
                return true;
        }
        return false;
    }

    /**
     * Checks if the player has collected a coin and if so,
     * destroys it and adds it's value to the player's score.
     */
    private void update_coins()
    {
        Iterator<Coin> it = coins.iterator();
        while(it.hasNext())
        {
            Coin c = it.next();
            if(c.get_bounds().intersects(get_bounds()))
            {
                score += c.get_value();
                c.destroy();
                it.remove();
            }
        }
    }
}
