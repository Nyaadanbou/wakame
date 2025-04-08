package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.entity.player.itemCooldownContainer
import cc.mewcraft.wakame.event.bukkit.NekoPostprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.extension.applyAttackCooldown
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerItemDamageEvent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import java.lang.reflect.Type

/**
 * 攻击类型.
 */
// 其实现类先不要写成单例.
// 未来确认是无参数的实现再写成单例.
@Deprecated("Use WeaponType instead")
sealed interface AttackType {
    /**
     * 玩家使用该攻击类型的物品直接左键攻击一个生物造成的伤害所使用的 [DamageMetadata].
     * 默认返回造成 1 点默认元素伤害的伤害元数据.
     * 返回 `null` 后续会使本次伤害事件取消.
     *
     * !!! 不要在该方法中的实现中写攻击的附带效果 !!!
     */
    fun generateDamageMetadata(player: Player, nekoStack: NekoStack): DamageMetadata? {
        return PlayerDamageMetadata.INTRINSIC_ATTACK
    }

    /**
     * 玩家使用该攻击类型的物品对直接生物造成伤害时执行的逻辑.
     */
    fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: LivingEntity, event: NekoPostprocessDamageEvent) = Unit

    /**
     * 玩家使用该攻击类型的物品进行交互事件时执行的逻辑.
     * 默认左键点击时触发攻击冷却.
     */
    fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) {
        if (!action.isLeftClick) return
        if (!player.itemCooldownContainer.isActive(nekoStack.id)) {
            // 没有左键到生物时, 也应该应用攻击冷却
            nekoStack.applyAttackCooldown(player)
        }
        wrappedEvent.actionPerformed = true
    }

    /**
     * 玩家使用该攻击类型的物品触发原版掉耐久事件时执行的逻辑.
     */
    fun handleDamage(player: Player, nekoStack: NekoStack, event: PlayerItemDamageEvent) = Unit
}

/**
 * [AttackType] 的默认实现.
 *
 * 实际表现为玩家徒手造成 1 默认元素伤害.
 */
data object HandAttack : AttackType

/**
 * [AttackType] 的序列化器.
 */
internal object AttackTypeSerializer : TypeSerializer2<AttackType> {
    override fun emptyValue(specificType: Type, options: ConfigurationOptions): AttackType {
        return HandAttack // 默认的攻击类型
    }

    override fun deserialize(type: Type, node: ConfigurationNode): AttackType {
        return when (
            val attackType = node.node("type").getString("")
        ) {
            AxeAttack.NAME -> {
                val cancelVanillaDamage = node.getCancelVanillaDamage()
                AxeAttack(cancelVanillaDamage)
            }

            BowAttack.NAME -> {
                BowAttack()
            }

            CrossbowAttack.NAME -> {
                CrossbowAttack()
            }

            CudgelAttack.NAME -> {
                val cancelVanillaDamage = node.getCancelVanillaDamage()
                CudgelAttack(cancelVanillaDamage)
            }

            HammerAttack.NAME -> {
                val cancelVanillaDamage = node.getCancelVanillaDamage()
                HammerAttack(cancelVanillaDamage)
            }

            SpearAttack.NAME -> {
                val size = node.node("size").getDouble(0.2)
                val cancelVanillaDamage = node.getCancelVanillaDamage()
                SpearAttack(cancelVanillaDamage, size)
            }

            SwordAttack.NAME -> {
                val cancelVanillaDamage = node.getCancelVanillaDamage()
                SwordAttack(cancelVanillaDamage)
            }

            TridentAttack.NAME -> {
                val cancelVanillaDamage = node.getCancelVanillaDamage()
                TridentAttack(cancelVanillaDamage)
            }

            else -> {
                LOGGER.warn("Unknown attack type: '$attackType', using default instead.")
                HandAttack
            }
        }
    }

    private fun ConfigurationNode.getCancelVanillaDamage(): Boolean {
        return node("cancel_vanilla_damage").getBoolean(false)
    }
}