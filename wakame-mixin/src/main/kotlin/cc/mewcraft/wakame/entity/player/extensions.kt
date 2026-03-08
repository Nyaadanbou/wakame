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
val Player.powerLevel: Int
    get() = KoishUserManager.get(this).powerLevel

/**
 * 玩家的物品冷却.
 */
val Player.itemCooldownContainer: ItemCooldownContainer
    get() = KoishUserManager.get(this).itemCooldownContainer

/**
 * 玩家的属性容器.
 */
val Player.attributeContainer: AttributeMap
    get() = KoishUserManager.get(this).attributeContainer

/**
 * 玩家的铭刻容器.
 */
val Player.inscriptionContainer: KizamiMap
    get() = KoishUserManager.get(this).inscriptionContainer

/**
 * 玩家的数据是否已经跨服同步完成.
 */
var Player.isDataSynced: Boolean
    get() = KoishUserManager.get(this).isSynced
    set(value) {
        KoishUserManager.get(this).isSynced = value
    }
