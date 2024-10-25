package floodcompat

import arc.*
import arc.graphics.*
import arc.struct.*
import arc.util.*
import mindustry.Vars.*
import mindustry.ai.*
import mindustry.content.*
import mindustry.content.Blocks.*
import mindustry.content.UnitTypes.*
import mindustry.entities.abilities.*
import mindustry.entities.bullet.*
import mindustry.game.*
import mindustry.gen.*
import mindustry.graphics.*
import mindustry.mod.*
import mindustry.world.blocks.defense.turrets.*
import java.lang.reflect.*

// Based on old foo's implementation
class FloodCompat : Mod() {
    /** Vanilla values of changed vars for restoration later */
    private val defaults: MutableList<Any> = mutableListOf()
    /* Flood changes the bullet type and the overwrites system doesn't support that so we have to manage this manually */
    private var foreshadowBulletVanilla: BulletType? = null

    /** Used to prevent flood from applying twice */
    private var applied: Boolean = false


    override fun init() {
        Log.info("Flood Compatibility loaded!")


        Events.on(EventType.ResetEvent::class.java) {
            disable()
            applied = false
        }
        Events.on(EventType.WorldLoadEvent::class.java) { Log.info("Send flood"); Call.serverPacketReliable("flood", "1.0") }
        netClient.addPacketHandler("flood") { if (Strings.canParseInt(it)) enable() }
    }

    /** Applies flood changes */
    private fun enable() {
        if (applied) throw AssertionError("Tried to enable flood even though it was already enabled!")
        applied = true
        Log.info("Enabling FloodCompat")
        Time.mark()

        // Rules (not overwrites since the game overwrites them automatically when returning to menu
        state.rules.hideBannedBlocks = true
        state.rules.bannedBlocks.addAll(lancer, arc)
        state.rules.revealedBlocks.addAll(coreShard, scrapWall, scrapWallLarge, scrapWallHuge, scrapWallGigantic)

        overwrites( // This system is mostly functional and saves a lot of copy pasting.
            //Blocks
            scrapWall, "solid", false,
            titaniumWall, "solid", false,
            thoriumWall, "solid", false,
            berylliumWall, "absorbLasers", false,
            tungstenWall, "absorbLasers", false,
            carbideWall, "absorbLasers", false,
            phaseWall, "chanceDeflect", 0,
            surgeWall, "lightningChance", 0,
            reinforcedSurgeWall, "lightningChance", 0,
            mender, "reload", 800,
            mendProjector, "reload", 500,
            radar, "health", 500,
            shockwaveTower, "health", 2000,
            thoriumReactor, "health", 1400,
            massDriver, "health", 1250,
            impactReactor, "rebuildable", false,
            *(fuse as ItemTurret).ammoTypes.flatMap { Seq.with(it.value, "pierce", false) }.toTypedArray(),
            (fuse as ItemTurret).ammoTypes.get(Items.titanium), "damage", 10,
            (fuse as ItemTurret).ammoTypes.get(Items.thorium), "damage", 20,
            *(scathe as ItemTurret).ammoTypes.flatMap { Seq.with(
                it.value, "buildingDamageMultiplier", 0.3F,
                it.value, "damage", 700,
                it.value, "splashDamage", 80)
            }.toTypedArray(),
            lancer, "shootType.damage", 10,
            arc, "shootType.damage", 4,
            arc, "shootType.lightningLength", 15,
            parallax, "force", 8,
            parallax, "scaledForce", 7,
            parallax, "range", 230,
            parallax, "damage", 6,
            forceProjector, "shieldHealth", 2500,
            // Units
            pulsar, "commands", arrayOf(UnitCommand.moveCommand, UnitCommand.boostCommand, UnitCommand.mineCommand),
            quasar, "commands", arrayOf(UnitCommand.moveCommand, UnitCommand.boostCommand, UnitCommand.mineCommand),
            pulsar, "abilities", Seq<Ability>(0), // pulsar.abilities.clear()
            bryde, "abilities", Seq<Ability>(0), // pulsar.abilities.clear()
            *merui.weapons.flatMap<Any> { Seq.with(it.bullet, "collides", true) }.toArray(),
            *quad.weapons.flatMap { Seq.with(
                it, "bullet.damage", 100,
                it, "bullet.splashDamage", 250,
                it, "bullet.splashDamageRadius", 100,
            ) }.toArray(),
            *fortress.weapons.flatMap { Seq.with(
                it, "bullet.damage", 40,
                it, "bullet.splashDamageRadius", 60
            ) }.toArray(),
            *scepter.weapons.flatMap { if (it.name == "scepter-weapon") Seq.with(
                it, "bullet.pierce", true,
                it, "bullet.pierceCap", 3
            ) else Seq.with(it, "bullet.damage", 25) }.toArray(),
            *reign.weapons.flatMap { Seq.with(
                it, "bullet.damage", 120,
                it, "bullet.fragBullet.damage", 30
            ) }.toArray(),
            crawler, "targetAir", false,
            spiroct, "targetAir", false,
            spiroct, "speed", 0.4F,
            *spiroct.weapons.flatMap { Seq.with(it, "bullet.damage", if (it.name == "spiroct-weapon") 25 else 20 ).apply { if (it.bullet is SapBulletType) this.add(it, "bullet.sapStrength", 0) } }.toArray(),
            arkyid, "targetAir", false,
            arkyid, "speed", 0.5F,
            arkyid, "hitSize", 21,
            *arkyid.weapons.flatMap {
                if (it.bullet is SapBulletType) Seq.with(it, "bullet.sapStrength", 0)
                else Seq.with(it, "bullet.pierceBuilding", true,
                              it, "bullet.pierceCap", 7
                )
            }.toArray(),
            crawler, "health", 100,
            crawler, "speed", 1.5F,
            crawler, "accel", 0.08F,
            crawler, "drag", 0.016F,
            crawler, "hitSize", 6,
            atrax, "speed", 0.5F,
            toxopid, "hitSize", 21,
            *toxopid.weapons.flatMap<Any> { if (it.name == "toxopid-cannon") Seq.with(
                it.bullet.fragBullet, "pierce", true, // TODO: Make all of the other bullet references it.bullet, "blah" instead of it, "bullet.blah"
                it.bullet.fragBullet, "pierceCap", 2
            ) else Seq.with() }.toArray(),
            flare, "health", 275,
            flare, "engineOffset", 5.5F,
            flare, "range", 140,
            horizon, "health", 440,
            horizon, "speed", 1.7F,
            horizon, "itemCapacity", 20,
            zenith, "health", 1400,
            zenith, "speed", 1.8F,
            *vela.weapons.flatMap { Seq.with(it.bullet, "damage", 20) }.toArray(),
            *oct.abilities.flatMap<Any> { if (it is ForceFieldAbility) Seq.with(
                it, "regen", 16,
                it, "max", 15_000
            ) else Seq.with() }.toArray(),
            *minke.weapons.flatMap { if (it.bullet is FlakBulletType) Seq.with(it.bullet, "collidesGround", true) else Seq.with<Any>()}.toArray()
        )

        // TODO: Implement anticreep packet


        foreshadowBulletVanilla = (foreshadow as ItemTurret).ammoTypes.put(Items.surgeAlloy, foreshadowBulletFlood)
        Log.debug("Enabled FloodCompat in ${Time.elapsed()}ms")
    }

    /** Reverts flood changes */
    private fun disable() {
        Log.debug("Disabling FloodCompat")
        Time.mark()

        defaults.indices.step(3).forEach { (defaults[it + 1] as Field).set(defaults[it], defaults[it + 2]) } // (obj, field, value) -> field.set(obj, value)
        defaults.clear()
        Log.debug("Disabled FloodCompat in ${Time.elapsed()}ms")
    }


    // Utility functions

    /** Convenient way of adding multiple overwrites at once */
    private fun overwrites(vararg args: Any) =
        args.indices.step(3).forEach { overwrite(args[it], args[it + 1] as String, args[it + 2]) }

    private fun <O : Any, T : Any> overwrite(obj: O, name: String, value: T) {
        val split = name.split('.', limit = 2)
        val field = obj::class.java.getField(split[0])
        field.isAccessible = true

        // In the case of a string with periods, run the function recursively until we get to the last item which is then set
        if (split.size > 1) return overwrite(field.get(obj), split[1], value)

        defaults.add(obj)
        defaults.add(field)
        defaults.add(field.get(obj))
        field.set(obj, value)
    }



    private val foreshadowBulletFlood = LaserBulletType().apply {
        length = 460f
        damage = 560f
        width = 75f
        lifetime = 65f
        lightningSpacing = 35f
        lightningLength = 5
        lightningDelay = 1.1f
        lightningLengthRand = 15
        lightningDamage = 50f
        lightningAngleRand = 40f
        largeHit = true
        lightningColor = Pal.heal
        lightColor = lightningColor
        shootEffect = Fx.greenLaserCharge
        sideAngle = 15f
        sideWidth = 0f
        sideLength = 0f
        colors = arrayOf(Color.clear, Color.clear, Color.clear) // TODO: Make this properly invisible
    }

}