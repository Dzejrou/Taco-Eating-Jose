package Jose.src;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.ScalableGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.Input;

import Jose.src.characters.Player;

public class JoseMain extends BasicGame
{
    private TiledMap map;
    private Player plr;
    private View view;

    public JoseMain()
    {
        super("Taco Eating Jose");
    }

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
    public void init(GameContainer cont) throws SlickException
    {
        map = new TiledMap("resources/maps/map00.tmx");
        view = new View(map, 0, 0, 800, 700);
        plr = new Player(map, 100, 400, cont.getInput(), view);
    }

    @Override
    public void update(GameContainer cont, int i) throws SlickException
    {
        plr.update(i);
    }

    @Override
    public void render(GameContainer cont, Graphics graph) throws SlickException
    {
        int tile_offset_x = (int) - (view.x % map.getTileWidth());
        int tile_offset_y = (int) - (view.y % map.getTileHeight());

        int tile_index_x = (int)(view.x / map.getTileWidth());
        int tile_index_y = (int)(view.y / map.getTileHeight());

        map.render(tile_offset_x, tile_offset_y,
                   tile_index_x, tile_index_y,
                   (int)((view.width - tile_offset_x) / map.getTileWidth() + 1),
                   (int)((view.height - tile_offset_y) / map.getTileHeight() + 1));
        plr.draw(graph);
    }
}
