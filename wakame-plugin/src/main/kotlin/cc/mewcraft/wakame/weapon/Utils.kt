package cc.mewcraft.wakame.weapon

import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.extensions.mul
import cc.mewcraft.wakame.extensions.plus
import cc.mewcraft.wakame.util.collision.OBB
import cc.mewcraft.wakame.util.collision.calculateOrthonormalBasis
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

private val LOGGING by MAIN_CONFIG.entry<Boolean>("debug", "attack_hitbox")

internal val Player.boundingBoxScale: Float get() = attributeContainer.getValue(Attributes.SCALE).toFloat()

/**
 * 获取基于 [player] 和 特定参数OBB 判定攻击到的生物.
 *
 * 该 OBB 基于玩家视角且位于玩家视角正前方, 与玩家碰撞箱中心点相切.
 * 基础半长宽高分别为 [halfDepthBase], [halfWidthBase], [halfHeightBase], 倾斜角度为 [angle].
 * 长宽高会根据玩家尺寸缩放.
 * [aabbRadius] 用来预筛选可能碰撞的生物, 减少计算量.
 */
internal fun getHitEntities(player: Player, aabbRadius: Double, halfWidthBase: Float, halfHeightBase: Float, halfDepthBase: Float, angle: Float = 0f): List<LivingEntity> {
    val centerVector = player.boundingBox.center.toVector3f()
    val location = player.location
    val forwardVector = location.direction.toVector3f()

    val scale = player.boundingBoxScale
    val halfWidth = halfWidthBase * scale
    val halfHeight = halfHeightBase * scale
    val halfDepth = halfDepthBase * scale
    val attackOBB = OBB(
        (forwardVector mul halfDepth) + centerVector,
        calculateOrthonormalBasis(player, angle),
        floatArrayOf(halfWidth, halfHeight, halfDepth)
    )
    if (LOGGING) {
        player.sendMessage("OBB 中心: ${attackOBB.center}")
        attackOBB.drawWireframe(player)
    }

    return location.getNearbyLivingEntities(aabbRadius) { entity ->
        entity != player
    }.filter { entity ->
        attackOBB.isCollide(entity)
    }
}