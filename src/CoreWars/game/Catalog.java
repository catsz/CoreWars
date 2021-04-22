package CoreWars.game;

import CoreWars.func.Command;
import CoreWars.logic.Icon;
import CoreWars.type.PlayerType;
import arc.struct.Seq;
import mindustry.type.ItemStack;

public class Catalog {

    public enum Type {
        defense, armor, weapons
    }

    public Type type;
    public Seq<Xitem> items;

    public Catalog(Type type, Xitem... items) {
        this.type = type;
        if (items.length > 4) {
            this.items = new Seq<>();
            this.items.add(items[0], items[1], items[2], items[3]);
        } else {
            this.items = new Seq<>(items);
        }
    }

    public static class Xitem {

        String name;
        Command onBuy;
        Seq<ItemStack> cost;

        public Xitem(String name, Command onBuy, ItemStack... cost) {
            this.name = name;
            this.onBuy = onBuy;
            this.cost = new Seq(cost);
        }

        public String gen() {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append("\n");
            for (ItemStack itemStack : cost) {
                sb.append("[white]").append(Icon.get(itemStack.item)).append(":").append("\t[#").append(itemStack.item.color.toString()).append("]").append(itemStack.amount).append(" ");
            }
            return sb.toString();
        }

        public void onBuy(PlayerType player) {
            for (ItemStack itemStack : cost) {
                player.resources.add(itemStack.item, -itemStack.amount);
            }
            onBuy.run(player);
        }

        public boolean canBuy(PlayerType player) {
            boolean canBuy = true;
            for (ItemStack itemStack : cost) {
                if (player.resources.get(itemStack.item) < itemStack.amount) {
                    canBuy = false;
                }
            }
            return canBuy;
        }
    }
}
