package BedWars.type;

import BedWars.game.Catalog;
import BedWars.logic.Icon;
import arc.Events;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.type.ItemStack;

public class PlayerType {

    public static Seq<PlayerType> players = new Seq<>();

    public Player owner;
    public Resources resources;
    public Catalog.Type catalog = Catalog.Type.armor;

    public static void init() {
        Events.on(EventType.PlayerConnect.class, event -> {
            players.add(new PlayerType(event.player));
        });

        Events.on(EventType.PlayerLeave.class, event -> {
            players.remove(d -> d.owner.equals(event.player));
        });
    }

    public static PlayerType get(Player player) {
        return players.find(p -> p.owner == player);
    }

    public PlayerType(Player owner) {
        this.owner = owner;
        resources = new Resources();
    }

    public String generateResources() {
        StringBuilder sb = new StringBuilder();
        for (ItemStack itemStack : resources.inventory.values()) {
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
