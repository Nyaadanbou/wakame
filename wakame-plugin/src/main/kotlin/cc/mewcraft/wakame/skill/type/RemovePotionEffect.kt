package cc.mewcraft.wakame.skill.type

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.NodeConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.registry.SkillConditionRegistry
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.condition.SkillCondition
import net.kyori.adventure.key.Key
import org.bukkit.potion.PotionEffectType
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.util.*

class RemovePotionEffect(
    override val key: Key,
    uniqueId: Provider<UUID>,
    trigger: Provider<Skill.Trigger>,
    conditionContexts: Provider<List<SkillCondition<*>>>,
    effectType: Provider<List<PotionEffectType>>
) : Skill {
    override val uniqueId: UUID by uniqueId
    override val trigger: Skill.Trigger by trigger
    override val conditions: List<SkillCondition<*>> by conditionContexts
    private val effectType: List<PotionEffectType> by effectType

    companion object Factory : SkillFactory<RemovePotionEffect> {
        override fun create(config: ConfigProvider, key: Key): RemovePotionEffect {
            val uuid = config.entry<UUID>("uuid")
            val trigger = config.optionalEntry<Skill.Trigger>("trigger").orElse(Skill.Trigger.NONE)
            val effectTypes = config.optionalEntry<List<PotionEffectType>>("effect_types").orElse(emptyList())

            val conditionsClasses = config.entry<List<ConfigurationNode>>("conditions")
                .map { nodes ->
                    nodes.mapNotNull nodes@{ node ->
                        val type = node.node("type").get<String>() ?: return@nodes null
                        val provider = SkillConditionRegistry.INSTANCE[type]
                        provider.provide(NodeConfigProvider(node, config.relPath))
                    }
                }

            return RemovePotionEffect(key, uuid, trigger, conditionsClasses, effectTypes)
        }
    }

    override fun castAt(target: Target.LivingEntity) {
        effectType.forEach { target.bukkitEntity.removePotionEffect(it) }
    }
}