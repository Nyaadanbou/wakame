package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item.property.impl.HoldLastDamageAction.CANCEL
import cc.mewcraft.wakame.item.property.impl.HoldLastDamageAction.FINISH
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 通过配置文件对 [cc.mewcraft.wakame.item.behavior.impl.HoldLastDamage] 行为的逻辑进行精细设置.
 */
@ConfigSerializable
data class HoldLastDamage(
    val actions: Map<ItemBehaviorHandlerType, HoldLastDamageAction> = emptyMap(),
    val messages: Map<ItemBehaviorHandlerType, Component?> = emptyMap(),
) {
    fun getAction(type: ItemBehaviorHandlerType): HoldLastDamageAction {
        return actions[type] ?: type.defaultAction
    }

    fun getMessage(type: ItemBehaviorHandlerType): Component {
        return messages[type] ?: type.defaultMessage
    }
}

/**
 * 将 [cc.mewcraft.wakame.item.behavior.ItemBehavior] 接口下的各种以 handle 开头的函数称为 ItemBehaviorHandler.
 *
 * 该枚举类列举了 HoldLastDamage 行为中, 所有被考虑的 ItemBehaviorHandler 对应的类型.
 */
enum class ItemBehaviorHandlerType(
    val defaultAction: HoldLastDamageAction,
    val defaultMessage: Component,
) {
    SIMPLE_USE(CANCEL, TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_SIMPLE_USE.build()),

    SIMPLE_ATTACK(CANCEL, TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_SIMPLE_ATTACK.build()),

    CAUSE_DAMAGE(CANCEL, TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_CAUSE_DAMAGE.build()),

    // 通常不应该取消玩家受伤事件(取消了那玩家就无敌了), 因此默认值是FINISH.
    RECEIVE_DAMAGE(FINISH, TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_RECEIVE_DAMAGE.build()),

    CONSUME(CANCEL, TranslatableMessages.MSG_HOLD_LAST_DAMAGE_DEFAULT_WHEN_CONSUME.build())
}

/**
 * HoldLastDamage 行为对一种特定的 ItemBehaviorHandler 的处理行为逻辑.
 *
 * 请注意 [FINISH] 和 [CANCEL] 的区别. 例如, 一个具有“进行攻击交互时会产生攻击特效”行为的物品:
 * - 若标记 [ItemBehaviorHandlerType.SIMPLE_ATTACK] 为 [FINISH], 则该物品在“损坏”时仍然可以左键进行攻击交互, 但不会产生攻击特效.
 * - 若标记 [ItemBehaviorHandlerType.SIMPLE_ATTACK] 为 [CANCEL], 则该物品在“损坏”时产生的一切攻击交互事件都会被直接取消.
 */
enum class HoldLastDamageAction {
    /**
     * 什么都不做, 等效于没有 HoldLastDamage 行为.
     */
    NONE,

    /**
     * 仅中断后续同类 ItemBehaviorHandler 的执行而不取消相关事件.
     */
    FINISH,

    /**
     * 直接取消事件(当然后续同类 ItemBehaviorHandler 更不会执行了).
     */
    CANCEL
}

