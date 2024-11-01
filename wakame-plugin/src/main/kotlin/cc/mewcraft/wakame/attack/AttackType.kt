package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
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
     * 玩家使用该攻击类型的物品直接左键攻击一个生物产生攻击时执行的逻辑.
     * 返回的 [DamageMetadata] 影响该生物本次受到的伤害.
     * 返回空后续会使本次伤害事件取消.
     * 默认返回造成 1 点默认元素伤害的伤害元数据.
     */
    fun handleDirectMeleeAttackEntity(player: Player, nekoStack: NekoStack, event: EntityDamageEvent): DamageMetadata? {
        return PlayerDamageMetadata.HAND_WITHOUT_ATTACK
    }

    /**
     * 玩家使用该攻击类型的物品进行交互事件时执行的逻辑.
     */
    fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) = Unit
}

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

            BowShoot.NAME -> {
                BowShoot()
            }

            CrossbowShoot.NAME -> {
                CrossbowShoot()
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
                SwordAttack()
            }
        }
    }
}