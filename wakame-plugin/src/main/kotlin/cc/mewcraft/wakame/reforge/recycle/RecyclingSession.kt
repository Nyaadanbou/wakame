package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.reforge.common.PriceInstance
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface RecyclingSession {
    val station: RecyclingStation
    val viewer: Player

    /**
     * 将物品加入待回收列表.
     * 如果物品无法回收, 则不会加入列表.
     *
     * @param item 要回收的物品, 应为直接引用
     * @param playerSlot 物品在玩家背包里的 slot
     * @return 执行结果, 可以用来看物品是否已成功加入列表
     */
    fun claimItem(item: ItemStack, playerSlot: Int): ClaimResult

    /**
     * 购买玩家要回收的物品.
     * 物品将从玩家背包中移除, 并且玩家将获得相应的金币.
     *
     * @param dryRun 是否空运行
     */
    fun purchase(dryRun: Boolean): PurchaseResult

    fun getClaim(displaySlot: Int): Claim?

    fun removeClaim(displaySlot: Int): Claim?

    /**
     * 重置回收列表.
     */
    fun reset()

    fun getAllClaims(): Collection<Claim>

    fun getAllInputs(): Array<ItemStack> // partially overlapped with getAllOffers

    /**
     * 封装了一个物品堆叠的回收信息.
     */
    sealed interface Claim {
        /**
         * 物品在回收列表里的 slot.
         */
        val displaySlot: Int

        /**
         * 物品在玩家背包里的 slot.
         * 用于将回收列表里的物品放回玩家背包里的原位置.
         */
        val playerSlot: Int

        /**
         * 被回收的物品堆叠 (直接引用).
         */
        val originalItem: ItemStack

        /**
         * 物品堆叠的回收价格.
         */
        val priceInstance: PriceInstance
    }

    /**
     * 玩家添加待回收物品的结果.
     */
    sealed interface ClaimResult {
        // 这些内容要在[确认回收]的按钮上显示
        val description: List<Component>

        // 该结果代表物品无法回收
        interface Failure : ClaimResult

        // 该结果代表物品已加入待回收列表
        interface Success : ClaimResult {
            val displaySlot: Int
        }
    }

    /**
     * 系统购买待回收物品的结果.
     */
    sealed interface PurchaseResult {
        val description: List<Component>

        // 结果通常是 Success, 有异常抛出或无法给玩家增加余额才会是 Failure

        interface Failure : PurchaseResult

        interface Success : PurchaseResult {
            val minPrice: Double // 最低价格
            val maxPrice: Double // 最高价格
            val fixPrice: Double // 实际价格
        }
    }
}
