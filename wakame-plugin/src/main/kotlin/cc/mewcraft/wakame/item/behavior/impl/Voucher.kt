package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.messaging2.ServerInfoProvider
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item.behavior.BehaviorResult
import cc.mewcraft.wakame.item.behavior.ConsumeContext
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.getData
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.token.SingleUseTokenRepository
import cc.mewcraft.wakame.item.token.TokenVerificationMode
import net.kyori.adventure.text.Component

/**
 * 兑换券行为.
 *
 * 玩家消耗兑换券时, 根据 [cc.mewcraft.wakame.item.token.TokenVerificationMode] 决定是否校验一次性令牌:
 * - [cc.mewcraft.wakame.item.token.TokenVerificationMode.REQUIRED]: 校验令牌后再执行动作.
 *   - 如果当前服务器不在允许列表中, 拒绝使用
 *   - 如果令牌不存在, 视为无效兑换券
 *   - 如果令牌已被使用过 (数据库中已存在), 视为重复兑换
 *   - 如果令牌未被使用过, 原子性地标记为已使用, 然后执行配置的动作列表
 * - [cc.mewcraft.wakame.item.token.TokenVerificationMode.SKIPPED]: 跳过令牌校验, 直接执行动作.
 *   适用于低价值、无需防复制保护的兑换券.
 *
 * 核心安全保证 (仅 REQUIRED 模式): 使用数据库的 `INSERT ... ON CONFLICT DO NOTHING` 原子操作,
 * 确保即使物品被复制, 同一个令牌也只能被成功兑换一次.
 */
object Voucher : ItemBehavior {

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val player = context.player
        val itemstack = context.itemstack

        // 获取兑换券的动作配置
        val voucherData = itemstack.getProp(ItemPropTypes.VOUCHER) ?: return BehaviorResult.PASS

        // 校验当前服务器是否允许使用此兑换券
        if (!voucherData.serverFilter.allows(ServerInfoProvider.serverKey)) {
            player.sendMessage(TranslatableMessages.MSG_VOUCHER_SERVER_NOT_ALLOWED.build())
            return BehaviorResult.FINISH_AND_CANCEL
        }

        when (voucherData.tokenMode) {
            TokenVerificationMode.REQUIRED -> {
                // 获取物品上的一次性令牌
                val token = itemstack.getData(ItemDataTypes.SINGLE_USE_TOKEN)
                if (token == null) {
                    player.sendMessage(TranslatableMessages.MSG_VOUCHER_NO_CODE.build())
                    return BehaviorResult.FINISH_AND_CANCEL
                }

                // 原子性地尝试标记为已使用
                val redeemed: Boolean = try {
                    SingleUseTokenRepository.markRedeemed(token, player.uniqueId)
                } catch (ex: Exception) {
                    LOGGER.error("Failed to mark single-use token as redeemed for player ${player.name} (token=$token)", ex)
                    player.sendMessage(TranslatableMessages.MSG_ERR_INTERNAL_ERROR.build())
                    return BehaviorResult.FINISH_AND_CANCEL
                }

                if (!redeemed) {
                    // 该令牌已被使用过 — 可能是复制的物品
                    player.sendMessage(TranslatableMessages.MSG_VOUCHER_ALREADY_REDEEMED.build())
                    LOGGER.error("Found an already-used single-use token. This indicates a serious bug!")
                    return BehaviorResult.FINISH_AND_CANCEL
                }

                // 兑换成功, 执行所有动作
                for (action in voucherData.actions) {
                    try {
                        action.execute(player)
                    } catch (ex: Exception) {
                        LOGGER.error("Failed to execute voucher action for player ${player.name} (token=$token)", ex)
                    }
                }
            }

            TokenVerificationMode.SKIPPED -> {
                LOGGER.info("Voucher consumed by player {} with token verification skipped (item={})", player.name, itemstack.type)

                // 跳过令牌校验, 直接执行所有动作
                for (action in voucherData.actions) {
                    try {
                        action.execute(player)
                    } catch (ex: Exception) {
                        LOGGER.error("Failed to execute voucher action for player ${player.name} (no token)", ex)
                    }
                }
            }
        }

        // 向玩家发送自定义提示
        val prompt = voucherData.message
        if (prompt != Component.empty()) {
            player.sendMessage(prompt)
        }

        return BehaviorResult.FINISH
    }
}
