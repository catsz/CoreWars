package CoreWars;

import CoreWars.logic.Icon;
import CoreWars.logic.Logic;
import CoreWars.type.PlayerType;
import CoreWars.type.UnitData;
import arc.Events;
import arc.struct.Seq;
import arc.util.CommandHandler;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.world.Block;

/**
 *
 * @author Xusk
 */
public class MainX extends Plugin {

    Logic logic;
    public static Rules rules;

    @Override
    public void init() {
        rules = new Rules();
        for (Block block : Vars.content.blocks()) {
            if (block == Blocks.copperWallLarge || block == Blocks.plastaniumWallLarge || block == Blocks.thoriumWallLarge) {
                continue;
            }
            rules.bannedBlocks.add(block);
        }
        rules.pvp = true;
        rules.canGameOver = false;
        rules.waveTimer = false;
        rules.buildSpeedMultiplier = 0.5f;

        Blocks.plastaniumWall.health = 999999;
        Blocks.plastaniumConveyor.health = 999999;
        Blocks.conveyor.buildCostMultiplier = 10f;
        UnitTypes.poly.health = 999999f;
        UnitTypes.poly.speed = 0;
        UnitTypes.poly.defaultController = UnitTypes.mega.defaultController;
        UnitTypes.poly.weapons.clear();

        UnitData.init();
        PlayerType.init();
        Icon.load();

        logic = new Logic();
        logic.init();

        Events.on(EventType.ServerLoadEvent.class, event -> {
            load();
            Vars.netServer.openServer();
        });

        Events.on(EventType.WorldLoadEvent.class, e -> {
            Vars.state.rules = rules.copy();
        });
    }

    public static void load() {
        Seq<Player> players = new Seq<>();
        Groups.player.copy(players);

        Vars.logic.reset();
        Call.worldDataBegin();
        Vars.state.rules = rules.copy();
        Vars.world.loadMap(Vars.maps.getNextMap(Gamemode.pvp, Vars.state.map));

        for (Player player : players) {
            Vars.netServer.sendWorldData(player);
        }
        Vars.logic.play();
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("info", "Info for CoreWars", (args, player) -> {
            player.sendMessage("[scarlet]Defend |[white]core.\n"
                    + "[scarlet]Generators | [white]generate resources, go near and wait to pick up it"
                    + "\n[accent]Shop | [white]hold on item to buy it, '<' and '>' switch catalog"
                    + "\n[accent]BuildMaterial(Graphite) | [white]automaticly place conveyor under you if you have it"
                    + "\n[accent]Poly | [white]just build walls, you can buy resources in shop"
                    + "\n[accent]Target | [white]destroy all enemy cores\n[accent]/inforu | [white]Для русского перевода");
        });
        handler.<Player>register("inforu", "Инфо по CastleWars", (args, player) -> {
            player.sendMessage("[scarlet]Защищайте |[white]ядро.\n"
                    + "[scarlet]Генераторы | [white]генерируют ресурсы, подойдите к ним и подождите чтобы собрать"
                    + "\n[accent]Магазин | [white]держите на предмете чтобы купить, '<' и '>' для переключения каталога"
                    + "\n[accent]Строительный Материал(Графит) | Автоматический ставит конвеер под вами когда вы на воде"
                    + "\n[accent]Поли| [white]для строительства стен, ресурсы найдете в магазине"
                    + "\n[accent]Цель | [white]уничтожить все вражеския ядра\n[accent]/info | [white]for en translation");
        });
    }

}
