package cc.mewcraft.koish.feature.townhall.system

import cc.mewcraft.koish.feature.townhall.TownHallFeature
import cc.mewcraft.koish.feature.townhall.TownyFamilies
import cc.mewcraft.koish.feature.townhall.component.EnhancementType
import cc.mewcraft.koish.feature.townhall.component.TownHall
import cc.mewcraft.wakame.ecs.component.ParticleEffect
import cc.mewcraft.wakame.ecs.data.FixedPath
import cc.mewcraft.wakame.ecs.data.ParticleConfiguration
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.hook.impl.towny.TownyFamilies
import cc.mewcraft.wakame.hook.impl.towny.TownyHook
import cc.mewcraft.wakame.hook.impl.towny.component.Level
import cc.mewcraft.wakame.hook.impl.towny.component.EnhancementType
import cc.mewcraft.wakame.hook.impl.towny.component.TownHall
import cc.mewcraft.wakame.util.random
import com.destroystokyo.paper.ParticleBuilder
import com.github.quillraven.fleks.Entity
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.FurnaceSmeltEvent

object BuffFurnace : ListenableIteratingSystem(
    family = TownyFamilies.BUFF_FURNACE
) {
    override fun onTickEntity(entity: Entity) {
    }

    @EventHandler
    fun on(event: FurnaceBurnEvent) {
        val townHall = TownHallFeature.getTownHallEntity(event.block)?.unwrap() ?: return
        val buffFurnace = townHall[TownHall].enhancements[EnhancementType.BUFF_FURNACE]
            ?: return
        val randomInt = (0..100).random()
        if (randomInt < buffFurnace[Level].getConsumeChance()) {
            playFixedParticle(getFurnaceParticleLocation(event.block), buffFurnace, Particle.HAPPY_VILLAGER)
            event.setConsumeFuel(false)
        }
    }

    @EventHandler
    fun on(event: FurnaceSmeltEvent) {
        val townHall = TownHallFeature.getTownHallEntity(event.block)?.unwrap() ?: return
        val buffFurnace = townHall[TownHall].enhancements[EnhancementType.BUFF_FURNACE]
            ?: return
        val randomInt = (0..100).random()
        if (randomInt < buffFurnace[Level].getMultipleChance()) {
            playFixedParticle(getFurnaceParticleLocation(event.block), buffFurnace, Particle.HEART)
            event.result.amount += event.result.amount
        }
    }

    private fun playFixedParticle(location: Location, buffFurnace: Entity, particle: Particle) {
        buffFurnace.configure {
            it += ParticleEffect(
                world = location.world,
                ParticleConfiguration(
                    builderProvider = { loc ->
                        ParticleBuilder(particle)
                            .location(loc)
                            .receivers(8)
                            .extra(0.5)
                    },
                    particlePath = FixedPath(location),
                    amount = 1,
                    times = 1
                )
            )
        }
    }

    private fun Level.getConsumeChance(): Int {
        return 100 - (level * 10).coerceIn(0, 100)
    }

    private fun Level.getMultipleChance(): Int {
        return (level * 5).coerceIn(0, 100)
    }

    fun getFurnaceParticleLocation(furnace: Block): Location {
        val randomOffset = (0.5..1.0).random()

        return furnace.location.clone().apply { y += 0.6 + randomOffset }
    }
}