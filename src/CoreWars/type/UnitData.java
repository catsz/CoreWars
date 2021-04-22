package CoreWars.type;

import arc.Events;
import arc.math.Angles;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.units.WeaponMount;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

public class UnitData {

    public static IntMap<UnitData> data = new IntMap<>();
    public final Unit unit;
    public Seq<Weapon> weapons;
    public Seq<WeaponMount> mounts;
    public boolean hasFire = false;

    public static void init() {
        Events.run(EventType.Trigger.update, () -> {
            Groups.unit.each(unit -> {
                if (!unit.spawnedByCore && !data.containsKey(unit.id)) {
                    data.put(unit.id, new UnitData(unit));
                }
            });
        });

        Events.on(EventType.UnitDestroyEvent.class, event -> {
            if (event.unit.isPlayer()) {
                if (PlayerType.get(event.unit.getPlayer()).aviableToRemove) {
                    PlayerType.get(event.unit.getPlayer()).resources.inventory.clear();
                    PlayerType.get(event.unit.getPlayer()).aviableToRemove = false;
                }
            }
            if (data.containsKey(event.unit.id)) {
                data.remove(event.unit.id);
            }
        });
    }

    public UnitData(Unit unit) {
        this.unit = unit;
        weapons = new Seq<>();
        mounts = new Seq<>();
    }

    public void addWeapon(UnitType type) {
        if (type.hasWeapons()) {
            for (Weapon weapon : type.weapons) {
                weapons.add(weapon);
                mounts.add(new WeaponMount(weapon));
            }
        }
    }

    public void addWeapon(Weapon weapon) {
        weapons.add(weapon);
        mounts.add(new WeaponMount(weapon));
    }

    public void removeWeapon(Weapon weapon) {
        weapons.remove(weapon);
        mounts.remove(m -> m.weapon == weapon);
    }

    public void update() {
        if (unit.isShooting && hasWeapons()) {
            for (WeaponMount mount : mounts) {
                if (mount.reload < 0) {
                    BulletType bt = mount.weapon.bullet;
                    float x = unit.x + Angles.trnsx(unit.rotation, mount.weapon.x, mount.weapon.y),
                            y = unit.y + Angles.trnsy(unit.rotation, mount.weapon.x, mount.weapon.y);
                    mount.weapon.bullet.createNet(unit.team, x, y, unit.rotation, bt.damage, 1, bt.lifetime / bt.range() * bt.speed);
                    if (hasFire) {
                        Call.effect(Fx.fire, x, y, unit.rotation, mount.weapon.heatColor);
                    }
                    Call.effect(mount.weapon.ejectEffect, x, y, unit.rotation, mount.weapon.heatColor);
                    mount.reload = mount.weapon.reload;
                } else {
                    mount.reload--;
                }
            }
        }
        // Only For This gamemode
        if (!unit.isPlayer() && unit.type != UnitTypes.poly) {
            unit.kill();
        } else if (unit.type != null) {
            if (unit.type == UnitTypes.poly && unit.core() != null) {
                unit.set(unit.core().x, unit.core().y);
            }
        }
    }

    public boolean hasWeapons() {
        return weapons.size > 0;
    }

    public int id() {
        return unit.id;
    }
}
