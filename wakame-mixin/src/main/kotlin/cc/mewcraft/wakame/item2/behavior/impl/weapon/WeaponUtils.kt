package cc.mewcraft.wakame.item2.behavior.impl.weapon

import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.extensions.mul
import cc.mewcraft.wakame.extensions.plus
import cc.mewcraft.wakame.util.collision.OBB
import cc.mewcraft.wakame.util.collision.calculateOrthonormalBasis
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.joml.Vector3f
import xyz.xenondevs.commons.provider.orElse
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object WeaponUtils {

    private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "attack_hitbox").orElse(false)

    fun buildPlayerAttackDamageSource(player: Player): DamageSource {
        return DamageSource.builder(DamageType.PLAYER_ATTACK).withCausingEntity(player).withDirectEntity(player).build()
    }

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

        return location.getNearbyLivingEntities(aabbRadius * scale) { entity ->
            entity != player
        }.filter { entity ->
            attackOBB.isCollide(entity)
        }
    }

    fun getHitEntities(player: Player, aabbRadius: Double, halfExtentsBase: Vector3f, angle: Float = 0f): List<LivingEntity> {
        return getHitEntities(player, aabbRadius, halfExtentsBase.x, halfExtentsBase.y, halfExtentsBase.z, angle)
    }

    fun Player.getInputDirection(): Vector3f? {
        val currentInput = this.currentInput
        val yawRad = this.yaw.toRadians()
        val forward = Vector3f(-sin(yawRad), 0f, cos(yawRad))
        val right = Vector3f(-cos(yawRad), 0f, -sin(yawRad))

        // 累加方向
        var result = Vector3f(0f, 0f, 0f)
        if (currentInput.isForward && !currentInput.isBackward) result = result.add(forward)
        if (currentInput.isBackward && !currentInput.isForward) result = result.sub(forward)
        if (currentInput.isRight && !currentInput.isLeft) result = result.add(right)
        if (currentInput.isLeft && !currentInput.isRight) result = result.sub(right)

        // 如果输入相互抵消, 即累加结果为零向量, 则返回 null
        return if (result.lengthSquared() < 1e-5) null else result.normalize()
    }

    // TODO 我觉得真应该搞个 MathUtils
    //  用于单精度浮点数的角度弧度互相转换、提供不可变的0向量和XYZ单位向量等
    //  还有 [cc.mewcraft.wakame.util.collision.isOrthonormalBasis(...)] 等方法
    /**
     * 单精度浮点型的圆周率.
     */
    private const val PI_FLOAT: Float = PI.toFloat()

    /**
     * 把角度制转为弧度制.
     */
    private fun Float.toRadians(): Float = this * (PI_FLOAT / 180f)

    /**
     * 把弧度制转为角度制.
     */
    private fun Float.toDegrees(): Float = this * (180f / PI_FLOAT)
}

