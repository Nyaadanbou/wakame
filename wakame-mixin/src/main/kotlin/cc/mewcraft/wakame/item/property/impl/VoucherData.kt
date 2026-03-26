package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.util.ServerFilter
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 兑换券的数据结构.
 *
 * @param actions 消耗后执行的动作列表
 * @param messages 消耗后向玩家发送的聊天提示
 * @param serverFilter 限制兑换券可在哪些服务器上使用
 */
@ConfigSerializable
data class VoucherData(
    val actions: List<VoucherAction> = emptyList(),
    val messages: Component = Component.text("兑换成功!"),
    val serverFilter: ServerFilter = ServerFilter.None,
)
