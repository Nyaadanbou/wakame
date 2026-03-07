@file:JvmName("PlayerExt")

package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.kizami.KizamiMap
import org.bukkit.entity.Player

// ------------
// 快速访问 Player 对象上的 Koish 数据
// ------------

/**
 * 玩家的冒险等级.
 */
val Player.koishLevel: Int
    get() = UserManager.get(this).powerLevel

/**
 * 玩家的物品冷却.
 */
val Player.itemCooldownContainer: ItemCooldownContainer
    get() = UserManager.get(this).itemCooldownContainer

/**
 * 玩家的属性容器.
 */
val Player.attributeContainer: AttributeMap
    get() = UserManager.get(this).attributeContainer

/**
 * 玩家的铭刻容器.
 */
val Player.kizamiContainer: KizamiMap
    get() = UserManager.get(this).inscriptionContainer

@get:Synchronized
@set:Synchronized
var Player.isInventoryListenable: Boolean
    get() = UserManager.get(this).initialized
    set(value) {
        UserManager.get(this).initialized = value
    }
