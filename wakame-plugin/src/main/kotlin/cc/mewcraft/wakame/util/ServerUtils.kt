package cc.mewcraft.wakame.util

import io.papermc.paper.ServerBuildInfo
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.CraftServer

object ServerUtils {

    val SERVER_SOFTWARE by lazy {
        when (ServerBuildInfo.buildInfo().brandId()) {
            Key.key("papermc", "paper") -> ServerSoftware.PAPER
            Key.key("papermc", "folia") -> ServerSoftware.FOLIA
            Key.key("pufferfish", "pufferfish") -> ServerSoftware.PUFFERFISH
            Key.key("purpurmc", "purpur") -> ServerSoftware.PURPUR
            else -> ServerSoftware.UNKNOWN
        }
    }

    fun isReload(): Boolean = (Bukkit.getServer() as CraftServer).reloadCount != 0

}

enum class ServerSoftware(private val upstream: ServerSoftware? = null) {

    UNKNOWN,
    PAPER(),
    FOLIA(PAPER),
    PUFFERFISH(PAPER),
    PURPUR(PUFFERFISH);

    val superSoftwares: List<ServerSoftware>

    init {
        val superSoftwares = ArrayList<ServerSoftware>()
        var software: ServerSoftware? = this
        while (software != null) {
            superSoftwares.add(software)
            software = software.upstream
        }
        this.superSoftwares = superSoftwares
    }

}