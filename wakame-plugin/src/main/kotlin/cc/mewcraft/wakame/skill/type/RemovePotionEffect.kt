package cc.mewcraft.wakame.skill.type

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import net.kyori.adventure.key.Key
import org.bukkit.potion.PotionEffectType
import java.util.*

class RemovePotionEffect(
    override val key: Key,
    uniqueId: Provider<UUID>,
    conditions: Provider<SkillConditionGroup>,
    effectType: Provider<List<PotionEffectType>>
) : ConfiguredSkill {
    override val uniqueId: UUID by uniqueId
    override val conditions: SkillConditionGroup by conditions
    private val effectType: List<PotionEffectType> by effectType

    companion object Type : SkillType<RemovePotionEffect> {
        override fun create(config: ConfigProvider, key: Key): RemovePotionEffect {
            val uuid = config.entry<UUID>("uuid")
            val effectTypes = config.optionalEntry<List<PotionEffectType>>("effect_types").orElse(emptyList())
            val conditions = config.optionalEntry<SkillConditionGroup>("conditions").orElse(EmptySkillConditionGroup)

            return RemovePotionEffect(key, uuid, conditions, effectTypes)
        }
    }

    override fun castAt(target: Target.LivingEntity) {
        effectType.forEach { target.bukkitEntity.removePotionEffect(it) }
    }
}