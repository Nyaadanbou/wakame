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
    @EventHandler(priority = EventPriority.LOWEST)
    fun on(event: AsyncPlayerConnectionConfigureEvent) {
        val conn = event.connection
        val audience = conn.audience
        val id = audience.get(Identity.UUID).getOrNull()
        if (id != null) {
            UserManager.get(id)
        } else {
            LOGGER.error("Failed to get player UUID from connection audience, skipping User initialization.")
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
        return data.computeIfAbsent(playerId) {
            LOGGER.info("[UserManager] Creating User for player with UUID: $playerId")
            User(it)
        }
    }

    fun remove(playerId: UUID) {
        data.remove(playerId)
    }

    fun remove(player: Player) {
        remove(player.uniqueId)
    }
}

/**
 * 封装了 Koish 系统下的一个玩家所需要的所有数据.
 */
class User(
    val playerId: UUID,
) {
    /**
     * 标记 Koish 是否应该处理该玩家背包里的物品变化.
     *
     * 当该值为 `false` 时, Koish 不应该处理背包物品的变化;
     * 这意味着玩家背包里的任何物品都不会提供任何来自 Koish 的效果 (例如: 属性, 技能).
     *
     * 当该值为 `true` 时, Koish 应该处理背包物品的变化;
     * 也就是说 Koish 会分析背包里的物品变化并将对应的效果提供给玩家 (或从玩家身上移除).
     *
     * 并不是所有时刻 Koish 都应该处理玩家背包物品的变化.
     */
    var initialized: Boolean =
        false

    val player: Player?
        get() = Bukkit.getPlayer(playerId)

    val powerLevel: Int
        get() = PlayerLevelIntegration.getOrDefault(playerId, 1)

    val itemSlotChanges: ItemSlotChanges by ValueOrEmpty(
        ItemSlotChanges::empty,
        ItemSlotChanges::create
    )

    val attributeContainer: AttributeMap by ValueOrEmpty(
        AttributeMapFactory.INSTANCE::empty,
        AttributeMapFactory.INSTANCE::create
    )

    val itemCooldownContainer: ItemCooldownContainer by ValueOrEmpty(
        ItemCooldownContainer::empty,
        ItemCooldownContainer::minecraft
    )

    val inscriptionContainer: KizamiMap by ValueOrEmpty(
        KizamiMap::empty,
        KizamiMap::create
    )

    /**
     * 返回初始化后的数据, 或空数据 (如果尚未初始化).
     */
    inner class ValueOrEmpty<T>(
        private val emptyConstructor: () -> T,
        private val valueConstructor: (Player) -> T,
    ) : ReadOnlyProperty<Any, T> {
        private var valueOrNull: T? = null

        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            val valueOrNull = valueOrNull
            if (valueOrNull != null) {
                return valueOrNull
            } else if (initialized) {
                val player = player ?: return emptyConstructor()
                val value = valueConstructor(player)
                this.valueOrNull = value
                return value
            } else {
                return emptyConstructor()
            }
        }
    }
}