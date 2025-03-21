package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.extension.damage
import cc.mewcraft.wakame.item.extension.isDamageable
import cc.mewcraft.wakame.item.extension.maxDamage
import cc.mewcraft.wakame.item.extension.playerAbilities
import cc.mewcraft.wakame.item.logic.ItemSlotChanges
import cc.mewcraft.wakame.item.wrap
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import xyz.xenondevs.commons.collections.takeUnlessEmpty

class AbilityAddSystem : IteratingSystem(
    family = Families.BUKKIT_PLAYER
) {
    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayerComponent].bukkitPlayer
        val itemSlotChanges = entity[ItemSlotChanges]
        val changingItems = itemSlotChanges.changingItems
        val user = player.toUser()
        for ((slot, current, _) in changingItems) {
            val nekoStack = current?.wrap() ?: continue
            if (!testLevel(user, nekoStack)) {
                continue
            }
            if (!testDurability(nekoStack)) {
                continue
            }
            val abilities = nekoStack.playerAbilities.takeUnlessEmpty() ?: continue
            abilities.forEach { ability -> ability.record(player, null, slot) }
        }

        user.combo.reset()
    }

    private fun testLevel(user: User<*>, nekoStack: NekoStack): Boolean {
        val itemLevel = nekoStack.components.get(ItemComponentTypes.LEVEL)?.level
        if (itemLevel == null) {
            return true // 如果物品没有等级, 那么玩家的等级一定高于该物品 (0)
        }

        val playerLevel = user.level
        return itemLevel <= playerLevel
    }

    private fun testDurability(nekoStack: NekoStack): Boolean {
        if (!nekoStack.isDamageable) {
            return true // 如果物品有“无法破坏”或耐久组件不完整, 那么认为物品没有耐久度, 应该返回 true
        }

        if (nekoStack.damage >= nekoStack.maxDamage) {
            return false // 如果物品已经损坏, 那么应该返回 false
        }

        return true
    }
}