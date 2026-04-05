package cc.mewcraft.wakame.item.behavior.impl.weapon

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.item.behavior.impl.SimpleInteract
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 代表一种武器的物品行为.
 */
sealed interface Weapon : SimpleInteract {

    /**
     * 玩家使用该武器类型可造成的 [DamageMetadata].
     * 返回 `null` 会让 Koish 系统取消本次伤害事件.
     *
     * 警告: 不要在该方法中的实现中写攻击的附带效果!
     * 会导致伤害事件取消时附带效果没有被取消!
     *
     * 进一步优化:
     * 不使用源生伤害事件的武器物品行为覆写 [handleSimpleAttack] 方法时,
     * 建议使用会取消事件的返回值, 直接不触发源生伤害事件, 节约性能.
     */
    fun generateDamageMetadata(player: Player, itemstack: ItemStack): DamageMetadata? {
        return null
    }
}