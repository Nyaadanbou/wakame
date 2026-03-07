package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.attribute.AttributeMapFactory
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.item.ItemSlotChanges
import cc.mewcraft.wakame.kizami.KizamiMap
import cc.mewcraft.wakame.util.decorate
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import net.kyori.adventure.identity.Identity
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.slf4j.Logger
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object UserListener : Listener {
    private val logger: Logger = LOGGER.decorate(UserListener::class)

    @EventHandler(priority = EventPriority.LOWEST)
    fun on(event: AsyncPlayerConnectionConfigureEvent) {
        val conn = event.connection
        val audience = conn.audience
        val id = audience.get(Identity.UUID).getOrNull()
        if (id != null) {
            UserManager.create(id)
        } else {
            logger.error("Failed to get player UUID from connection audience, skipping User initialization.")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerQuitEvent) {
        val player = event.player
        UserManager.remove(player)
    }
}

object UserManager : Listener {
    private val logger: Logger = LOGGER.decorate(UserManager::class)
    private val data: ConcurrentHashMap<UUID, User> = ConcurrentHashMap()

    val users: Iterable<User>
        get() = data.values

    fun get(player: Player): User {
        return get(player.uniqueId)
    }

    fun get(playerId: UUID): User {
        return data[playerId] ?: EmptyUser
    }

    fun create(playerId: UUID): User {
        return data.computeIfAbsent(playerId) {
            logger.info("Creating user object for player $playerId")
            SimpleUser(it)
        }
    }

    fun remove(playerId: UUID) {
        data.remove(playerId)
        logger.info("Removed user object for player $playerId")
    }

    fun remove(player: Player) {
        remove(player.uniqueId)
    }
}

/**
 * 封装了 Koish 系统下的一个玩家所需要的所有数据.
 */
interface User {
    val playerId: UUID
    var synced: Boolean
    val player: Player?
    val powerLevel: Int
    val itemSlotChanges: ItemSlotChanges
    val attributeContainer: AttributeMap
    val itemCooldownContainer: ItemCooldownContainer
    val inscriptionContainer: KizamiMap

    /**
     * 该 [User] 是否为空实现.
     */
    val isEmpty: Boolean
}

/**
 * 空的 [User] 实现. 所有属性返回默认/空值.
 *
 * 当玩家不在线或 [User] 尚未创建时, [UserManager.get] 返回此实例.
 */
private object EmptyUser : User {
    private val NIL_UUID: UUID = UUID(0, 0)

    override val playerId: UUID get() = NIL_UUID
    override var synced: Boolean
        get() = false
        set(_) {}
    override val player: Player? get() = null
    override val powerLevel: Int get() = 1
    override val itemSlotChanges: ItemSlotChanges get() = ItemSlotChanges.empty()
    override val attributeContainer: AttributeMap get() = AttributeMapFactory.empty()
    override val itemCooldownContainer: ItemCooldownContainer get() = ItemCooldownContainer.empty()
    override val inscriptionContainer: KizamiMap get() = KizamiMap.empty()
    override val isEmpty: Boolean get() = true
}

private class SimpleUser(
    override val playerId: UUID,
) : User {
    /**
     * 标记该玩家的数据是否已经跨服同步完成.
     *
     * 当服务端只有一个时, 该值在玩家加入服务器时就会被设置为 `true`.
     *
     * 当该值为 `false` 时, Koish 不应该处理背包物品的变化;
     * 这意味着玩家背包里的任何物品都不会提供任何来自 Koish 的效果 (例如: 属性, 技能).
     *
     * 当该值为 `true` 时, Koish 应该处理背包物品的变化;
     * 也就是说 Koish 会分析背包里的物品变化并将对应的效果提供给玩家 (或从玩家身上移除).
     *
     * 并不是所有时刻 Koish 都应该处理玩家背包物品的变化.
     */
    @Volatile
    override var synced: Boolean =
        false

    override val player: Player?
        get() = Bukkit.getPlayer(playerId)

    override val powerLevel: Int
        get() = PlayerLevelIntegration.getOrDefault(playerId, 1)

    override val itemSlotChanges: ItemSlotChanges by ValueOrEmpty(
        ItemSlotChanges::empty,
        ItemSlotChanges::create
    )

    override val attributeContainer: AttributeMap by ValueOrEmpty(
        AttributeMapFactory::empty,
        AttributeMapFactory::create
    )

    override val itemCooldownContainer: ItemCooldownContainer by ValueOrEmpty(
        ItemCooldownContainer::empty,
        ItemCooldownContainer::minecraft
    )

    override val inscriptionContainer: KizamiMap by ValueOrEmpty(
        KizamiMap::empty,
        KizamiMap::create
    )

    override val isEmpty: Boolean get() = false

    /**
     * 返回初始化后的数据, 或空数据 (如果尚未初始化).
     */
    inner class ValueOrEmpty<T>(
        private val emptyProvider: () -> T,
        private val valueProvider: (Player) -> T,
    ) : ReadOnlyProperty<Any, T> {
        @Volatile
        private var value: T? = null

        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            val current = value
            if (current != null) {
                return current
            }
            if (!synced) {
                return emptyProvider()
            }
            synchronized(this) {
                val existing = value
                if (existing != null) {
                    return existing
                }
                val player = player ?: return emptyProvider()
                val value = valueProvider(player)
                this.value = value
                return value
            }
        }
    }
}