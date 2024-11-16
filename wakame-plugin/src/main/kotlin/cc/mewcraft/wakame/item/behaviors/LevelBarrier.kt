package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.level
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.user.toUser
import net.kyori.adventure.extra.kotlin.text
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack

interface LevelBarrier : ItemBehavior {
    private object Default : LevelBarrier {
        override fun handleAttackEntity(player: Player, itemStack: ItemStack, damagee: Entity, event: NekoEntityDamageEvent) {
            tryCancelEvent(itemStack, player, event)
        }

        override fun handleInteract(player: Player, itemStack: ItemStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
            tryCancelEvent(itemStack, player, wrappedEvent.event)
        }

        override fun handleInteractAtEntity(player: Player, itemStack: ItemStack, clicked: Entity, event: PlayerInteractAtEntityEvent) {
            tryCancelEvent(itemStack, player, event)
        }

        override fun handleBreakBlock(player: Player, itemStack: ItemStack, event: BlockBreakEvent) {
            tryCancelEvent(itemStack, player, event)
        }

        override fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) {
            tryCancelEvent(itemStack, player, event)
        }

        override fun handleEquip(player: Player, itemStack: ItemStack, equipped: Boolean, event: ArmorChangeEvent) {
            if (equipped) {
                // 只处理取消穿戴动作, 脱下动作一律自然发生.
                // 防止物品在物品穿戴条件不满足时, 出现问题.
                tryCancelEvent(itemStack, player, event)
            }
        }

        override fun handleConsume(player: Player, itemStack: ItemStack, event: PlayerItemConsumeEvent) {
            tryCancelEvent(itemStack, player, event)
        }

        override fun handleSkillPrepareCast(caster: Player, itemStack: ItemStack, skill: Skill, event: PlayerSkillPrepareCastEvent) {
            tryCancelEvent(itemStack, caster, event)
        }

        private fun tryCancelEvent(itemStack: ItemStack, player: Player, e: Cancellable) {
            val itemLevel = itemStack.shadowNeko()?.level ?: 0
            val playerLevel = player.toUser().level
            if (itemLevel > playerLevel) {
                player.sendMessage(text { content("你的冒险等级不足以使用这个物品") })
                e.isCancelled = true
            }
        }
    }

    companion object Type : ItemBehaviorType<LevelBarrier> {
        override fun create(): LevelBarrier {
            return Default
        }
    }
}