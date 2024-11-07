package cc.mewcraft.wakame.reforge.repair

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
         * 该 [Claim] 的物品堆叠,
         * 也是要被修复的物品堆叠.
         *
         * 该物品堆叠是玩家背包里物品的 *直接引用*.
         * 这样当我们修改这个物品堆叠的耐久度时,
         * 玩家背包里的那个也会被同时修改.
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
        /**
         * 修复该物品的花费(货币).
         */
        val value: Double

        /**
         * 测试玩家是否有足够的 [value] 货币来修复该物品.
         */
        fun test(player: Player): Boolean

        /**
         * 从玩家身上扣除 [value] 货币.
         */
        fun take(player: Player)
    }
}