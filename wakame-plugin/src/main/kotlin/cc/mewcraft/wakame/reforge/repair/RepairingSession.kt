package cc.mewcraft.wakame.reforge.repair

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

interface RepairingSession {
    val table: RepairingTable
    val viewer: Player

    fun getClaim(displaySlot: Int): Claim?

    fun removeClaim(displaySlot: Int): Claim?

    fun clearClaims()

    fun getAllClaims(): List<Claim>

    fun registerClaims(inventory: PlayerInventory)

    interface Claim {
        /**
         * 该 claim 的物品堆叠,
         * 也是要被修复的物品堆叠.
         *
         * 注意: 该物品堆叠是玩家背包里物品的直接引用.
         */
        val originalItem: ItemStack

        // 该 claim 显示在菜单里的位置
        val displaySlot: Int

        // 该 claim 的花费
        val repairCost: RepairCost

        /**
         * 修复物品. 该函数应该在 [RepairCost.test] 返回 `true` 时调用.
         */
        fun repair(player: Player)
    }

    interface RepairCost {
        val description: List<Component>
        fun test(player: Player): Boolean
        fun take(player: Player)
    }
}