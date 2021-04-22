package CoreWars.game;

import CoreWars.logic.Icon;
import CoreWars.type.ItemType;
import CoreWars.type.PlayerType;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.gen.Posc;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.world.blocks.storage.CoreBlock;

public class Spawner {

    public static Seq<Spawner> spawners = new Seq<>();
    public float drawx, drawy;
    public CoreBlock.CoreBuild nearestCore;
    Item item;
    int x, y;
    int spawnTime;
    Interval interval;
    Seq<ItemType> items;
    boolean enabled = true;

    public Spawner(Item item, int x, int y, int spawnTime) {
        this.item = item;
        this.x = x;
        this.y = y;
        this.drawx = x * Vars.tilesize;
        this.drawy = y * Vars.tilesize;
        this.interval = new Interval(1);
        this.items = new Seq<>();
        this.spawnTime = spawnTime;
    }

    public void update(PlayerType player) {
        if (interval.get(0, spawnTime) && enabled) {
            if (items.size < 10) {
                items.add(ItemType.create(item, drawx + Mathf.random(-16f, 16f), drawy + Mathf.random(-16f, 16f)));
            }
            if (inRange(player.owner, 16)) {
                player.resources.add(item, items.size);
                items.clear();
            }
            for (ItemType item1 : items) {
                Call.label(Icon.get(item1.item), spawnTime / 60f, item1.getX(), item1.getY());
            }
        }
        if (nearestCore != null) {
            if (nearestCore.dead || nearestCore.tile.build == null) {
                remove();
            }
        } else {
            remove();
        }
    }

    public void remove() {
        Spawner.spawners.remove(this);
        for (Unit unit : nearestCore.team.data().units) {
            unit.kill();
        }
    }
    
    public boolean inRange(Posc pos, float dst) {
        return pos.dst(drawx, drawy) <= dst;
    }

    public boolean inRange(float x, float y, float dst) {
        return Mathf.dst(drawx, drawy, x, y) <= dst;
    }
}
