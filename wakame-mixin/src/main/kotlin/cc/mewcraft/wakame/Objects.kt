@file:JvmName("Objects")

package cc.mewcraft.wakame

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Server

val MM: MiniMessage = MiniMessage.miniMessage()
val SERVER: Server
    get() = Bukkit.getServer()