package Jose.src;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.ScalableGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.Input;
import org.newdawn.slick.Color;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import Jose.src.util.View;
import Jose.src.characters.Character;
import Jose.src.characters.Player;
import Jose.src.characters.Boo;
import Jose.src.characters.Robot;
import Jose.src.objects.Coin;
import Jose.src.objects.Platform;
import Jose.src.objects.Portal;

/**
 * Main game class, updates and renders the game, selects levels.
 */
public class JoseMain extends BasicGame
{
    /**
     * Enum that denotes the game's state.
     */
    private enum GAME_STATE { RUNNING, END, WON }

    /**
     * The game's current state.
     */
    private GAME_STATE curr_state;

    /**
     * Reference to the current level's map.
     */
    private TiledMap map;

    /**
     * List containing all of the current level's characters.
     */
    private List<Character> characters;

    /**
     * Current level's view (camera).
     */
    private View view;

    /**
     * Reference to the player (outside of characters).
     */
    private Player player;

    /**
     * Window dimensions.
     */
    private static int window_width, window_height;

    /**
     * Current level number (for map choice).
     */
    public int current_level;

    /**
     * Total number of levels accessible, increment this number after
     * creating a level.
     */
    public final int levels_total = 2;

    /**
     * Reference to the game container.
     */
    private GameContainer game_container;

    /**
     * Used to preserve last level's score, so that when the player dies,
     * his score gets reset to this value.
     */
    private int player_score;

    /**
     * Indicates if the player can enter the debug mode.
     */
    private final boolean debug_possible = true;

    /**
     * Portal used to end the level.
     */
    private Portal portal;

    /**
     * Constructor, sets all necessary attributes.
     */
    public JoseMain()
    {
        super("Taco Eating Jose");

        characters = new ArrayList<Character>();
    }

    /**
     * Main method of the game, creates an instance of the JoseMain
     * object and runs it.
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        window_width = 800;
        window_height = 600;
        while(true)
        {
            try
            {
                AppGameContainer game = new AppGameContainer(new ScalableGame(
                            new JoseMain(), window_width, window_height));
                game.setDisplayMode(window_width, window_height, false);
                game.setTargetFrameRate(60);
                game.setVSync(true);
                game.setShowFPS(false);
                game.start();
            }
            catch(SlickException ex)
            {
                System.out.println(ex.getMessage());
            }
        }
    }

    @Override
    /**
     * Initializes the game object by calling init_level on the
     * starting level.
     * @param cont The game container.
     * @throws SlickException When error occurs during level load.
     */
    public void init(GameContainer cont) throws SlickException
    {
        game_container = cont;
        player_score = 0;
        curr_state = GAME_STATE.RUNNING;
        current_level = 1;
        init_level(cont, current_level);
    }

    @Override
    /**
     * Updates the game on each frame.
     * @param cont The game container.
     * @param i Time passed since the last update call.
     * @throws SlickException When error occurs during GameContainer
     *                        manipulation.
     */
    public void update(GameContainer cont, int i) throws SlickException
    {
        // Escape key - TODO: Implement menu.
        if(cont.getInput().isKeyDown(Input.KEY_ESCAPE))
            System.exit(0);

        // Update characters.
        for(Character c : characters)
            c.update(i);

        // End the game if necessary.
        if(player.is_dead() && curr_state != GAME_STATE.WON)
            curr_state = GAME_STATE.END;

        if(curr_state == GAME_STATE.END && cont.getInput().
                isKeyDown(Input.KEY_ENTER))
        {
            curr_state = GAME_STATE.RUNNING;
            init_level(cont, current_level); // Reload the level.
        } else if(curr_state == GAME_STATE.WON && cont.getInput().
                isKeyDown(Input.KEY_ENTER))
        {
            curr_state = GAME_STATE.RUNNING;
            current_level = 1;
            player_score = 0;
            init_level(cont, current_level); // Reload the level.
        }

        // Check for dead characters.
        Iterator<Character> it = characters.iterator();
        while(it.hasNext())
        {
            Character c = it.next();

            // RIP.
            if(c.is_dead())
                it.remove();
        }

        // Turns debug mode ON if possible.
        if(debug_possible && cont.getInput().isKeyDown(Input.KEY_H))
            Character.debug = false;
        else if(debug_possible && cont.getInput().isKeyDown(Input.KEY_G))
            Character.debug = true;

        // Level finnished?
        if(portal.get_bounds().intersects(player.get_bounds()))
        {
            player_score = player.get_score(); // Preserve the score!
            next_level();
        }
    }

    @Override
    /**
     * Renders all of the game's tiles, characters and objects.
     * @param cont The game container.
     * @param g The game's graphics context.
     * @throws SlickException When error occurs during graphis drawing.
     */
    public void render(GameContainer cont, Graphics g) throws SlickException
    {
        Color tmp = g.getColor(); // Color backup for drawing.
        switch(curr_state)
        {
            case RUNNING:
                render_running(g);
                break;
            case END:
                render_running(g);
                g.setColor(Color.black);
                g.drawString("Score: " + player_score, 350, 300);
                g.drawString("YOU LOST!", 350, 325);
                g.drawString("Press enter to restart the level or escape to leave the game.", 150, 350);
                g.setColor(tmp);
                break;
            case WON:
                render_running(g);
                g.setColor(Color.black);
                g.drawString("Score: " + player_score, 340, 300);
                g.drawString("YOU WON!", 350, 325);
                g.drawString("Press enter to restart the game or escape to leave the game.", 150, 350);
                g.setColor(tmp);
                break;
        }

        if(Character.debug)
        {
            g.drawString("FPS: " + cont.getFPS(), 10, 10);
        }
    }

    /**
     * Renders the game while in the running state.
     * @param g The game's gics context.
     */
    private void render_running(Graphics g)
    {
        // Calculate map offsets wrt the view.
        int tile_offset_x = (int) - (view.x % map.getTileWidth());
        int tile_offset_y = (int) - (view.y % map.getTileHeight());

        int tile_index_x = (int)(view.x / map.getTileWidth());
        int tile_index_y = (int)(view.y / map.getTileHeight());

        map.render(tile_offset_x, tile_offset_y,
                   tile_index_x, tile_index_y,
                   (int)((view.width - tile_offset_x) / map.getTileWidth() + 1),
                   (int)((view.height - tile_offset_y) / map.getTileHeight() + 1));

        // Draw the end portal.
        portal.draw(g);

        // Draw individual characters.
        for(Character c : characters)
            c.draw(g);
    }

    /**
     * Initializes a given level by loading it's characters, map, view etc.
     * @param cont The game container.
     * @param level_number The number of the level being initialized.
     * @throws SlickException When error occurs during level load.
     */
    private void init_level(GameContainer cont, int level_number) throws SlickException
    {
        if(level_number < 0)
        {
            System.out.println("Wrong level number.");
            System.exit(1);
        }

        String num;
        if(level_number >= 10)
            num = new String("" + level_number);
        else
            num = new String("0" + level_number);

        String level_name = "resources/maps/map" + num + ".tmx";
        map = new TiledMap(level_name);
        view = new View(map, 0, map.getTileHeight() * map.getHeight()
                - window_height, window_width, window_height);
        characters = new ArrayList<Character>(); // Reset the enemies!
        boolean portal_found = false; // Can't check for null on
                                      // consecutive levels.

        int tile_width = map.getTileWidth();
        int tile_height = map.getTileHeight();
        String type;
        for(int i = 0; i < map.getWidth(); ++i)
        {
            for(int j = 0; j < map.getHeight(); ++j)
            {
                // Keeping the characters as blocks. (Not visible ingame.)
                type = map.getTileProperty(map.getTileId(i, j, 0),
                        "type", "nil");

                if(type.equals("nil") || type.equals("platform"))
                    continue;

                int x = i * tile_width;
                int y = j * tile_height;
                switch(type)
                {
                    case "player":
                        player = new Player(map, x, y, cont.getInput(), view,
                                get_coins_from_map(map),
                                get_platforms_from_map(map));
                        characters.add(player);
                        player.set_score(player_score);
                        break;
                    case "boo":
                        Boo boo = new Boo(map, x, y, view, player);
                        characters.add(boo);
                        break;
                    case "robot":
                        Robot robot = new Robot(map, x, y, view, player);
                        characters.add(robot);
                        break;
                    case "coin":
                        // DO NOTHING...
                        break;
                    case "portal":
                        portal = new Portal(x, y, view);
                        portal_found = true;
                        break;
                    default:
                        System.out.println("Invalid character type: " + type);
                        System.exit(1);
                }
            }
        }

        if(!portal_found)
        { // Invalid level map...
            System.out.println("[Error] No portal in the level: "
                    + level_number);
            System.exit(1);
        }
    }

    /**
     * Returns all the coins in the map in a List.
     * @param m Reference to the current level's map.
     */
    List<Coin> get_coins_from_map(TiledMap m)
    {
        List<Coin> tmp = new ArrayList<Coin>();
        String value;
        String color;
        int val = 0;

        int tile_width = m.getTileWidth();
        int tile_height = m.getTileHeight();
        for(int i = 0; i < m.getWidth(); ++i)
        {
            for(int j = 0; j < m.getHeight(); ++j)
            {
                int id = m.getTileId(i, j, 0);
                if("coin".equals(m.getTileProperty(id, "type", "nil")))
                {
                    value = m.getTileProperty(id, "value", "1");
                    try
                    {
                        val = Integer.parseInt(value);
                    }
                    catch(NumberFormatException ex)
                    {
                        System.out.println("Error, wrong coin value: " + value
                                + " at ID #" + id);
                        System.exit(1);
                    }
                    
                    // Remember to center the coins!
                    int pos_x = i * tile_width + tile_width / 2;
                    int pos_y = j * tile_height + tile_height / 2;

                    Coin tmp_c;
                    color = m.getTileProperty(id, "color", "nil");
                    switch(color)
                    {
                        case "yellow":
                            tmp_c = new Coin(pos_x, pos_y, val, view,
                                        Color.yellow);
                            tmp.add(tmp_c);
                            break;
                        case "red":
                            tmp_c = new Coin(pos_x, pos_y, val, view,
                                        Color.red);
                            tmp.add(tmp_c);
                            break;
                        case "blue":
                            tmp_c = new Coin(pos_x, pos_y, val, view,
                                        Color.blue);
                            tmp.add(tmp_c);
                            break;
                        default:
                            System.out.println("Invalid coin color: " + color);
                            System.exit(0);
                    }
                }
            
            }
        }
    return tmp;
    }

    /**
     * Returns list of all platforms in a map.
     * @param m The target tile map.
     */
    private List<Platform> get_platforms_from_map(TiledMap m)
    {
        List<Platform> tmp = new ArrayList<Platform>();
        Platform t_plat;
        int tile_width = m.getTileWidth();
        int tile_height = m.getTileHeight();
        String count_str, max_str, mode;
        int count = 0;
        int max = 0;

        for(int i = 0; i < m.getWidth(); ++i)
        {
            for(int j = 0; j < m.getHeight(); ++j)
            {
                // Just for better readability.
                int id = m.getTileId(i, j, 0);
                int pos_x = i * tile_width;
                int pos_y = j * tile_height;

                // Search by attribute in the map.
                if("platform".equals(m.getTileProperty(id, "type", "nil")))
                {
                    count_str = m.getTileProperty(id, "count", "0");
                    max_str = m.getTileProperty(id, "max", "0");
                    mode = m.getTileProperty(id, "mode", "horizontal");
                    try
                    { // Str to int.
                        count = Integer.parseInt(count_str);
                        max = Integer.parseInt(max_str);
                    }
                    catch(NumberFormatException ex)
                    {
                        System.out.println(ex.getMessage());
                        System.exit(1);
                    }
                    
                    t_plat = new Platform(pos_x, pos_y, count, view,
                            max, mode);
                    tmp.add(t_plat);
                }
            }
        }
        return tmp;
    }

    /**
     * Loads the next level of the game.
     * @throws SlickException When error occurs during level load.
     */
    public void next_level() throws SlickException
    {
        if(current_level + 1 >= levels_total)
        { // TODO: Win screen!
            curr_state = GAME_STATE.WON;
            player_score = player.get_score();
            player.die(); // Ha Ha!
            return;
        }

        current_level++;
        init_level(game_container, current_level);
    }

    /**
     * Loads the previous level of the game.
     * @throws SlickException When error occurs during the level load.
     */
    public void prev_level() throws SlickException
    {
        if(current_level - 1 < 0)
        { // Just don't load it and inform the user.
            System.out.println("[Error] Trying to load an invalid level:"
                    + (current_level - 1));
            return;
        }

        current_level--;
        init_level(game_container, current_level);
    }
}
