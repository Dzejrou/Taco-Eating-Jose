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

import Jose.src.View;

public class Player extends Character
{
    private enum STATE { MOVING, JUMPING, DEBUG, STANDING, FALLING }
    private enum DIRECTION { RIGHT, LEFT, NONE }
    private STATE curr_state;
    private DIRECTION curr_dir;
    private Animation anim_left, anim_right, anim_jump_right,
            anim_jump_left, anim_stand, anim_hit;
    private Image[] im_left, im_right, im_jump_right, im_jump_left,
            im_stand, im_hit;
    private Input input;
    private float speed_x, speed_y, jump_distance;
    private View view;
    private boolean falling_from_jump, debug;

    private final float max_jump_height = 0.5f;
    private final float default_jump_speed = 0.5f;
    private final float jump_diff = 0.01f;

    public Player(TiledMap m, float pos_x, float pos_y, Input i, View v)
    {
        super(m, pos_x, pos_y);
        input = i;
        view = v;
        jump_distance = 0.f;
        speed_x = 0.3f;
        speed_y = default_jump_speed;
        curr_state = STATE.STANDING;
        curr_dir = DIRECTION.NONE;
        falling_from_jump = false;
        debug = false;

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

        curr_anim = anim_stand;
        curr_anim.setLooping(true);
        curr_anim.start();

        width = 96;
        height = 108;
        offset_x = 16; // Bounding box is in the middle of the player sprite.
        offset_y = 20; // And a bit below of the top of the head.
        System.out.println("PLR = [" + width + ", " + height + "]");
    }

    public void update(long delta)
    {
        if(debug && input.isKeyDown(input.KEY_P))
        {
            x = 100;
            y = 400;
            view.x = 0;
            view.y = 0;
        }
        else if(debug)
        {
            if(input.isKeyDown(input.KEY_K))
                y += 10;
            if(input.isKeyDown(input.KEY_I))
                y -= 10;
            if(input.isKeyDown(input.KEY_L))
                x += 10;
            if(input.isKeyDown(input.KEY_J))
                x -= 10;
            if(input.isKeyDown(input.KEY_N))
                curr_state = STATE.DEBUG;
            if(input.isKeyDown(input.KEY_M))
                curr_state = STATE.STANDING;
        }
        if(input.isKeyDown(input.KEY_H))
            debug = false;
        else if(input.isKeyDown(input.KEY_G))
            debug = true;

        update_state(delta);
        update_movement(delta);
        
        view.move(x, y);
        curr_anim.update(delta);
    }
    
    /**
     *
     */
    private void update_state(long delta)
    {
        if(curr_state != STATE.FALLING /* Fall down. */
        && curr_state != STATE.JUMPING
        && curr_state != STATE.DEBUG
        && can_move_to(x, y + speed_y * delta))
        {
            curr_state = STATE.FALLING;
        }

        switch(curr_state)
        {
            case STANDING:
                if(input.isKeyDown(input.KEY_A))
                {
                    curr_state = STATE.MOVING;
                    curr_dir = DIRECTION.LEFT;
                    curr_anim = anim_left;
                }
                else if(input.isKeyDown(input.KEY_D))
                {
                    curr_state = STATE.MOVING;
                    curr_dir = DIRECTION.RIGHT;
                    curr_anim = anim_right;
                }
                else if(input.isKeyDown(input.KEY_SPACE))
                {
                    curr_state = STATE.JUMPING;
                    if(curr_dir == DIRECTION.LEFT)
                        curr_anim = anim_left;
                    else
                        curr_anim = anim_right;

                    // Just to be sure the values are right.
                    speed_y = default_jump_speed;
                    jump_distance = 0f;
                }
                break;
            case MOVING:
                // Change the direction if necessary.
                if(curr_dir != DIRECTION.LEFT && input.isKeyDown(input.KEY_A))
                {
                    curr_dir = DIRECTION.LEFT;
                    curr_anim = anim_left;
                }
                else if(curr_dir != DIRECTION.RIGHT &&
                        input.isKeyDown(input.KEY_D))
                {
                    curr_dir = DIRECTION.RIGHT;
                    curr_anim = anim_right;
                }

                // Stopped moving.
                if(curr_dir == DIRECTION.LEFT && !input.isKeyDown(input.KEY_A)
                || curr_dir == DIRECTION.RIGHT /* Direction must match. */
                && !input.isKeyDown(input.KEY_D))
                {
                    curr_state = STATE.STANDING;
                    curr_dir = DIRECTION.NONE;
                    curr_anim = anim_stand;
                }

                // Preserve the direction.
                if(input.isKeyDown(input.KEY_SPACE))
                {
                    curr_state = STATE.JUMPING;

                    // Just to be sure the values are right.
                    speed_y = default_jump_speed;
                    jump_distance = 0f;
                }
                break;
            case JUMPING:
                jump_distance += jump_diff;
                speed_y -= jump_diff; // Slow down the ascending speed.
                if(!input.isKeyDown(input.KEY_SPACE)
                || jump_distance > max_jump_height)
                {
                    curr_state = STATE.FALLING;
                    falling_from_jump = true;
                    jump_distance = 0.f;
                    speed_y = default_jump_speed;
                }

                // Changing direction mid-air, do not allow this
                // while falling.
                if(curr_dir != DIRECTION.RIGHT
                && input.isKeyDown(input.KEY_D))
                {
                    curr_dir = DIRECTION.RIGHT;
                    curr_anim = anim_right;
                }
                else if(curr_dir != DIRECTION.LEFT
                && input.isKeyDown(input.KEY_A))
                {
                    curr_dir = DIRECTION.LEFT;
                    curr_anim = anim_left;
                }
                break;
            case FALLING:
                break;
        }
    }

    private void update_movement(long delta)
    {
        float mov_x = 0.f;
        float mov_y = 0.f;
        float modifier;

        if(curr_dir == DIRECTION.LEFT)
            modifier = -1.f;
        else if(curr_dir == DIRECTION.RIGHT)
            modifier = 1.f;
        else
            modifier = 0.f; // Will jump/fall straight up/down.

        switch(curr_state)
        {
            case FALLING:
                mov_y += speed_y * delta;
                if(falling_from_jump)
                    mov_x += speed_x * delta * modifier;
                else
                { // Walking off a ledge.
                    mov_x += speed_x * delta * modifier / 2;
                }
                break;
            case MOVING:
                // Modifies the direction of movement.

                mov_x += speed_x * delta * modifier;
                break;
            case JUMPING:
                mov_y += speed_y * delta * -1;
                mov_x += speed_x * delta * modifier;
                break;
            case STANDING:
                // TODO: Moving platforms?
                break;
        }

        // Collision detection etc.
        if(can_move_to(x + mov_x, y + mov_y))
        {
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
        { // Slide down the wall.
            speed_y = default_jump_speed / 2; // Account for the wall.
            curr_state = STATE.FALLING;
        }
        
        if(curr_state == STATE.FALLING && !can_move_to(x, y + mov_y)) // Obstructed.
        {
            curr_anim = anim_stand;
            curr_state = STATE.STANDING;
            falling_from_jump = false;
        }
    }

    /**
     * Method that is used to draw the player's current
     * animation and all other necessary sprites and/or
     * text.
     * @param g The game's graphics context.
     */
    public void draw(Graphics g)
    {

        // RIP 6 hours of my life.
        //curr_anim.draw(x + view.x, y - view.y);

        // Draws the sprite.
        curr_anim.draw(x - view.x, y - view.y);

        /* DEBUG */
        if(debug)
        {
            // Drawing the debug text.
            g.drawString("Player: [" + x + ", " + y + "]", 10, 30);
            g.drawString("View: [" + view.x + ", " + view.y + " | " + view.width + ", " + view.height + "]",10,50);
            g.drawString("State: " + curr_state, 10, 70);
            g.drawString("Direction: " + curr_dir, 170, 70);
            g.drawString("x", x - view.x, y - view.y); // Tracking the x, y coords.

            // Drawing the debug bounds.
            Rectangle tmp_bounds = get_bounds();
            tmp_bounds.setX(tmp_bounds.getX() - view.x);
            tmp_bounds.setY(tmp_bounds.getY() - view.y);
            Color tmp = g.getColor();
            g.setColor(Color.red);
            g.draw(tmp_bounds);
            g.setColor(tmp);

            for(Rectangle b : solid_tiles)
            {
                g.drawRect(b.getX() - view.x, b.getY() - view.y,
                        b.getWidth(), b.getHeight());
            }
        }
    }
}
