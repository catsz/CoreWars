package CoreWars.type;

import CoreWars.game.Catalog;
import CoreWars.logic.Icon;
import arc.Events;
import arc.struct.IntMap;
import arc.struct.Seq;
import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.type.ItemStack;

public class PlayerType {

    public static IntMap<PlayerType> players = new IntMap<PlayerType>();

    public Player owner;
    public Resources resources;
    public Catalog.Type catalog = Catalog.Type.armor;
    public boolean aviableToRemove = true;

    public static void init() {
        Events.on(EventType.PlayerConnect.class, e -> players.put(e.player.id, new PlayerType(e.player)));

        Events.on(EventType.PlayerLeave.class, e -> players.remove(e.player.id));
    }

    public static PlayerType get(Player player) {
        return players.get(player.id);
    }

    public PlayerType(Player owner) {
        players.put(owner.id, this);
        this.owner = owner;
        resources = new Resources();
    }

    public String generateResources() {
        StringBuilder sb = new StringBuilder();
        for (ItemStack itemStack : resources.inventory) {
            sb.append("[white]").append(Icon.get(itemStack.item)).append(":").append("\t[#").append(itemStack.item.color.toString()).append("]").append(itemStack.amount).append(" ");
        }
        return sb.toString();
    }

    public void nextPage() {
        switch (catalog) {
            case armor:
                catalog = Catalog.Type.defense;
                break;
            case defense:
                catalog = Catalog.Type.weapons;
                break;
            case weapons:
                catalog = Catalog.Type.armor;
                break;
            default:
                catalog = Catalog.Type.armor;
        }
    }

    public void prevPage() {
        switch (catalog) {
            case armor:
                catalog = Catalog.Type.weapons;
                break;
            case defense:
                catalog = Catalog.Type.armor;
                break;
            case weapons:
                catalog = Catalog.Type.defense;
                break;
            default:
                catalog = Catalog.Type.weapons;
        }
    }
}
