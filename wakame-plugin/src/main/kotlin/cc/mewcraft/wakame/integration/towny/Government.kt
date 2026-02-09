package cc.mewcraft.wakame.integration.towny

import net.kyori.adventure.text.Component

/**
 * 代表一个组织: [Town] 或 [Nation].
 *
 * 共享了 [Town] 和 [Nation] 都有的 property 和 function.
 */
interface Government {

    /**
     * 返回该组织的名字.
     */
    val name: Component

    /**
     * 返回该组织的银行余额.
     */
    val balance: Double

    /**
     * 返回该组织是否可以展示在列表上.
     */
    val canShow: Boolean

    /**
     * 往该组织的银行里存钱.
     */
    fun withdraw(amount: Double)

    /**
     * 从该组织的银行里取钱.
     */
    fun deposit(amount: Double)
}