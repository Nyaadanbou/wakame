package cc.mewcraft.wakame.ability.instance

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.Target
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.bukkit.potion.PotionEffectType

class RemovePotionEffect(
    private val effectType: List<PotionEffectType>
) : Ability {
    override val key: Key = Key(NekoNamespaces.ABILITY, "remove_potion_effect")
    override fun castAt(target: Target.LivingEntity) {
        effectType.forEach { target.bukkitEntity.removePotionEffect(it) }
    }
}