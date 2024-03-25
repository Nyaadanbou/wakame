package cc.mewcraft.wakame.user

import java.util.UUID

interface UserManager<P> {
    fun getPlayer(uniqueId: UUID): User<P>
    fun getPlayer(player: P): User<P>
}
