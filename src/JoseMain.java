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
 * Game: (visualisation of the running process)
 *      Constructor
 *      Init
 *      Level Loop:
 *          Init Level
 *          Game Loop
 *              Update
 *              Render
 * @author Dzejrou
 */
public class JoseMain extends BasicGame
{
    /**
     * Enum that denotes the game's state.
     */
    private enum STATE { RUNNING, END, WON }

    /**
     * The game's current state.
     */
    private STATE curr_state;

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
     * Used to check if he's dead (to preserve the score in the
     * ENDED state).
     */
    private Player player;

    /**
     * Window dimensions.
     */
    private static int window_width, window_height;

    /**
     * Current level number (for map choice).
     * 0 = Test level, used for debug only and can be messed with.
     * 1 = Starting standard grass level.
     * 2 = Water platform level.
     * 3 = Narrow rock tomb with lava.
     */
    public int current_level;

    /**
     * Total number of levels accessible, increment this number after
     * creating a level.
     * This is used to check if the player is not load an invalid level
     * (negative or non existing positive level number).
     */
    public final int levels_total = 4;

    /**
     * Reference to the game container.
     * Used for misc stuff like getting input where there isn't container
     * as parameter and making it a parameter wouldn't be that good.
     */
    private GameContainer game_container;

    /**
     * Used to preserve last level's score, so that when the player dies,
     * his score gets reset to this value.
     */
    private int player_score;

    /**
     * Indicates if the player can enter the debug mode.
     * Set this to false in release version, false otherwise.
     * Without this set, the "turn debug on" key (G) will be ignored
     * and as a result of this, every other debug key will.
     */
    private final boolean debug_possible = true;

    /**
     * Portal used to end the level.
     */
    private Portal portal;

    /**
     * Keeps track of the number of frames since the last level change,
     * used for debug purposes - this prohibits one button click to change
     * the level multiple times.
     */
    private int level_change_cooldown;

    /**
     * Constructor, sets all necessary attributes.
     */
    public JoseMain()
    {
        super("Taco Eating Jose");

        characters = new ArrayList<Character>();
        window_width = 800;
        window_height = 600;
        level_change_cooldown = 0;
    }

    /**
     * Main method of the game, creates an instance of the JoseMain
     * object and runs it.
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        // No need for a while loop here, the repetition of levels upon
        // death is done inside the game by reloading the level (wiping
        // all character etc containers and resetting the score).
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

    /**
     * Initializes the game object by calling init_level on the
     * starting level.
     * Called only after the game's constructor, things that need to be
     * done between levels should be in init_level, not in init.
     * @param cont The game container.
     * @throws SlickException When error occurs during level load.
     */
    @Override
    public void init(GameContainer cont) throws SlickException
    {
        game_container = cont;
        player_score = 0;
        curr_state = STATE.RUNNING;
        current_level = 1;
        init_level(cont, current_level);
    }

    /**
     * Updates the game on each frame.
     * @param cont The game container.
     * @param i Time passed since the last update call.
     * @throws SlickException When error occurs during GameContainer
     *                        manipulation.
     */
    @Override
    public void update(GameContainer cont, int i) throws SlickException
    {
        // Just for better code readability.
        Input in = cont.getInput();

        // Escape key - TODO: Implement menu.
        if(in.isKeyDown(Input.KEY_ESCAPE))
            System.exit(0);

        // Update characters.
        for(Character c : characters)
            c.update(i);

        // End the game if necessary.
        if(player.is_dead() && curr_state != STATE.WON)
            curr_state = STATE.END;

        if(curr_state == STATE.END && in.isKeyDown(Input.KEY_ENTER))
        {
            player_score = 0; // So that the score is reseted after
                              // the player dies.
            curr_state = STATE.RUNNING;
            init_level(cont, current_level); // Reload the level.
        } else if(curr_state == STATE.WON && in.isKeyDown(Input.KEY_ENTER))
        {
            curr_state = STATE.RUNNING;
            current_level = 1;
            player_score = 0;
            init_level(cont, current_level); // Reload the level.
        }

        // Check for dead characters.
        if(player.is_dead())
            player_score = player.get_score(); // Save score before deleting.
        Iterator<Character> it = characters.iterator();
        while(it.hasNext())
        {
            Character c = it.next();

            // RIP.
            if(c.is_dead())
                it.remove();
        }

        // Turns debug mode ON if possible.
        if(debug_possible && in.isKeyDown(Input.KEY_H))
            Character.debug = false;
        else if(debug_possible && in.isKeyDown(Input.KEY_G))
            Character.debug = true;

        // Changing levels in debug mode.
        if(Character.debug)
        {
            // Pauses between level changes.
            if(level_change_cooldown < 30)
                level_change_cooldown++;

            if(level_change_cooldown == 30 && in.isKeyDown(Input.KEY_O))
            {
                level_change_cooldown = 0;
                prev_level();
            }
            else if(level_change_cooldown == 30 && in.isKeyDown(Input.KEY_P))
            {
                level_change_cooldown = 0;
                next_level();
            }
        }

        // Level finnished?
        if(portal.get_bounds().intersects(player.get_bounds()))
        {
            player_score = player.get_score(); // Preserve the score!
            next_level();
        }
    }

    /**
     * Renders all of the game's tiles, characters and objects.
     * Also renders the LOST/WON text when the level/game has ended.
     * @param cont The game container.
     * @param g The game's graphics context.
     * @throws SlickException When error occurs during graphis drawing.
     */
    @Override
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
                // Draw background:
                g.setColor(Color.black);
                g.fillRect(130, 280, 580, 100);
                g.setColor(Color.white);
                g.fillRect(140, 290, 560, 80);
                
                // Draw the text.
                g.setColor(Color.black);
                g.drawString("Score: " + player_score, 350, 300);
                g.drawString("YOU LOST!", 350, 325);
                g.drawString("Press enter to restart the level or escape to leave the game.", 150, 350);
                break;
            case WON:
                render_running(g);
                // Draw background:
                g.setColor(Color.black);
                g.fillRect(130, 280, 580, 100);
                g.setColor(Color.white);
                g.fillRect(140, 290, 560, 80);
                
                // Draw the text.
                g.setColor(Color.black);
                g.drawString("Score: " + player_score, 360, 300);
                g.drawString("YOU WON!", 370, 325);
                g.drawString("Press enter to restart the game or escape to leave the game.", 150, 350);
                break;
        }
        g.setColor(tmp); // Restore the backed up color.

        if(Character.debug)
        {
            // Easier to draw it manually (can be placed elsewhere etc.) than
            // to turn the option on on the game container.
            g.drawString("FPS: " + cont.getFPS(), 10, 10);
        }
    }

    /**
     * Renders the game while in the running state.
     * Draws portal, all characters and platforms and coins through the player.
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
     * Checks the current level map's tiles in the layer 0
     * for the locations, colors and values of all coins.
     * Uses XML parsing from the class TiledMap.
     * TODO: Merge the three map parsing functions into one,
     *       so that only one check is required per tile.
     * @param m Reference to the current level's map.
     * @return List of all coin object in the current level.
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
                    { // Value is separate from color for differently
                      // valued coins of the same color.
                        val = Integer.parseInt(value);
                    }
                    catch(NumberFormatException ex)
                    { // Any NumberFormatException is indicating an error in
                      // the tile properties.
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
                    { // Color distinction between coins.
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
     * Get's platforms from the 0 layer in the current level's map
     * and returns them in a List.
     * Uses XML parsing from the class TiledMap.
     * TODO: Merge all map parsing functions into one, so that only
     *       one check per tile is required.
     * @param m The target tile map.
     * @return List of all movable platforms in the current level.
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
                        // Any NumberFormatExceptions in this game mean wrong
                        // properties of map tiles.
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
     * Checks if the level number is ok - there is an actual map for it.
     * @throws SlickException When error occurs during level load.
     */
    public void next_level() throws SlickException
    {
        if(current_level + 1 >= levels_total)
        {
            // Debuggers don't need no win!
            if(Character.debug)
                return;

            curr_state = STATE.WON;
            player_score = player.get_score();
            player.die(); // Ha Ha!
        }
        else
        { // Load the next level.
            current_level++;
            init_level(game_container, current_level);
        }
    }

    /**
     * Loads the previous level of the game.
     * Checks if the level number is ok - there is an actual map for it.
     * @throws SlickException When error occurs during the level load.
     */
    public void prev_level() throws SlickException
    {
        if(current_level - 1 < 0)
        {
            // Just don't load when the debug mode is on.
            if(Character.debug)
                return;

            // Don't load and inform about the error when out of debug mode.
            System.out.println("[Error] Trying to load an invalid level:"
                    + (current_level - 1));
            return;
        }

        current_level--;
        init_level(game_container, current_level);
    }
}
