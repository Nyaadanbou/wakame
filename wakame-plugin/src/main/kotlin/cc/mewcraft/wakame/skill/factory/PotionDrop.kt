package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.*
import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.ThrownPotion
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType


interface PotionDrop : Skill {

    /**
     * 药水效果类型
     */
    val effectTypes: List<PotionType>

    companion object Factory : SkillFactory<PotionDrop> {
        override fun create(key: Key, config: ConfigProvider): PotionDrop {
            val effectTypes = config.optionalEntry<List<PotionType>>("effect_types").orElse(emptyList())
            return DefaultImpl(key, config, effectTypes)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
        effectTypes: Provider<List<PotionType>>,
    ) : PotionDrop, SkillBase(key, config) {
        override val effectTypes: List<PotionType> by effectTypes

        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick {
            return Tick(context, triggerConditionGetter.interruptTriggers, triggerConditionGetter.forbiddenTriggers)
        }

        private inner class Tick(
            context: SkillContext,
            override val interruptTriggers: TriggerConditions,
            override val forbiddenTriggers: TriggerConditions
        ) : AbstractPlayerSkillTick(this@DefaultImpl, context) {
            override fun tickCast(): TickResult {
                val location = when {
                    SkillContextKey.TARGET_LOCATION in context -> {
                        context.getOrThrow(SkillContextKey.TARGET_LOCATION).bukkitLocation
                    }
                    SkillContextKey.CASTER_ENTITY in context -> {
                        context.getOrThrow(SkillContextKey.CASTER_ENTITY).bukkitEntity.location
                    }
                    else -> return TickResult.INTERRUPT
                }
                SkillTicker.addChildren(PotionTick(location.add(.0, 3.0, .0)))
                return TickResult.ALL_DONE
            }

            private inner class PotionTick(
                private val location: Location
            ) : SkillTick {
                override val skill: Skill = this@DefaultImpl
                override val context: SkillContext = this@Tick.context

                private var counter: Int = 0

                override fun tick(): TickResult {
                    if (counter % 20 == 0) {
                        val potionItem = ItemStack(Material.SPLASH_POTION)
                        val potionMeta = potionItem.itemMeta as PotionMeta
                        potionMeta.basePotionType = effectTypes.random()
                        val thrownPotion = location.world.spawnEntity(location, EntityType.POTION) as ThrownPotion
                        thrownPotion.potionMeta = potionMeta
                        location.world.strikeLightning(location)
                    }

                    if (counter >= 200) {
                        return TickResult.ALL_DONE
                    }
                    counter++

                    return TickResult.CONTINUE_TICK
                }
            }
        }
    }
}