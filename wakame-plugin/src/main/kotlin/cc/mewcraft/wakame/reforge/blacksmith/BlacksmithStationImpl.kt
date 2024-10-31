package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.reforge.recycle.RecyclingStation
import cc.mewcraft.wakame.reforge.recycle.WtfRecyclingStation
import cc.mewcraft.wakame.reforge.repair.RepairingTable
import cc.mewcraft.wakame.reforge.repair.WtfRepairingTable

internal object WtfBlacksmithStation : BlacksmithStation {
    override val recyclingStation: RecyclingStation = WtfRecyclingStation
    override val repairingTable: RepairingTable = WtfRepairingTable
}