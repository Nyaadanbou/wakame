package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.recycle.RecyclingStation
import cc.mewcraft.wakame.reforge.repair.RepairingTable

interface BlacksmithStation {
    val primaryMenuSettings: BasicMenuSettings
    val recyclingMenuSettings: BasicMenuSettings
    val repairingMenuSettings: BasicMenuSettings

    val recyclingStation: RecyclingStation
    val repairingTable: RepairingTable

    /**
     * 获取回收站用于存放物品的容器的大小 (i.e. 容器的格子数量).
     */
    val recyclingInventorySize: Int

    companion object Shared {
        fun calculateRecyclingInventorySize(settings: BasicMenuSettings): Int =
            settings.structure.joinToString().count { it == 'i' }
    }
}