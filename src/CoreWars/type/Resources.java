package CoreWars.type;

import arc.struct.IntMap;
import mindustry.type.Item;
import mindustry.type.ItemSeq;
import mindustry.type.ItemStack;

public class Resources {
    ItemSeq inventory;

    public Resources() {
        inventory = new ItemSeq();
    }
    /*
        copper - start material
        silicon - like an iron
        plastinium - high level
        graphite - build material | need to build plastinium conveyor
    */
    public void add(Item item, int amount) {
        inventory.add(item, amount);
    }
    
    public int get(Item item) {
        return inventory.get(item);
    }
}
