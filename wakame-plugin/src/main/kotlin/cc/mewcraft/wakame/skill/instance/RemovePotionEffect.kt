package cc.mewcraft.wakame.skill.instance

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillSerializer
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.bukkit.potion.PotionEffectType
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type
import java.util.*

class RemovePotionEffect(
    override val uniqueId: UUID,
    private val effectType: List<PotionEffectType>
) : Skill {
    override val key: Key = Key(NekoNamespaces.SKILL, "remove_potion_effect")
    override fun castAt(target: Target.LivingEntity) {
        effectType.forEach { target.bukkitEntity.removePotionEffect(it) }
    }
}

internal object RemovePotionEffectSerializer : SkillSerializer<RemovePotionEffect> {
    override fun deserialize(type: Type?, node: ConfigurationNode): RemovePotionEffect {
        val uuid = node.node("uuid").krequire<UUID>()
        val effectTypes = node.node("effect_types").get<List<PotionEffectType>>().orEmpty()
        return RemovePotionEffect(uuid, effectTypes)
    }
}