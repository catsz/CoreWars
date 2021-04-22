package CoreWars;

import CoreWars.logic.Icon;
import CoreWars.logic.Logic;
import CoreWars.type.PlayerType;
import CoreWars.type.UnitData;
import arc.Events;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Gamemode;
import mindustry.game.Rules;
import mindustry.gen.Call;
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
        UnitData.init();
        PlayerType.init();
        Icon.load();

        logic = new Logic();
        logic.init();
        
        rules = new Rules();
        for (Block block : Vars.content.blocks()) {
            if (block == Blocks.copperWallLarge || block == Blocks.plastaniumWallLarge || block == Blocks.thoriumWallLarge) continue;
            rules.bannedBlocks.add(block);
        }
        rules.pvp = false;
        rules.unitAmmo = true;
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
        Events.on(EventType.ServerLoadEvent.class, event -> {
            load();
            Vars.netServer.openServer();
        });
        
        Events.on(EventType.WorldLoadEvent.class, e -> Vars.state.rules = rules.copy());
    }
    
    public static void load() {
        Seq<Player> players = new Seq<>();
        
        Vars.logic.reset();
        Call.worldDataBegin();
        Vars.state.rules = rules.copy();
        Vars.world.loadMap(Vars.maps.getNextMap(Gamemode.pvp, Vars.state.map));
        for (Player player : players) {
            Vars.netServer.sendWorldData(player);
        }
        Vars.logic.play();
    }
}
