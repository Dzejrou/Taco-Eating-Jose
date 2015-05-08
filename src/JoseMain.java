package Jose.src;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.ScalableGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.Input;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import Jose.src.util.View;
import Jose.src.characters.Character;
import Jose.src.characters.Player;
import Jose.src.characters.Boo;

/**
 * Main game class, updates and renders the game, selects levels.
 */
public class JoseMain extends BasicGame
{
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
        try
        {
            AppGameContainer game = new AppGameContainer(new ScalableGame(new JoseMain(), 800, 700));
            game.setDisplayMode(800,700,false);
            game.setVSync(true);
            game.start();
        }
        catch(SlickException ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    /**
     * Initializes the game object by calling init_level on the
     * starting level.
     * @param cont The game container.
     */
    public void init(GameContainer cont) throws SlickException
    {
        init_level(cont, 0);
    }

    @Override
    /**
     * Updates the game on each frame.
     * @param cont The game container.
     * @param i Time passed since the last update call.
     */
    public void update(GameContainer cont, int i) throws SlickException
    {
        // Update characters.
        for(Character c : characters)
            c.update(i);

        // Check for dead characters.
        Iterator<Character> it = characters.iterator();
        while(it.hasNext())
        {
            Character c = it.next();
            if(c.is_dead())
                it.remove();
        }
    }

    @Override
    /**
     * Renders all of the game's tiles, characters and objects.
     * @param cont The game container.
     * @param graph The game's graphics context.
     */
    public void render(GameContainer cont, Graphics graph) throws SlickException
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

        // Draw individual characters.
        for(Character c : characters)
            c.draw(graph);
    }

    /**
     * Initializes a given level by loading it's characters, map, view etc.
     * @param cont The game container.
     * @param level_number The number of the level being initialized.
     */
    private void init_level(GameContainer cont, int level_number) throws SlickException
    {
        switch(level_number)
        {
            case 0:
                // Testing area.
                map = new TiledMap("resources/maps/map00.tmx");
                view = new View(map, 0, 0, 800, 700);
                Player tmp = new Player(map, 100, 400, cont.getInput(), view);

                characters.add(tmp);
                characters.add(new Boo(map, 400, 100, view, tmp));
                break;
            default:
                System.out.println("[Error] Wrong level selected.");
                System.exit(1);
        }
    }
}
