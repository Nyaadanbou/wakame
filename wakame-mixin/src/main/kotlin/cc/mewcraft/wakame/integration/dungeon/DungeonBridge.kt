package cc.mewcraft.wakame.integration.dungeon

import cc.mewcraft.wakame.integration.party.Party
import org.bukkit.entity.Player

interface DungeonBridge {

    /**
     * 检查桥接是否已初始化.
     */
    fun inited(): Boolean = false

    // Dungeon Management

    fun hasDungeon(id: String): Result<Boolean>

    // Player Management

    /**
     * 让玩家进入指定的地牢.
     *
     * @param player 要进入地牢的玩家
     * @param dungeon 地牢的标识符
     * @return 进入地牢的结果
     */
    fun play(player: Player, dungeon: String): Result<Unit>

    /**
     * 让一组玩家进入指定的地牢. 第一个玩家将作为队长 (如果实现支持).
     *
     * @param players 要进入地牢的玩家集合
     * @param dungeon 地牢的标识符
     * @return 进入地牢的结果
     */
    fun play(players: List<Player>, dungeon: String): Result<Unit>

    /**
     * 将玩家从倒计时队列中移除.
     */
    fun unqueue(player: Player): Result<Unit>

    /**
     * 玩家是否正在等待进入地牢 (读倒计时).
     */
    fun isAwaitingDungeon(player: Player): Result<Boolean>

    /**
     * 检查玩家是否在地牢内.
     */
    fun isInsideDungeon(player: Player): Result<Boolean>

    /**
     * 使玩家离开当前所在小队 (如果有的话).
     */
    fun leaveParty(player: Player): Result<Unit>

    /**
     * 为玩家创建一个新的小队, 并返回新小队.
     */
    fun createParty(player: Player): Result<Party>

    /**
     * 为一组玩家创建一个新的小队, 并返回新小队. 第一个玩家将作为队长 (如果实现支持).
     */
    fun createParty(players: List<Player>): Result<Party>

    companion object : DungeonBridge {

        private var implementation: DungeonBridge = object : DungeonBridge {
            override fun hasDungeon(id: String): Result<Boolean> = Result.failure(NotImplementedError())
            override fun play(player: Player, dungeon: String): Result<Unit> = Result.failure(NotImplementedError())
            override fun play(players: List<Player>, dungeon: String): Result<Unit> = Result.failure(NotImplementedError())
            override fun unqueue(player: Player): Result<Unit> = Result.failure(NotImplementedError())
            override fun isAwaitingDungeon(player: Player): Result<Boolean> = Result.failure(NotImplementedError())
            override fun isInsideDungeon(player: Player): Result<Boolean> = Result.failure(NotImplementedError())
            override fun leaveParty(player: Player): Result<Unit> = Result.failure(NotImplementedError())
            override fun createParty(player: Player): Result<Party> = Result.failure(NotImplementedError())
            override fun createParty(players: List<Player>): Result<Party> = Result.failure(NotImplementedError())
        }

        fun setImplementation(implementation: DungeonBridge) {
            this.implementation = implementation
        }

        override fun inited(): Boolean = implementation.inited()
        override fun hasDungeon(id: String): Result<Boolean> = implementation.hasDungeon(id)
        override fun play(player: Player, dungeon: String): Result<Unit> = implementation.play(player, dungeon)
        override fun play(players: List<Player>, dungeon: String): Result<Unit> = implementation.play(players, dungeon)
        override fun unqueue(player: Player): Result<Unit> = implementation.unqueue(player)
        override fun isAwaitingDungeon(player: Player): Result<Boolean> = implementation.isAwaitingDungeon(player)
        override fun isInsideDungeon(player: Player): Result<Boolean> = implementation.isInsideDungeon(player)
        override fun leaveParty(player: Player): Result<Unit> = implementation.leaveParty(player)
        override fun createParty(player: Player): Result<Party> = implementation.createParty(player)
        override fun createParty(players: List<Player>): Result<Party> = implementation.createParty(players)
    }
}