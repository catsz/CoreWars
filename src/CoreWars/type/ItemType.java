package BedWars.type;

import mindustry.type.Item;

public class ItemType {
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
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
}
