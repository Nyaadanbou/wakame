package cc.mewcraft.wakame.weapon

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.PlayerDamageMetadata
import cc.mewcraft.wakame.event.bukkit.NekoPostprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.require
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import org.bukkit.damage.DamageSource
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerItemDamageEvent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import java.lang.reflect.Type
import kotlin.reflect.KClass


/**
 * 武器类型.
 */
sealed interface WeaponType {
    /**
     * 玩家使用该武器类型物品直接左键攻击一个生物造成的伤害所使用的 [DamageMetadata].
     * 默认返回造成 1 点默认元素伤害的伤害元数据.
     * 返回 `null` 后续会使本次伤害事件取消.
     * 请注意, 被取消的伤害事件仍然会触发 [PlayerClickEvent]
     *
     * !!! 不要在该方法中的实现中写攻击的附带效果 !!!
     */
    fun generateDamageMetadata(player: Player, nekoStack: NekoStack): DamageMetadata? {
        return PlayerDamageMetadata.INTRINSIC_ATTACK
    }

    /**
     * 玩家将该武器类型物品切出有效槽位时触发的逻辑.
     */
    fun handleSlotChangePreviousItem(player: Player, nekoStack: NekoStack, slot: ItemSlot) = Unit

    /**
     * 玩家将该武器类型物品切入有效槽位时触发的逻辑.
     */
    fun handleSlotChangeCurrentItem(player: Player, nekoStack: NekoStack, slot: ItemSlot) = Unit

    /**
     * 玩家 "激活" 该武器类型物品时, 每tick执行的逻辑.
     * "激活": 正在使用或消耗.
     */
    fun handleActiveTick(player: Player, nekoStack: NekoStack, event: ServerTickStartEvent) = Unit

    /**
     * 玩家使用该武器类型物品点击时执行的逻辑.
     *
     * !!! 注意该事件与以下两个事件之间的联系 !!!
     */
    fun handleClick(player: Player, nekoStack: NekoStack, clickAction: PlayerClickEvent.Action, clickHand: PlayerClickEvent.Hand, event: PlayerClickEvent) = Unit

    /**
     * 玩家使用该武器类型物品对直接生物造成伤害时执行的逻辑.
     */
    fun handleAttackEntity(player: Player, nekoStack: NekoStack, damagee: LivingEntity, event: NekoPostprocessDamageEvent) = Unit

    /**
     * 玩家使用该武器类型物品受到伤害时执行的逻辑.
     */
    fun handlePlayerDamage(player: Player, nekoStack: NekoStack, damageSource: DamageSource, event: NekoPostprocessDamageEvent) = Unit

    /**
     * 玩家使用该武器类型物品进行交互事件时执行的逻辑.
     */
    fun handleInteract(player: Player, nekoStack: NekoStack, action: Action, wrappedEvent: WrappedPlayerInteractEvent) = Unit

    /**
     * 玩家停止使用该武器类型物品时执行的逻辑.
     */
    fun handleRelease(player: Player, nekoStack: NekoStack, event: PlayerStopUsingItemEvent) = Unit

    /**
     * 玩家使用该武器类型物品触发原版掉耐久事件时执行的逻辑.
     */
    @Deprecated("Use weapon component instead after upgrading to 1.20.5")
    fun handleDamage(player: Player, nekoStack: NekoStack, event: PlayerItemDamageEvent) = Unit
}

/**
 * [WeaponType] 的默认实现.
 *
 * 实际表现为玩家徒手造成 1 默认元素伤害.
 */
data object EmptyHand : WeaponType

/**
 * [WeaponType] 的序列化器.
 */
internal object WeaponTypeSerializer : TypeSerializer2<WeaponType> {
    private val weaponTypeMap: Map<String, KClass<out WeaponType>> = mapOf(
        "katana" to KatanaWeapon::class
    )

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): WeaponType {
        return EmptyHand // 默认的武器类型, 即空手
    }

    override fun deserialize(type: Type, node: ConfigurationNode): WeaponType {
        val typeStr = node.node("type").getString("hand")
        val clazz = weaponTypeMap[typeStr]
        if (clazz == null) {
            LOGGER.warn("Unknown attack type: '$typeStr', using default instead.")
            return EmptyHand
        } else {
            return node.require(clazz)
        }
    }
}
