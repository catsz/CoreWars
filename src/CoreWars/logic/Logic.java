package CoreWars.logic;

import CoreWars.MainX;
import CoreWars.game.Catalog;
import CoreWars.game.Shop;
import CoreWars.game.Spawner;
import CoreWars.type.PlayerType;
import CoreWars.type.UnitData;
import arc.Events;
import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Time;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.game.EventType.BlockBuildBeginEvent;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.storage.CoreBlock;

public class Logic {

    public float updateTime = 60;
    public float shopInterval = 60 * 2f;
    public float shopTime = shopInterval;
    int currentTeam = 10;
    boolean loaded = false;
    Interval interval;
    Catalog[] catalogs;
    Seq<CoreBlock.CoreBuild> cores;

    public void init() {
        catalogs = new Catalog[3];
        initCatalogs();
        interval = new Interval(1);
        cores = new Seq<>();
        Events.on(EventType.WorldLoadEvent.class, event -> {
            onWorldLoad();
        });
        Events.on(EventType.BlockDestroyEvent.class, event -> {

        });

        Events.on(EventType.BlockBuildBeginEvent.class, event -> {
            onBlockBuildBegin(event);
        });

        //*sigh*
        Events.run(EventType.Trigger.update, () -> {
            update();
        });
    }

    public void onBlockDestory(EventType.BlockDestroyEvent event) {
        if (cores.contains(c -> (c.tile.x == event.tile.x && c.tile.y == event.tile.y))) {
            Log.info(event.tile.x + " : " + event.tile.y);
            CoreBlock.CoreBuild core = cores.find(c -> (c.tile.x == event.tile.x && c.tile.y == event.tile.y));
            core.team.data().units.forEach(u -> u.kill());
            core.kill();
        };
    }

    public void onBlockBuildBegin(BlockBuildBeginEvent event) {
        if (event.tile.build != null) {
            if (event.tile.floor().isLiquid) {
                event.tile.build.kill();
            } else if (event.unit != null) {
                if (event.unit.isPlayer() && event.tile.build.block == Blocks.conveyor) {
                    if (PlayerType.get(event.unit.getPlayer()).resources.get(Items.graphite) <= 0) {
                        event.tile.build.kill();
                    };
                }
            }
        }
    }

    public void onWorldLoad() {
        Shop.shops.clear();
        UnitData.data.clear();
        Spawner.spawners.clear();
        cores.clear();

        boolean firstCore = true;
        for (Tile tile : Vars.world.tiles) {
            if (tile.overlay() instanceof OreBlock) { // Remove all ores from map | fuck it xd
                tile.setOverlay(Blocks.air);
            }
            if (tile.floor() == (Floor) Blocks.darkPanel6) { // Core Spawner
                if (firstCore) {
                    tile.setNet(Blocks.coreShard, Team.sharded, 0);
                    firstCore = false;
                } else {
                    tile.setNet(Blocks.coreShard, Team.get(currentTeam), 0);
                    currentTeam++;
                }
                cores.add((CoreBlock.CoreBuild) tile.build);
            }
            if (tile.floor() == (Floor) Blocks.metalFloor2) { // Shop Spawner
                Shop shop = Shop.create(tile.x, tile.y);
                shop.spawn();
                shop.add(catalogs);
            }
            if (tile.floor() == (Floor) Blocks.darkPanel1) { // Copper Generator
                Spawner.spawners.add(new Spawner(Items.copper, tile.x, tile.y, (int) (60 * 2f)));
            }
            if (tile.floor() == (Floor) Blocks.darkPanel5) { // Thorium Generator
                Spawner.spawners.add(new Spawner(Items.thorium, tile.x, tile.y, 60 * 5));
            }
            if (tile.floor() == (Floor) Blocks.darkPanel4) { // Plastinium Generator
                Spawner.spawners.add(new Spawner(Items.plastanium, tile.x, tile.y, 60 * 10));
            }
        }
        for (Spawner spawner : Spawner.spawners) { // set nearest core for spawners | to disable and remove when core die
            CoreBlock.CoreBuild nearest = cores.get(0);
            for (CoreBlock.CoreBuild core : cores) {
                if (core.dst(spawner.drawx, spawner.drawy) < nearest.dst(spawner.drawx, spawner.drawy)) {
                    nearest = core;
                }
            }
            spawner.nearestCore = nearest;
        }
        Timer.schedule(() -> { // spawn core builders | haha poly go brrrrr
            for (CoreBlock.CoreBuild core : cores) {
                UnitTypes.poly.spawn(core.team, core.x, core.y);
            }
            loaded = true;
        }, 3);
    }

    public void update() {
        if (Vars.state.serverPaused) {
            return;
        }
        shopTime -= Time.delta;
        Spawner.spawners.forEach(s -> s.spawnTime -= Time.delta);
        for (PlayerType player : PlayerType.players.values()) {
            // --- Non Air Check ---
            if (player.owner.unit() != null) {
                Tile tile = player.owner.unit().tileOn();
                if (tile != null) {
                    if (tile.build == null && tile.floor().isLiquid) {
                        if (player.resources.get(Items.graphite) <= 0) {
                            player.aviableToRemove = true;
                            player.owner.unit().kill();
                        } else { // if build resouce < 1 plauyer can't build conveyor => die
                            player.resources.add(Items.graphite, -1);
                            if (player.owner.unit() != null) {
                                tile.setNet(Blocks.conveyor, player.owner.team(), (int) (player.owner.unit().rotation / 90f));
                            }
                        }
                    }
                }
            }
            if (player.owner.unit() == null || player.owner.unit().spawnedByCore) { // Spawn dagger when play try to spawn in core bruh
                if (player.owner.team().core() != null) {
                    CoreBlock.CoreBuild c = player.owner.team().core();
                    Unit unit = UnitTypes.dagger.spawn(c.team, c.x, c.y + Vars.tilesize * 5);
                    unit.health = UnitTypes.dagger.health;
                    player.owner.unit(unit);
                }
            }
            // --- TeleportUnitFix ---
            if (player.oldUnit != player.owner.unit() && player.owner.core() != null) { // that need when player select poly like a main unit, because oldUnit have NullController
                if (player.owner.dst(player.pos) > Vars.tilesize * 15) {
                    player.resources.clear();
                    player.aviableToRemove = false;
                }
            } else {
                player.pos = player.owner.unit();
            }
            player.oldUnit = player.owner.unit();
            // --- ShieldLogic ---
            if (player.owner.unit().shield != player.oldShieldValue) { // Some Color Stuff :P
                player.oldShieldValue = player.owner.unit().shield();
                Call.effect(Fx.shieldApply, player.owner.x, player.owner.y, player.owner.y, Color.clear);
            }

            // --- Shoot Coordinate ---
            float sx = player.owner.unit().aimX,
                    sy = player.owner.unit().aimY;
            // --- Shop ---
            if (shopTime < 0) {
                for (Shop shop : Shop.shops) {
                    // shop x, y
                    if (player.owner.dst(shop.getX() * Vars.tilesize, shop.getY() * Vars.tilesize) > 160) {
                        continue;
                    }
                    float shx = shop.getX() * Vars.tilesize,
                            shy = shop.getY() * Vars.tilesize;

                    // right switch
                    float rs = shx + Vars.tilesize * 2;
                    if (sx >= rs - 4 && sx <= rs + 12 && sy >= shy - 4 && sy <= shy + 12) {
                        Call.effect(player.owner.con, Fx.heal, rs, shy, 0, Color.clear);
                        player.nextPage();
                    }
                    Call.label(player.owner.con, ">", shopInterval / 60f, rs, shy);

                    // left switch
                    float ls = shx - Vars.tilesize * 2;
                    if (sx >= ls - 4 && sx <= ls + 12 && sy >= shy - 4 && sy <= shy + 12) {
                        Call.effect(player.owner.con, Fx.heal, ls, shy, 0, Color.clear);
                        player.prevPage();
                    }
                    Call.label(player.owner.con, "<", shopInterval / 60f, ls, shy);
                    Call.label(player.owner.con, player.catalog.name(), shopInterval / 60f, shx, shy);

                    // Items Logic
                    for (int i = 0; i < shop.get(player.catalog).items.size; i++) {
                        Catalog.Xitem item = shop.get(player.catalog).items.get(i);
                        float ty = shy + ((-2 - i * 2) * Vars.tilesize);

                        Call.label(player.owner.con, item.gen(), shopInterval / 60f, shx, ty);
                        if (sx >= shx - 4 && sx <= shx + 12 && sy >= ty - 4 && sy <= ty + 12) {
                            if (item.canBuy(player)) {
                                item.onBuy(player);
                                Call.effect(player.owner.con, Fx.heal, shx, ty, 0, Color.clear);
                            }
                        }
                    }
                }
            }
            // --- UnitData ---
            UnitData.data.forEach(e -> {
                if (e != null) {
                    if (e.value != null) {
                        e.value.update();
                    }
                }
            });

            // --- Spawner ---
            Spawner.spawners.forEach(s -> s.update(player));

            // --- Player HUD ---
            StringBuilder hud = new StringBuilder();

            hud.append("[#").append(player.owner.team().color.toString()).append("]Team#").append(player.owner.team().id).append("\n");
            hud.append(player.generateResources());
            Call.setHudText(player.owner.con, hud.toString());
            // --- End Game ---
            for (CoreBlock.CoreBuild core : cores) {
                if (core.tile.build == null) {
                    cores.remove(core);
                }
            }
            if (cores.size <= 1 && loaded) {
                if (cores.size > 0) {
                    CoreBlock.CoreBuild c = cores.get(0);
                    Call.infoMessage("[#" + c.team.color.toString() + "]Team#" + c.team.id + " [accent]is Winner");
                    Timer.schedule(() -> MainX.load(), 3);
                    loaded = false;
                } else {
                    Call.infoMessage("[gray]Draw");
                    Timer.schedule(() -> MainX.load(), 3);
                    loaded = false;
                }
            }
        }
        // --- Fix Interval'nt ---
        // because Anuke interval suck
        Spawner.spawners.forEach(s -> {
            if (s.spawnTime < 0) {
                s.spawnTime = s.spawnInterval;
            }
            s.spawned = false;
        });
        if (shopTime < 0) {
            shopTime = shopInterval;
        }
    }

    public void initCatalogs() {
        // #armor
        catalogs[0] = new Catalog(Catalog.Type.armor, new Catalog.Xitem[]{
            new Catalog.Xitem("[lime]50[white] shield ", (player) -> {
                player.owner.unit().health = player.owner.unit().maxHealth * 1.2f;
                player.owner.unit().shield = 50f;
            }, new ItemStack(Items.copper, 10)),
            new Catalog.Xitem("[lime]150[white] shiled ", (player) -> {
                player.owner.unit().health = player.owner.unit().maxHealth * 1.7f;
                player.owner.unit().shield = 150f;
            }, new ItemStack(Items.copper, 15), new ItemStack(Items.thorium, 7)),
            new Catalog.Xitem("[lime]350[white] shiled", (player) -> {
                player.owner.unit().shield = 350f;
            }, new ItemStack(Items.plastanium, 5), new ItemStack(Items.thorium, 12)),
            new Catalog.Xitem("[red]1.7[white] maxhealth ", (player) -> {
                player.owner.unit().maxHealth = player.owner.unit().type.health * 1.7f;
                player.owner.unit().health = player.owner.unit().maxHealth;
            }, new ItemStack(Items.plastanium, 5), new ItemStack(Items.thorium, 5)),});
        // #defense
        catalogs[1] = new Catalog(Catalog.Type.defense, new Catalog.Xitem[]{
            new Catalog.Xitem("[#" + Items.graphite.color.toString() + "]Build Material[]", (player) -> {
                if (player.owner.unit().team.core() != null) {
                    player.resources.add(Items.graphite, 10);
                    Call.transferItemEffect(Items.graphite, player.owner.team().core().x, player.owner.core().y, player.owner.unit());
                }
            }, new ItemStack(Items.copper, 5)),
            new Catalog.Xitem("[#" + Items.copper.color.toString() + "]Copper[]", (player) -> {
                if (player.owner.core() != null) {
                    Call.transferItemTo(player.owner.unit(), Items.copper, 24, player.owner.x, player.owner.y, player.owner.core());
                }
            }, new ItemStack(Items.copper, 10)),
            new Catalog.Xitem("[#" + Items.thorium.color.toString() + "]Throium[]", (player) -> {
                if (player.owner.core() != null) {
                    Call.transferItemTo(player.owner.unit(), Items.thorium, 24, player.owner.x, player.owner.y, player.owner.core());
                }
            }, new ItemStack(Items.thorium, 8)),
            new Catalog.Xitem("[#" + Items.plastanium.color.toString() + "]Plastinium[]", (player) -> {
                if (player.owner.core() != null) {
                    Call.transferItemTo(player.owner.unit(), Items.plastanium, 48, player.owner.x, player.owner.y, player.owner.core());
                    Call.transferItemTo(player.owner.unit(), Items.metaglass, 16, player.owner.x, player.owner.y, player.owner.core());
                }
            }, new ItemStack(Items.plastanium, 6))
        });
        // #weapons
        catalogs[2] = new Catalog(Catalog.Type.weapons, new Catalog.Xitem[]{
            new Catalog.Xitem("[gray]Duo []", (player) -> {
                setWeaponForUnitData(player, UnitTypes.beta);
            }, new ItemStack(Items.copper, 15)),
            new Catalog.Xitem("[gray]Knife []", (player) -> {
                setWeaponForUnitData(player, UnitTypes.mace);
            }, new ItemStack(Items.thorium, 5), new ItemStack(Items.copper, 15)),
            new Catalog.Xitem("[gray]Laser []", (player) -> {
                setWeaponForUnitData(player, UnitTypes.quasar);
            }, new ItemStack(Items.plastanium, 5), new ItemStack(Items.thorium, 10)),
            new Catalog.Xitem("[gray]Artillery[]", (player) -> {
                setWeaponForUnitData(player, UnitTypes.fortress);
            }, new ItemStack(Items.plastanium, 10)),}
        );
    }

    public void setWeaponForUnitData(PlayerType player, UnitType type) {
        UnitData dat = UnitData.data.get(player.owner.unit().id);
        dat.mounts.clear();
        dat.weapons.clear();
        if (type == UnitTypes.mace) {
            dat.hasFire = true;
        }
        dat.addWeapon(type);
    }
}
