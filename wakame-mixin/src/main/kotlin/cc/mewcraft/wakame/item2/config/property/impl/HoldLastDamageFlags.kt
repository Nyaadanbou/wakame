package cc.mewcraft.wakame.item2.config.property.impl

import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
/**
 * 通过配置文件对 [cc.mewcraft.wakame.item2.behavior.impl.HoldLastDamage] 行为的逻辑进行精细控制.
 * 请注意 finish 和 cancel 的区别.
 * finish 是仅中断后续同类行为的执行而不取消事件, cancel 是直接取消事件(当然后续同类行为更不会执行了).
 * 例如, 一个具有“进行攻击交互时会产生攻击特效”这一行为的物品.
 * 若标记 [finishAttack] 为 true, 则该物品在“损坏”时仍然可以左键进行攻击交互, 但不会产生攻击特效.
 * 若标记 [cancelAttack] 为 true, 则该物品在“损坏”时产生的一切攻击交互事件都会被直接取消.
 */
data class HoldLastDamageFlags(
    /**
     * 物品处于“损坏”状态时, 是否中断后续的“使用交互”行为.
     * 包括对空气、对方块、对实体的使用交互.
     */
    val finishUse: Boolean = true,

    /**
     * 物品处于“损坏”状态时, 是否中断后续的“攻击交互”行为.
     * 包括对空气、对方块、对实体的攻击交互.
     */
    val finishAttack: Boolean = true,

    /**
     * 物品处于“损坏”状态时, 是否中断后续的“玩家造成伤害”行为.
     * 物品造成伤害可能未必是由使用/攻击交互触发的, 因此单独列出.
     */
    val finishCauseDamage: Boolean = true,

    /**
     * 物品处于“损坏”状态时, 是否中断后续的“玩家受到伤害”行为.
     * 请注意“玩家受到伤害”的事件一般来说是不应该被取消的(取消了那玩家就无敌了).
     */
    val finishReceiveDamage: Boolean = true,

    /**
     * 物品处于“损坏”状态时, 是否中断后续的“消耗”行为.
     */
    val finishConsume: Boolean = true,


    /**
     * 物品处于“损坏”状态时, 是否取消“使用交互”的对应事件.
     */
    val cancelUse: Boolean = true,

    /**
     * 物品处于“损坏”状态时, 是否取消“攻击交互”的对应事件.
     */
    val cancelAttack: Boolean = true,

    /**
     * 物品处于“损坏”状态时, 是否取消“玩家造成伤害”的对应事件.
     */
    val cancelCauseDamage: Boolean = true,

    /**
     * 物品处于“损坏”状态时, 是否取消“消耗”的对应事件.
     */
    val cancelConsume: Boolean = true,


    /**
     * 物品处于“损坏”状态, 且触发“使用交互”时, 玩家收到的动作栏信息.
     * 留空则使用默认信息.
     */
    val msgUse: Component? = null,

    /**
     * 物品处于“损坏”状态, 且触发“攻击交互”时, 玩家收到的动作栏信息.
     * 留空则使用默认信息.
     */
    val msgAttack: Component? = null,

    /**
     * 物品处于“损坏”状态, 且触发“玩家造成伤害”时, 玩家收到的动作栏信息.
     * 留空则使用默认信息.
     */
    val msgCauseDamage: Component? = null,

    /**
     * 物品处于“损坏”状态, 且触发“玩家受到伤害”时, 玩家收到的动作栏信息.
     * 留空则使用默认信息.
     */
    val msgReceiveDamage: Component? = null,

    /**
     * 物品处于“损坏”状态, 且触发“消耗”时, 玩家收到的动作栏信息.
     * 留空则使用默认信息.
     */
    val msgConsume: Component? = null,
)