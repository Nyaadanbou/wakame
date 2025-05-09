package cc.mewcraft.wakame.item2.behavior.impl.weapon

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 代表一种武器的物品行为.
 */
interface Weapon : ItemBehavior {

    /**
     * 玩家使用该武器类型可造成的 [DamageMetadata].
     * 返回 `null` 会让 Koish 系统取消本次伤害事件.
     *
     * 注意: 被取消的伤害事件仍然会触发 [PlayerItemLeftClickEvent].
     *
     * 警告: 不要在该方法中的实现中写攻击的附带效果!
     */
    fun generateDamageMetadata(player: Player, itemstack: ItemStack): DamageMetadata? {
        return PlayerDamageMetadata.INTRINSIC_ATTACK
    }

}