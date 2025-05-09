package cc.mewcraft.wakame.item2.behavior.impl.weapon

import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.extensions.mul
import cc.mewcraft.wakame.extensions.plus
import cc.mewcraft.wakame.util.collision.OBB
import cc.mewcraft.wakame.util.collision.calculateOrthonormalBasis
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import xyz.xenondevs.commons.provider.orElse

object WeaponUtils {

    private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "attack_hitbox").orElse(false)

    /**
     * 获取基于 [player] 和 特定参数OBB 判定攻击到的生物.
     *
     * 该 OBB 基于玩家视角且位于玩家视角正前方, 与玩家碰撞箱中心点相切.
     * 基础半长宽高分别为 [halfDepthBase], [halfWidthBase], [halfHeightBase], 倾斜角度为 [angle].
     * 长宽高会根据玩家的尺寸 ([Attributes.SCALE]) 缩放.
     * [aabbRadius] 用来预筛选可能碰撞的生物, 减少计算量.
     */
    fun getHitEntities(player: Player, aabbRadius: Double, halfWidthBase: Float, halfHeightBase: Float, halfDepthBase: Float, angle: Float = 0f): List<LivingEntity> {
        val centerVector = player.boundingBox.center.toVector3f()
        val location = player.location
        val forwardVector = location.direction.toVector3f()

        val scale = player.attributeContainer.getValue(Attributes.SCALE).toFloat()
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

}