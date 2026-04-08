package cc.mewcraft.wakame.integration.party

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

interface Party {

    val name: Component

    val id: UUID

    val members: Set<UUID>

    // members who are online
    val players: Set<Player>

    fun addMember(id: UUID)

    fun removeMember(id: UUID)

    fun disband()

    companion object {

        /**
         * A no-operation [Party] implementation.
         */
        @JvmField
        val NO_OP: Party = object : Party {
            override val name: Component
                get() = Component.text("No-Op Party")
            override val id: UUID
                get() = UUID(0, 0)
            override val members: Set<UUID>
                get() = emptySet()
            override val players: Set<Player>
                get() = members.mapNotNull(Bukkit::getPlayer).toSet()

            override fun addMember(id: UUID) = Unit
            override fun removeMember(id: UUID) = Unit
            override fun disband() = Unit
        }
    }
}