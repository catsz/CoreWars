package CoreWars.game;

import CoreWars.logic.Icon;
import CoreWars.type.ItemType;
import CoreWars.type.PlayerType;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Interval;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.gen.Call;
import mindustry.gen.Posc;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.world.blocks.storage.CoreBlock;

public class Spawner {

    public static Seq<Spawner> spawners = new Seq<>();
    public float drawx, drawy;
    public CoreBlock.CoreBuild nearestCore;
    public boolean spawned = false, enabled = true;
    public float spawnTime, spawnInterval;
    Item item;
    int x, y;
    int max;
    Seq<ItemType> items;

    public Spawner(Item item, int x, int y, int spawnTime) {
        this.item = item;
        this.x = x;
        this.y = y;
        this.drawx = x * Vars.tilesize;
        this.drawy = y * Vars.tilesize;
        this.spawnInterval = spawnTime;
        this.items = new Seq<>();
        this.spawnTime = spawnTime;
        this.max = getMax();
    }

    public void update(PlayerType player) {
        if (spawnTime < 0 && enabled) {
            if (!spawned && items.size < max) {
                items.add(ItemType.create(item, drawx + Mathf.random(-16f, 16f), drawy + Mathf.random(-16f, 16f)));
                spawned = true;
            }
            if (inRange(player.owner, 16)) {
                for (ItemType item1 : items) {
                    Call.transferItemEffect(item, item1.getX(), item1.getY(), player.owner.unit());
                }
                player.resources.add(item, items.size);
                items.clear();
            }
            if (inRange(player.owner, 120)) {
                for (ItemType item1 : items) {
                    Call.label(Icon.get(item1.item), spawnInterval / 60f, item1.getX(), item1.getY());
                }
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
        if (Vars.world.tile(x, y) != null) {
            Vars.world.tile(x, y).setNet(Blocks.plastaniumWall);
        }
        for (Unit unit : nearestCore.team.data().units) {
            if (unit.isPlayer()) {
                Vars.netServer.assignTeam(unit.getPlayer());
            }
            unit.kill();
        }
    }

    public boolean inRange(Posc pos, float dst) {
        return pos.dst(drawx, drawy) <= dst;
    }

    public boolean inRange(float x, float y, float dst) {
        return Mathf.dst(drawx, drawy, x, y) <= dst;
    }

    public int getMax() {
        if (item == Items.copper) {
            return 20;
        } else if (item == Items.thorium) {
            return 15;
        }
        return 10;
    }
}
