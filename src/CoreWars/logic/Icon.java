package CoreWars.logic;

import java.util.HashMap;

import mindustry.content.Items;
import mindustry.gen.Iconc;
import mindustry.type.Item;

public class Icon {

    public static HashMap<Item, Character> icons = new HashMap<>();

    public static void load() {
        /* Not for my brain xd
        for (Field field : Iconc.class.getDeclaredFields()) {
            if (field.getName().startsWith("item")) {
                icons.put(Vars.content.getByName(ContentType.item, field.getName().toLowerCase().substring(4)), field.get(Iconc));
            }
        }*/
        icons.put(Items.copper, Iconc.itemCopper);
        icons.put(Items.graphite, Iconc.itemGraphite);
        icons.put(Items.plastanium, Iconc.itemPlastanium);
        icons.put(Items.thorium, Iconc.itemThorium);
    }

    public static String get(Item item) {
        if (icons.containsKey(item)) {
            return icons.get(item).toString();
        }
        return "";
    }
}
