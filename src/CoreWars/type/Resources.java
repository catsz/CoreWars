package BedWars.type;

import arc.struct.IntMap;
import mindustry.type.Item;
import mindustry.type.ItemStack;

public class Resources {
    IntMap<ItemStack> inventory;

    public Resources() {
        inventory = new IntMap<>();
    }
    /*
        copper - start material
        silicon - like an iron
        plastinium - high level
        graphite - build material | need to build plastinium conveyor
    */
    public void add(Item item, int amount) {
        if (inventory.containsKey(item.id)) {
            inventory.get(item.id).amount += amount;
        } else {
            inventory.put(item.id, new ItemStack(item, amount));
        }
    }
    
    public int get(Item item) {
        if (inventory.containsKey(item.id)) {
            return inventory.get(item.id).amount;
        }
        return 0;
    }
}
