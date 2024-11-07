package cc.mewcraft.wakame.reforge.blacksmith

import cc.mewcraft.wakame.reforge.recycle.RecyclingStation
import cc.mewcraft.wakame.reforge.repair.RepairingTable

interface BlacksmithStation {
    val recyclingStation: RecyclingStation
    val repairingTable: RepairingTable
}