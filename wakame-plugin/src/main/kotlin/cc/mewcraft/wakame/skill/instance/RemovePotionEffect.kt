package cc.mewcraft.wakame.skill.instance

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.bukkit.potion.PotionEffectType

class RemovePotionEffect(
    private val effectType: List<PotionEffectType>
) : Skill {
    override val key: Key = Key(NekoNamespaces.SKILL, "remove_potion_effect")
    override fun castAt(target: Target.LivingEntity) {
        effectType.forEach { target.bukkitEntity.removePotionEffect(it) }
    }
}