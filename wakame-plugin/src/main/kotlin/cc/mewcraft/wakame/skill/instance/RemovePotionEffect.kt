package cc.mewcraft.wakame.skill.instance

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillSerializer
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.condition.DurabilityCondition
import cc.mewcraft.wakame.skill.condition.SkillCondition
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.bukkit.potion.PotionEffectType
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type
import java.util.UUID

class RemovePotionEffect(
    override val uniqueId: UUID,
    override val trigger: Skill.Trigger,
    override val conditions: List<SkillCondition<*>>,
    private val effectType: List<PotionEffectType>
) : Skill {
    override val key: Key = Key(Namespaces.SKILL, "remove_potion_effect")
    override fun castAt(target: Target.LivingEntity) {
        effectType.forEach { target.bukkitEntity.removePotionEffect(it) }
    }
}

internal object RemovePotionEffectSerializer : SkillSerializer<RemovePotionEffect> {
    override fun deserialize(type: Type?, node: ConfigurationNode): RemovePotionEffect {
        val uuid = node.node("uuid").krequire<UUID>()
        val trigger = node.node("trigger").get<Skill.Trigger>() ?: Skill.Trigger.NONE
        val conditions = node.node("conditions").get<List<SkillCondition<*>>>().orEmpty()
        val effectTypes = node.node("effect_types").get<List<PotionEffectType>>().orEmpty()
        // TODO: implement conditions serializer
        return RemovePotionEffect(uuid, trigger, listOf(DurabilityCondition), effectTypes)
    }
}