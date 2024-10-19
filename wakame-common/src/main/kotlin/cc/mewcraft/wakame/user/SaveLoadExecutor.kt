package cc.mewcraft.wakame.user

import io.papermc.paper.persistence.PersistentDataContainerView
import org.bukkit.persistence.PersistentDataContainer

interface SaveLoadExecutor {
    fun saveTo(pdc: PersistentDataContainer)
    fun loadFrom(pdc: PersistentDataContainerView)
}