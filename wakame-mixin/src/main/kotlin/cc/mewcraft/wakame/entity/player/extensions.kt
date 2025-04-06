@file:JvmName("PlayerExtensions")

package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.player.component.InventoryListenable
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelManager
import cc.mewcraft.wakame.kizami2.KizamiMap
import org.bukkit.entity.Player

// ------------
// 快速访问 Player 对象上的 Koish 数据
// ------------

/**
 * 玩家的冒险等级.
 */
val Player.koishLevel: Int
    get() = PlayerLevelManager.getOrDefault(uniqueId, 1)

/**
 * 玩家的属性容器.
 */
val Player.attributeContainer: AttributeMap
    get() = koishify()[AttributeMap]

/**
 * 玩家的铭刻容器.
 */
val Player.kizamiContainer: KizamiMap
    get() = koishify()[KizamiMap]

/**
 * @see InventoryListenable
 */
@get:Synchronized
@set:Synchronized
var Player.isInventoryListenable: Boolean
    get() = koishify().has(InventoryListenable)
    set(value) = if (value) koishify() += InventoryListenable
    else koishify() -= InventoryListenable
