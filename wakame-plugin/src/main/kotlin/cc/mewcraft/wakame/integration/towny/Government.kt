package cc.mewcraft.wakame.integration.towny

import net.kyori.adventure.text.Component

/**
 * 代表一个组织: [Town] 或 [Nation].
 *
 * 共享了 [Town] 和 [Nation] 都有的 property 和 function.
 */
interface Government {

    /**
     * 组织的名字.
     */
    val name: Component
}