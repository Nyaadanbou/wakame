package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.tick.*
import cc.mewcraft.wakame.tick.Ticker
import cc.mewcraft.wakame.tick.TickResult
import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.ThrownPotion
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


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

        override fun cast(context: SkillContext): SkillTick<PotionDrop> {
            return PotionDropTick(context, this, triggerConditionGetter.interruptTriggers, triggerConditionGetter.forbiddenTriggers)
        }
    }
}

private class PotionDropTick(
    context: SkillContext,
    skill: PotionDrop,
    override val interruptTriggers: Provider<TriggerConditions>,
    override val forbiddenTriggers: Provider<TriggerConditions>
) : AbstractPlayerSkillTick<PotionDrop>(skill, context) {
    override fun tickCast(tickCount: Long): TickResult {
        val location = TargetUtil.getLocation(context) ?: return TickResult.INTERRUPT
        PotionDropSupport.ticker.addTick(PotionDropEffectTick(this, location.bukkitLocation.add(.0, 3.0, .0)))
        return TickResult.ALL_DONE
    }
}

private class PotionDropEffectTick(
    tick: PotionDropTick,
    private val location: Location
) : AbstractSkillTick<PotionDrop>(tick.skill, tick.context) {
    override fun tick(): TickResult {
        if (!checkConditions())
            return TickResult.ALL_DONE
        if (tickCount % 20 == 0L) {
            val potionItem = ItemStack(Material.SPLASH_POTION)
            val potionMeta = potionItem.itemMeta as PotionMeta
            potionMeta.basePotionType = skill.effectTypes.random()
            val thrownPotion = location.world.spawnEntity(location, EntityType.POTION) as ThrownPotion
            thrownPotion.potionMeta = potionMeta
            location.world.strikeLightning(location)
        }

        if (tickCount >= 200) {
            return TickResult.ALL_DONE
        }

        return TickResult.CONTINUE_TICK
    }
}

private object PotionDropSupport : KoinComponent {
    val ticker: Ticker by inject()
}