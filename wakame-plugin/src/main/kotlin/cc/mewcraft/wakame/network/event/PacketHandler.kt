package cc.mewcraft.wakame.network.event

import org.bukkit.event.EventPriority

annotation class PacketHandler(
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreIfCancelled: Boolean = false
)