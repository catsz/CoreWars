package CoreWars.game;

import arc.struct.Seq;
import java.awt.Point;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;

public class Shop {
    public static Seq<Shop> shops = new Seq<>();
    
    Seq<Catalog> catalogs;
    int x, y;

    public static Shop create(int x, int y) {
        Shop shop = new Shop();
        shop.x = x;
        shop.y = y + 4;
        shop.catalogs = new Seq<>();

        shops.add(shop);
        return shop;
    }
    
    public Catalog get(Catalog.Type type) {
        return catalogs.find(e -> e.type == type);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point pos() {
        return new Point(x, y);
    }

    public void spawn() {
        Vars.world.tile(x, y).setBlock(Blocks.plastaniumWall, Team.purple, 0);
        Vars.world.tile(x - 2, y).setBlock(Blocks.plastaniumConveyor, Team.purple, 2);
        Vars.world.tile(x + 2, y).setBlock(Blocks.plastaniumConveyor, Team.purple, 0);
        Vars.world.tile(x, y - 2).setBlock(Blocks.plastaniumWall, Team.purple, 0);
        Vars.world.tile(x, y - 4).setBlock(Blocks.plastaniumWall, Team.purple, 0);
        Vars.world.tile(x, y - 6).setBlock(Blocks.plastaniumWall, Team.purple, 0);
        Vars.world.tile(x, y - 8).setBlock(Blocks.plastaniumWall, Team.purple, 0);
    }

    private Shop() {
    }

    public void add(Catalog ... catalogs) {
        this.catalogs.addAll(catalogs);
    }
}
