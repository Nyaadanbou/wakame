package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillBase
import cc.mewcraft.wakame.skill.TargetUtil
import cc.mewcraft.wakame.skill.TriggerConditions
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.AbstractSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.tick.Ticker
import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.ThrownPotion
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get


interface PotionDrop : Skill {

    /**
     * 药水效果类型
     */
    val effectTypes: List<PotionType>

    companion object Factory : SkillFactory<PotionDrop> {
        override fun create(key: Key, config: ConfigurationNode): PotionDrop {
            val effectTypes = config.node("effect_types").get<List<PotionType>>() ?: emptyList()
            return Impl(key, config, effectTypes)
        }
    }

    private class Impl(
        override val key: Key,
        config: ConfigurationNode,
        override val effectTypes: List<PotionType>,
    ) : PotionDrop, SkillBase(key, config) {
        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick<PotionDrop> {
            return PotionDropTick(context, this, triggerConditionGetter.interrupt, triggerConditionGetter.forbidden)
        }
    }
}

private class PotionDropTick(
    context: SkillContext,
    skill: PotionDrop,
    override val interruptTriggers: TriggerConditions,
    override val forbiddenTriggers: TriggerConditions
) : AbstractPlayerSkillTick<PotionDrop>(skill, context) {
    override fun tickCast(tickCount: Long): TickResult {
        val location = TargetUtil.getLocation(context) ?: return TickResult.INTERRUPT
        Ticker.INSTANCE.schedule(PotionDropEffectTick(this, location.bukkitLocation.add(.0, 3.0, .0)))
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
        }

        if (tickCount >= 200) {
            return TickResult.ALL_DONE
        }

        return TickResult.CONTINUE_TICK
    }
}