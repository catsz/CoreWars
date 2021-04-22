package CoreWars.type;

import arc.math.geom.Position;
import mindustry.type.Item;

//why not implement position
public class ItemType implements Position {
    public Item item;
    float x, y;
    
    public static ItemType create(Item item, float x, float y) {
        ItemType it = new ItemType();
        it.x = x;
        it.y = y;
        it.item = item;
        return it;
    }
    
    private ItemType() {
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }
}
