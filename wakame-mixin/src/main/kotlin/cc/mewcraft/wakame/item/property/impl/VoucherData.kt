package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.item.token.TokenVerificationMode
import cc.mewcraft.wakame.util.ServerFilter
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 兑换券的数据结构.
 *
 * @param actions 消耗后执行的动作列表
 * @param message 消耗后向玩家发送的聊天提示
 * @param tokenMode 令牌校验模式. 默认为 [TokenVerificationMode.REQUIRED]
 * @param serverFilter 限制兑换券可在哪些服务器上使用
 */
@ConfigSerializable
data class VoucherData(
    val actions: List<VoucherAction> = emptyList(),
    val message: Component = Component.text("兑换成功!"),
    val tokenMode: TokenVerificationMode = TokenVerificationMode.SKIPPED,
    val serverFilter: ServerFilter = ServerFilter.None,
)
