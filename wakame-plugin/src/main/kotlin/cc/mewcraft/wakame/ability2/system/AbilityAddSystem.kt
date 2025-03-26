package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.AbilityCastUtils
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.item.logic.ItemSlotChanges
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.item2.getProperty
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.item.damage
import cc.mewcraft.wakame.util.item.isDamageable
import cc.mewcraft.wakame.util.item.maxDamage
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import org.bukkit.inventory.ItemStack

class AbilityAddSystem : IteratingSystem(
    family = Families.BUKKIT_PLAYER
) {
    override fun onTickEntity(entity: Entity) {
        val bukkitPlayer = entity[BukkitPlayerComponent].bukkitPlayer
        val itemSlotChanges = entity[ItemSlotChanges]
        val changingItems = itemSlotChanges.changingItems
        val user = bukkitPlayer.toUser()
        for ((slot, current) in changingItems) {
            current ?: continue
            if (!testLevel(user, current)) {
                continue
            }
            if (!testDurability(current)) {
                continue
            }
            val abilityObject = current.getProperty(ItemPropertyTypes.ABILITY) ?: continue
            AbilityCastUtils.idle(abilityObject, bukkitPlayer, bukkitPlayer, slot)
        }

        user.combo.reset()
    }

    private fun testLevel(user: User<*>, itemStack: ItemStack): Boolean {
        val itemLevel = itemStack.getData(ItemDataTypes.LEVEL)?.level
        if (itemLevel == null) {
            return true // 如果物品没有等级, 那么玩家的等级一定高于该物品 (0)
        }

        val playerLevel = user.level
        return itemLevel <= playerLevel
    }

    private fun testDurability(itemStack: ItemStack): Boolean {
        if (!itemStack.isDamageable) {
            return true // 如果物品有“无法破坏”或耐久组件不完整, 那么认为物品没有耐久度, 应该返回 true
        }

        if (itemStack.damage >= itemStack.maxDamage) {
            return false // 如果物品已经损坏, 那么应该返回 false
        }

        return true
    }
}