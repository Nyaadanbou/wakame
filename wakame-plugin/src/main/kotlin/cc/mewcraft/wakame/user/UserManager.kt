package cc.mewcraft.wakame.user

import java.util.UUID

interface UserManager<P> {
    /**
     * 获取一个玩家的 [User] 实例.
     *
     * @param uniqueId 玩家的唯一标识符
     * @return 玩家的 [User] 实例
     */
    fun getUser(uniqueId: UUID): User<P>

    /**
     * 获取一个玩家的 [User] 实例.
     *
     * @param player 玩家
     * @return 玩家的 [User] 实例
     */
    fun getUser(player: P): User<P>
}
