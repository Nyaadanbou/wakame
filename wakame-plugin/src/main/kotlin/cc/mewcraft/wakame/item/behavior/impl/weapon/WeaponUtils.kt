package cc.mewcraft.wakame.item.behavior.impl.weapon

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.optionalEntry
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.hurt
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.entity.player.attributeContainer
import cc.mewcraft.wakame.util.collision.OBB
import cc.mewcraft.wakame.util.math.copy
import cc.mewcraft.wakame.util.math.maxComponentValue
import cc.mewcraft.wakame.util.math.rotateAroundAxis
import cc.mewcraft.wakame.util.math.toRadians
import org.bukkit.damage.DamageSource
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.joml.Vector3f
import xyz.xenondevs.commons.provider.orElse
import kotlin.math.cos
import kotlin.math.sin

object WeaponUtils {

    private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "attack_hitbox").orElse(false)

    /**
     * 获取基于 [player] 和 特定参数OBB 判定攻击到的生物.
     *
     * 该 OBB 基于玩家视角且位于玩家视角正前方, 与玩家碰撞箱中心点相切.
     * 基础半深度/宽度/高度分别对应 [halfExtentsBase] 的x/y/z分量, 倾斜角度为 [angle].
     * 长宽高会根据玩家的尺寸 ([Attributes.SCALE]) 缩放.
     * [aabbRadius] 用来预筛选可能碰撞的生物, 减少计算量.
     */
    fun getHitEntities(player: Player, aabbRadius: Double, halfExtentsBase: Vector3f, angle: Float = 0f): List<LivingEntity> {
        val centerVector = player.boundingBox.center.toVector3f()
        val location = player.location
        val forwardVector = location.direction.toVector3f()

        val scale = player.attributeContainer.getValue(Attributes.SCALE).toFloat()
        val attackOBB = OBB(
            (forwardVector.mul(halfExtentsBase.x * scale)).add(centerVector),
            calculateOrthonormalBasis(player, angle),
            halfExtentsBase.copy().mul(scale)
        )
        if (LOGGING) {
            if (player.hasPermission("koish.logging.attack_hitbox")) {
                player.sendMessage("OBB 中心: ${attackOBB.center}")
                attackOBB.drawWireframe(player)
            }
        }

        return location.getNearbyLivingEntities(aabbRadius * scale) { entity ->
            entity != player
        }.filter { entity ->
            attackOBB.isCollideFast(entity.boundingBox)
        }
    }

    fun getHitEntities(player: Player, halfExtentsBase: Vector3f, angle: Float = 0f): List<LivingEntity> {
        val aabbRadius = halfExtentsBase.maxComponentValue() + 1
        return getHitEntities(player, aabbRadius.toDouble(), halfExtentsBase, angle)
    }

    /**
     * 对 [hitEntities] 造成伤害.
     * 返回是否成功对任一生物造成了伤害.
     */
    fun hurtEntities(hitEntities: List<LivingEntity>, metadata: DamageMetadata, source: DamageSource, knockback: Boolean): Boolean {
        var flag = false
        hitEntities.forEach { entity ->
            if (entity.hurt(metadata, source, knockback)) {
                flag = true
            }
        }
        return flag
    }

    /**
     * 基于实体视角计算标准正交基.
     * 并在视角平面 (与视线向量垂直的平面) 内旋转 [angle] 角度.
     * 单位使用角度制.
     * 正值为顺时针旋转.
     */
    fun calculateOrthonormalBasis(yaw: Float, pitch: Float, angle: Float): Triple<Vector3f, Vector3f, Vector3f> {
        val yawRad = yaw.toRadians()
        val pitchRad = pitch.toRadians()

        val sinYaw = sin(yawRad)
        val cosYaw = cos(yawRad)
        val sinPitch = sin(pitchRad)
        val cosPitch = cos(pitchRad)

        val forward = Vector3f(-cosPitch * sinYaw, -sinPitch, cosPitch * cosYaw)
        val right = Vector3f(-cosYaw, 0f, -sinYaw).rotateAroundAxis(forward, angle.toRadians())
        val up = right.copy().cross(forward)
        return Triple(right, up, forward)
    }

    /**
     * 基于实体视角计算标准正交基.
     * 并在视角平面 (与视线向量垂直的平面) 内旋转 [angle] 角度.
     * 单位使用角度制.
     * 正值为顺时针旋转.
     */
    fun calculateOrthonormalBasis(entity: Entity, angle: Float = 0f): Triple<Vector3f, Vector3f, Vector3f> {
        return calculateOrthonormalBasis(entity.yaw, entity.pitch, angle)
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
        return if (result.lengthSquared() < 1e-6) null else result.normalize()
    }
}

