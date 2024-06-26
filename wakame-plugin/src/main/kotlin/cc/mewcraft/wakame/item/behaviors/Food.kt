package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorFactory
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillCastContext
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 物品食用后的逻辑。
 */
interface Food : ItemBehavior {
    private object Default : Food, KoinComponent {
        private val skillCastManager: SkillCastManager by inject()

        override fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
            skills.forEach { skill ->
                val result = skillCastManager.tryCast(
                    skill,
                    SkillCastContext(
                        CasterAdapter.adapt(player),
                        TargetAdapter.adapt(player),
                        itemStack.tryNekoStack
                    )
                )
                SkillTicker.addChildren(result.skillTick)
            }
        }
    }

    companion object : ItemBehaviorFactory<Food> {
        override fun create(): Food {
            return Default
        }
    }
}


