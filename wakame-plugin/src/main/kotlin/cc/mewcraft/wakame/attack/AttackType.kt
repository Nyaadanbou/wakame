package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.applyAttackCooldown
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.koin.core.component.get
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type


/**
 * 攻击类型.
 * 其实现类先不要写成单例.
 * 未来确认是无参数的实现再写成单例.
 */
sealed interface AttackType {
    /**
     * 玩家使用该攻击类型的物品直接左键攻击一个生物造成的伤害所使用的 [DamageMetadata].
     * 默认返回造成 1 点默认元素伤害的伤害元数据.
     * 返回空后续会使本次伤害事件取消.
     * !!!不要在该方法中的实现中写攻击的附带效果!!!
     */
    fun generateDamageMetadata(player: Player, nekoStack: NekoStack): DamageMetadata? {
        return PlayerDamageMetadata.HAND_WITHOUT_ATTACK
    }

    /**
     * 玩家使用该攻击类型的物品对直接生物造成伤害时执行的逻辑.
     */
    fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: LivingEntity, event: NekoEntityDamageEvent) = Unit

    /**
     * 玩家使用该攻击类型的物品进行交互事件时执行的逻辑.
     * 默认左键点击时触发攻击冷却
     */
    fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        if (!action.isLeftClick) return
        val user = player.toUser()
        if (!user.attackSpeed.isActive(nekoStack.id)) {
            // 没有左键到生物时, 也应该应用攻击冷却
            nekoStack.applyAttackCooldown(player)
        }
        wrappedEvent.actionPerformed = true
    }
}

/**
 * [AttackType] 的默认实现.
 * 实际表现为玩家徒手造成 1 默认元素伤害.
 */
data object HandAttack : AttackType

/**
 * [AttackType] 的序列化器.
 */
internal object AttackTypeSerializer : TypeSerializer<AttackType> {
    private val LOGGER: Logger = Injector.get()

    override fun deserialize(type: Type, node: ConfigurationNode): AttackType {
        return when (
            val attackType = node.node("type").getString("")
        ) {
            AxeAttack.NAME -> {
                AxeAttack()
            }

            BowAttack.NAME -> {
                BowAttack()
            }

            CrossbowAttack.NAME -> {
                CrossbowAttack()
            }

            CudgelAttack.NAME -> {
                CudgelAttack()
            }

            HammerAttack.NAME -> {
                HammerAttack()
            }

            SpearAttack.NAME -> {
                val size = node.node("size").getDouble(0.2)
                SpearAttack(size)
            }

            SwordAttack.NAME -> {
                SwordAttack()
            }

            TridentAttack.NAME -> {
                TridentAttack()
            }

            else -> {
                LOGGER.warn("Unknown attack type: '$attackType', using default attack type.")
                HandAttack
            }
        }
    }
}