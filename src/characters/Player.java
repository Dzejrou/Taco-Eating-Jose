package Jose.src.characters;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.Animation;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;

import Jose.src.View;

public class Player extends Character
{
    private enum STATE { MOVING_LEFT, MOVING_RIGHT,
                         JUMPING_RIGHT, JUMPING_LEFT, HIT, STANDING, FALLING }
    private STATE curr_state;
    private Animation anim_left, anim_right, anim_jump_right,
            anim_jump_left, anim_stand, anim_hit;
    private Image[] im_left, im_right, im_jump_right, im_jump_left,
            im_stand, im_hit;
    private Input input;
    private float speed, jump_distance;
    private View view;

    public Player(TiledMap m, float pos_x, float pos_y, Input i, View v)
    {
        super(m, pos_x, pos_y);
        input = i;
        view = v;
        speed = 0.3f;
        curr_state = STATE.STANDING;

        try
        {
            im_right = new Image[]{new Image("resources/sprites/player/plr_w1.png"), new Image("resources/sprites/player/plr_w2.png")};
            im_left = new Image[]{im_right[0].getFlippedCopy(true, false), im_right[1].getFlippedCopy(true, false)};
            im_stand = new Image[]{new Image("resources/sprites/player/plr_stand.png")};
            im_hit = new Image[]{new Image("resources/sprites/player/plr_hit.png")};
            im_jump_right = new Image[]{new Image("resources/sprites/player/plr_jump.png")};
            im_jump_left = new Image[]{im_jump_right[0].getFlippedCopy(true, false)};
        }
        catch(SlickException ex)
        {
            System.out.println(ex.getMessage());
        }

        int[] durations = {100, 100};
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

        // Set the width and height to the standing animation (some frames are a bit higher).
        width = curr_anim.getWidth();
        height = curr_anim.getHeight();
    }

    public void update(long delta)
    {
        // FALLING state has it's own movement and state because of the sprite drawn while falling from a jump.
        if(curr_state != STATE.JUMPING_RIGHT &&
           curr_state != STATE.JUMPING_LEFT &&
           curr_state != STATE.FALLING && !is_solid(x, y + 0.1f))
            y += delta * speed * 3; // Gravity.

        // To keep track for view movement.
        float mov_x = 0;
        float mov_y = 0;

        switch(curr_state)
        {
            case STANDING:
                if(input.isKeyDown(input.KEY_SPACE))
                { // Jump!
                    curr_state = STATE.JUMPING_RIGHT;
                    curr_anim.stop();
                    curr_anim = anim_jump_right;
                    curr_anim.start();

                    jump_distance = 0;
                    mov_y = delta * speed;
                    
                }
                else if(input.isKeyDown(input.KEY_A))
                { // Change direction to left.
                    curr_state = STATE.MOVING_LEFT;
                    curr_anim.stop();
                    curr_anim = anim_left;
                    curr_anim.start();

                    mov_x = delta * speed;
                }
                else if(input.isKeyDown(input.KEY_D))
                {
                    curr_state = STATE.MOVING_RIGHT;
                    curr_anim.stop();
                    curr_anim = anim_right;
                    curr_anim.start();

                    mov_x = delta * speed;
                }
                break;
            case MOVING_RIGHT:
                if(!input.isKeyDown(input.KEY_D))
                { // Stop moving.
                    curr_state = STATE.STANDING;
                    curr_anim.stop();
                    curr_anim = anim_stand;
                    curr_anim.start();
                }
                else if(input.isKeyDown(input.KEY_SPACE))
                { // Jump!
                    curr_state = STATE.JUMPING_RIGHT;
                    curr_anim.stop();
                    curr_anim = anim_jump_right;
                    curr_anim.start();

                    jump_distance = 0;
                    mov_y = delta * speed;
                }
                else if(input.isKeyDown(input.KEY_A))
                { // Change direction to left.
                    curr_state = STATE.MOVING_LEFT;
                    curr_anim.stop();
                    curr_anim = anim_left;
                    curr_anim.start();

                    mov_x = delta * speed;
                }
                else
                    mov_x = delta * speed;
                break;
            case MOVING_LEFT:
                if(!input.isKeyDown(input.KEY_A))
                { // Stop moving.
                    curr_state = STATE.STANDING;
                    curr_anim.stop();
                    curr_anim = anim_stand;
                    curr_anim.start();
                }
                else if(input.isKeyDown(input.KEY_SPACE))
                { // Jump!
                    curr_state = STATE.JUMPING_LEFT;
                    curr_anim.stop();
                    curr_anim = anim_jump_left;
                    curr_anim.start();

                    jump_distance = 0;
                    mov_y = delta * speed * -1;
                }
                else if(input.isKeyDown(input.KEY_D))
                { // Change direction to right.
                    curr_state = STATE.MOVING_RIGHT;
                    curr_anim.stop();
                    curr_anim = anim_right;
                    curr_anim.start();

                    mov_x = delta * speed;
                }
                else
                    mov_x = delta * speed * -1;
                break;
            case JUMPING_RIGHT:
                jump_distance += 0.1;
                if (jump_distance > 8)
                { // Start falling.
                    curr_state = STATE.FALLING;
                }

                mov_y = delta * speed * -1;
                mov_x = delta * speed;
                break;
            case JUMPING_LEFT:
                jump_distance += 0.1;
                if (jump_distance > 8)
                { // Start falling.
                    curr_state = STATE.FALLING;
                }
                mov_y = delta * speed * -1;
                mov_x = delta * speed * -1;
                break;
            case FALLING:
                jump_distance -= delta * speed;
                if(is_solid(x, y - 1))
                { // Stop falling.
                    curr_anim.stop();
                    curr_anim = anim_stand;
                    curr_anim.start();

                    curr_state = STATE.STANDING;
                }
                else if(input.isKeyDown(input.KEY_A))
                    mov_x = delta * speed * -1;
                else if(input.isKeyDown(input.KEY_D))
                    mov_x = delta * speed;

                mov_y = delta * speed;
                break;
            case HIT:
                break;
        }
        
        if((curr_state == STATE.MOVING_LEFT ||
           curr_state == STATE.MOVING_RIGHT) &&
           !is_solid(x + mov_x, y + mov_y - 8))
        {
            x += mov_x;
            y += mov_y;
        }
        else if(curr_state == STATE.JUMPING_RIGHT ||
                curr_state == STATE.JUMPING_LEFT ||
                curr_state == STATE.FALLING)
        {
            if(!is_solid(x + mov_x, y + mov_y - 8))
            {
                x += mov_x;
            }
            y += mov_y;
        }

        view.move(x, y);
        curr_anim.update(delta);
    }
    
    public void draw(Graphics g)
    {
        g.drawString("Player: [" + x + ", " + y + "]", 10, 30);
        g.drawString("View: [" + view.x + ", " + view.y + " | " + view.width + ", " + view.height + "]",10,50);

        Rectangle tmp_bounds = get_bounds();
        tmp_bounds.setX(tmp_bounds.getX() - view.x);
        tmp_bounds.setY(tmp_bounds.getY() - view.y);
        g.draw(tmp_bounds);
        curr_anim.draw(x - view.x, y - view.y);
    }
}
