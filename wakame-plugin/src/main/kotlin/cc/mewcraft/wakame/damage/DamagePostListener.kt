@file:Suppress(
    "UnstableApiUsage"
)

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.util.event
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority

/**
 * 监听萌芽伤害事件, 使其遵循保护系统的规则.
 */
@Init(
    stage = InitStage.POST_WORLD,
)
internal object DamagePostListener {

    @InitFun
    fun init() {
        event<NekoEntityDamageEvent>(EventPriority.HIGHEST, true) {
            val damager = it.damageSource.causingEntity as? Player ?: return@event
            val damagee = it.damagee as? LivingEntity ?: return@event

            it.isCancelled = !ProtectionManager.canHurtEntity(damager, damagee, null)
        }
    }

}