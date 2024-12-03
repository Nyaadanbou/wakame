package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.LOGGER
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

/**
 * 负责在玩家进出服务器 (包括跨服) 时同步玩家资源.
 *
 * 实现应该修复如下几个问题:
 *
 * - [Minecraft 原版本身的问题: MC-17876](https://bugs.mojang.com/browse/MC-17876)
 * - 在服务器安装了跨服同步系统时, 玩家的资源也能够正确同步
 */
internal object ResourceSynchronizer {
    private val HEALTH_KEY = NamespacedKey("wakame", "player_health")
    private val MANA_KEY = NamespacedKey("wakame", "player_mana")

    /**
     * 保存玩家的资源, 一般在玩家退出服务器或者切换服务器时调用.
     */
    fun save(player: Player) {
        val pdc = player.persistentDataContainer

        val health = player.health
        pdc.set(HEALTH_KEY, PersistentDataType.DOUBLE, health)
        LOGGER.info("[${player.name}] Saved player health: $health")

        // 考虑到 Wynn 没有保存魔法值, 我们也暂时不保存魔法值.
        // 如此设计并非无脑照搬. 魔法值上限本就不高, 并且恢复很快.

        // val mana = player.toUser().resourceMap.current(ResourceTypeRegistry.MANA)
        // pdc.set(MANA_KEY, PersistentDataType.INTEGER, mana)
    }

    /**
     * 保存所有在线玩家的资源, 一般在服务器关闭时调用.
     */
    fun saveAll() {
        Bukkit.getOnlinePlayers().toList().forEach { save(it) }
    }

    /**
     * 加载玩家的资源, 一般在玩家进入服务器或者切换服务器时调用.
     */
    fun load(player: Player) {
        val pdc = player.persistentDataContainer

        val health = pdc.get(HEALTH_KEY, PersistentDataType.DOUBLE)
        if (health != null) {
            try {
                player.health = health
                LOGGER.info("[${player.name}] Loaded player health: $health / ${player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value}")
            } catch (e: Exception) {
                LOGGER.error("[${player.name}] ${e.message}")
            }
        }

        // 考虑到 Wynn 没有保存魔法值, 我们也暂时不保存魔法值
        // val mana = pdc.get(MANA_KEY, PersistentDataType.INTEGER)
        // if (mana != null) {
        //     player.toUser().resourceMap.set(ResourceTypeRegistry.MANA, mana)
        // }
    }
}