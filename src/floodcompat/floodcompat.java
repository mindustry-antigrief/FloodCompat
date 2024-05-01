package floodcompat;

import arc.*;
import arc.audio.Sound;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.mod.*;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.power.ImpactReactor;

import java.util.Objects;

import static mindustry.content.UnitTypes.*;
import static mindustry.content.Blocks.*;
import static mindustry.Vars.*;

public class floodcompat extends Mod{
    static boolean flood, applied, enabled;
    static Ability pulsarAbility, brydeAbility;
    static ObjectSet<Tile> allTiles = new ObjectSet<>();
    public floodcompat(){
        Log.info("Flood Compatibility loaded!");

        Events.on(EventType.ContentInitEvent.class, e -> {
            pulsarAbility = pulsar.abilities.first();
            brydeAbility = bryde.abilities.first();
        });

        Events.on(EventType.ClientLoadEvent.class, e -> {
            if(Structs.contains(Version.class.getDeclaredFields(), var -> var.getName().equals("foos"))){
                enabled = false;
            }else enabled = true;
        });

        netClient.addPacketHandler("flood", (integer) -> {
            if(Strings.canParseInt(integer)){
                ui.chatfrag.addMessage("[lime]Server check succeeded!");
                flood = true;

                if(applied || !enabled) return;
                ui.chatfrag.addMessage("[accent]Applying flood changes!");

                state.rules.hideBannedBlocks = true;
                state.rules.bannedBlocks.addAll(Blocks.lancer, Blocks.arc);
                state.rules.revealedBlocks.addAll(Blocks.coreShard, Blocks.scrapWall, Blocks.scrapWallLarge, Blocks.scrapWallHuge, Blocks.scrapWallGigantic);

                Seq.with(scrapWall, titaniumWall, thoriumWall).each(w -> w.solid = false);
                Seq.with(berylliumWall, tungstenWall, carbideWall).each(w -> {
                    w.insulated = w.absorbLasers = true;
                });
                ((Wall) phaseWall).chanceDeflect = 0;
                ((Wall) surgeWall).lightningChance = 0;
                ((Wall) reinforcedSurgeWall).lightningChance = 0;
                ((MendProjector) mender).reload = 800;
                ((MendProjector) mendProjector).reload = 500;
                radar.health = 500;
                shockwaveTower.health = 2000;
                thoriumReactor.health = 1400;
                massDriver.health = 1250;
                impactReactor.rebuildable = false;
                ((ItemTurret) fuse).ammoTypes.values().toSeq().each(a -> a.pierce = false);
                ((ItemTurret) fuse).ammoTypes.get(Items.titanium).damage = 10;
                ((ItemTurret) fuse).ammoTypes.get(Items.thorium).damage = 20;
                ((ItemTurret) scathe).ammoTypes.values().toSeq().each(a -> {
                    a.buildingDamageMultiplier = 0.3f;
                    a.damage = 700;
                    a.splashDamage = 80;
                });
                ((PowerTurret) lancer).shootType.damage = 10;
                ((PowerTurret) arc).shootType.damage = 4;
                ((PowerTurret) arc).shootType.lightningLength = 15;
                ((TractorBeamTurret) parallax).force = 8;
                ((TractorBeamTurret) parallax).scaledForce = 7;
                ((TractorBeamTurret) parallax).range = 230;
                ((TractorBeamTurret) parallax).damage = 6;
                ((ForceProjector) forceProjector).shieldHealth = 2500;

                merui.weapons.each(w -> w.bullet.collides = true);
                quad.weapons.each(w -> {
                    w.bullet.damage = 100;
                    w.bullet.splashDamage = 250;
                    w.bullet.splashDamageRadius = 100f;
                });
                fortress.weapons.each(w -> {
                    w.bullet.damage = 40;
                    w.bullet.splashDamageRadius = 60f;
                });
                scepter.weapons.each(w -> {
                    if(Objects.equals(w.name, "scepter-weapon")){
                        w.bullet.pierce = true;
                        w.bullet.pierceCap = 3;
                    }else{
                        w.bullet.damage = 25;
                    }
                });
                reign.weapons.each(w -> {
                    w.bullet.damage = 120;
                    w.bullet.fragBullet.damage = 30;
                });
                Seq.with(crawler, spiroct, arkyid).each(u -> u.targetAir = false);
                crawler.health = 100;
                crawler.speed = 1.5f;
                crawler.accel = 0.08f;
                crawler.drag = 0.016f;
                crawler.hitSize = 6f;
                atrax.speed = 0.5f;
                pulsar.abilities.clear();
                bryde.abilities.clear();
                spiroct.speed = 0.4f;
                spiroct.weapons.each(w -> {
                    if(Objects.equals(w.name, "spiroct-weapon")){
                        w.bullet.damage = 25;
                    }else w.bullet.damage = 20;
                    if(w.bullet instanceof SapBulletType b) b.sapStrength = 0;
                });
                arkyid.speed = 0.5f;
                arkyid.hitSize = 21f;
                arkyid.weapons.each(w -> {
                    if(w.bullet instanceof SapBulletType b){
                        b.sapStrength = 0;
                    }else{
                        w.bullet.pierceBuilding = true;
                        w.bullet.pierceCap = 7;
                    }
                });
                toxopid.hitSize = 21f;
                toxopid.weapons.each(w -> {
                    if(Objects.equals(w.name, "toxopid-cannon")) {
                        w.bullet.fragBullet.pierce = true;
                        w.bullet.fragBullet.pierceCap = 2;
                    }
                });
                flare.health = 275;
                flare.engineOffset = 5.5f; // why?
                flare.range = 140;
                horizon.health = 440;
                horizon.speed = 1.7f;
                horizon.itemCapacity = 20;
                zenith.health = 1400;
                zenith.speed = 1.8f;
                vela.weapons.each(w -> w.bullet.damage = 20f);
                oct.abilities.each(a -> {
                    if(a instanceof ForceFieldAbility f){
                        f.regen = 16f;
                        f.max = 15000f;
                    }
                });
                minke.weapons.each(w -> {
                    if(w.bullet instanceof FlakBulletType){
                        w.bullet.collidesGround = true;
                    }
                });

                applied = true;
            }
        });

        netClient.addPacketHandler("anticreep", (string) -> {
            String[] vars = string.split(":");

            int pos = Strings.parseInt(vars[0]), rad = Strings.parseInt(vars[1]),
            time = Strings.parseInt(vars[2]), team = Strings.parseInt(vars[3]);

            if(pos > 0 && rad > 0 && time > 0 && team > 0){
                Tile tile = world.tiles.getp(pos);
                var color = Team.get(team).color;

                Seq<Tile> tiles = new Seq<>();
                Geometry.circle(tile.x, tile.y, rad, (cx, cy) -> {
                    Tile t = world.tile(cx, cy);
                    if(t != null && !allTiles.contains(t)){
                        tiles.add(t);
                    }
                });
                allTiles.addAll(tiles);

                var startTime = Time.millis();

                Timer.schedule(() -> {
                    tiles.each(t -> {
                        Timer.schedule(() -> {
                            var sizeMultiplier = 1 - (Time.millis() - startTime) / 1000f / time;
                            NetClient.effect(Fx.lightBlock, t.getX(), t.getY(), Mathf.random(0.01f, 1.5f * sizeMultiplier), color);
                        }, Mathf.random(1f));
                    });
                }, 0, 1, time);

                Timer.schedule(() -> {
                    allTiles.removeAll(tiles);
                    tiles.clear();
                }, time);
            }
        });

        Events.on(EventType.WorldLoadEvent.class, e -> {
            // no delay if the client's hosting, that would break stuff!
            int delay = net.client() ? 3 : 0;
            flood = false;

            if(delay > 0) Call.serverPacketReliable("flood", "0.5");
            Timer.schedule(() -> {
                // this is for cleanup only
                if(!flood && enabled){
                    if(net.client()) ui.chatfrag.addMessage("[scarlet]Server check failed...\n[accent]Playing on flood? Try rejoining!\nHave a nice day!");
                    if(applied){
                        Seq.with(scrapWall, titaniumWall, thoriumWall).each(w -> w.solid = true);
                        Seq.with(berylliumWall, tungstenWall, carbideWall).each(w -> {
                            w.insulated = w.absorbLasers = false;
                        });
                        ((Wall) phaseWall).chanceDeflect = 10;
                        ((Wall) surgeWall).lightningChance = 0.05f;
                        ((Wall) reinforcedSurgeWall).lightningChance = 0.05f;
                        ((MendProjector) mender).reload = 200;
                        ((MendProjector) mendProjector).reload = 250;
                        radar.health = 60;
                        shockwaveTower.health = 915;
                        thoriumReactor.health = 700;
                        massDriver.health = 430;
                        impactReactor.rebuildable = true;
                        ((ItemTurret) fuse).ammoTypes.values().toSeq().each(a -> a.pierce = true);
                        ((ItemTurret) fuse).ammoTypes.get(Items.titanium).damage = 66;
                        ((ItemTurret) fuse).ammoTypes.get(Items.thorium).damage = 105;
                        ((ItemTurret) scathe).ammoTypes.values().toSeq().each(a -> {
                            a.buildingDamageMultiplier = 0.2f;
                            a.damage = 1500;
                            a.splashDamage = 160;
                        });
                        ((PowerTurret) lancer).shootType.damage = 140;
                        ((PowerTurret) arc).shootType.damage = 20;
                        ((PowerTurret) arc).shootType.lightningLength = 25;
                        ((TractorBeamTurret) parallax).force = 12f;
                        ((TractorBeamTurret) parallax).scaledForce = 6f;
                        ((TractorBeamTurret) parallax).range = 240f;
                        ((TractorBeamTurret) parallax).damage = 0.3f;
                        ((ForceProjector) forceProjector).shieldHealth = 750;

                        merui.weapons.each(w -> w.bullet.collides = false);
                        quad.weapons.each(w -> {
                            w.bullet.damage = -1;
                            w.bullet.splashDamage = 220;
                            w.bullet.splashDamageRadius = 80f;
                        });
                        fortress.weapons.each(w -> {
                            w.bullet.damage = 20;
                            w.bullet.splashDamageRadius = 35f;
                        });
                        scepter.weapons.each(w -> {
                            if(Objects.equals(w.name, "scepter-weapon")){
                                w.bullet.pierce = false;
                                w.bullet.pierceCap = -1;
                            }else{
                                w.bullet.damage = 10;
                            }
                        });
                        reign.weapons.each(w -> {
                            w.bullet.damage = 80;
                            w.bullet.fragBullet.damage = 20;
                        });
                        Seq.with(crawler, spiroct, arkyid).each(u -> u.targetAir = true);
                        crawler.health = 200;
                        crawler.speed = 1f;
                        crawler.accel = 0;
                        crawler.drag = 0;
                        crawler.hitSize = 8f;
                        atrax.speed = 0.6f;
                        pulsar.abilities.add(pulsarAbility);
                        bryde.abilities.add(brydeAbility);
                        spiroct.speed = 0.54f;
                        spiroct.weapons.each(w -> {
                            if(Objects.equals(w.name, "spiroct-weapon")){
                                w.bullet.damage = 23;
                            }else w.bullet.damage = 18;
                            if(w.bullet instanceof SapBulletType b){
                                if(Objects.equals(w.name, "spiroct-weapon")){
                                    b.sapStrength = 0.5f;
                                }else b.sapStrength = 0.8f;
                            }
                        });
                        arkyid.speed = 0.62f;
                        arkyid.hitSize = 23f;
                        arkyid.weapons.each(w -> {
                            if(w.bullet instanceof SapBulletType b) {
                                b.sapStrength = 0.85f;
                            }else{
                                w.bullet.pierceBuilding = false;
                                w.bullet.pierceCap = -1;
                            }
                        });
                        toxopid.hitSize = 26f;
                        toxopid.weapons.each(w -> {
                            if(Objects.equals(w.name, "toxopid-cannon")) {
                                w.bullet.fragBullet.pierce = false;
                                w.bullet.fragBullet.pierceCap = -1;
                            }
                        });
                        flare.health = 70;
                        flare.range = 104;
                        horizon.health = 340;
                        horizon.speed = 1.65f;
                        horizon.itemCapacity = 0;
                        zenith.health = 700;
                        zenith.speed = 1.7f;
                        vela.weapons.each(w -> w.bullet.damage = 35f);
                        oct.abilities.each(a -> {
                            if(a instanceof ForceFieldAbility f){
                                f.regen = 4f;
                                f.max = 7000f;
                            }
                        });
                        minke.weapons.each(w -> {
                            if(w.bullet instanceof FlakBulletType){
                                w.bullet.collidesGround = false;
                            }
                        });

                        ui.chatfrag.addMessage("[accent]Flood changes reverted!\nConsider using /sync if playing on a server!\nIf you are the host, ignore this message!");
                        applied = false;
                    }
                }
            }, delay);
        });
    }
}
