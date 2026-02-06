package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.teleport.NetworkTeleport
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.behavior.*
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.data.impl.NetworkPosition
import cc.mewcraft.wakame.item.property.ItemPropTypes
import io.papermc.paper.datacomponent.DataComponentTypes

/**
 * 传送锚. 基于 [HuskHomes](https://william278.net/docs/huskhomes/api-examples) 实现.
 *
 * ### 行为描述
 *
 * - 一开始这个物品是一张“白纸”, 没有记录任何 [NetworkPosition].
 * - 对着空气右键, 会将玩家当前的 [NetworkPosition] 记录在物品上.
 * - 一旦物品有了记录, 就变成了消耗品 (`minecraft:consumable`).
 * - 此时如果消耗掉, 就会将玩家传送到物品上记录的 [NetworkPosition].
 */
object TeleportAnchor : ItemBehavior {

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val player = context.player
        if (!context.itemstack.hasProp(ItemPropTypes.TELEPORT_ANCHOR)) {
            return BehaviorResult.PASS
        }
        val pos = context.itemstack.getData(ItemDataTypes.NETWORK_POSITION) ?: return BehaviorResult.FINISH_AND_CANCEL
        val res = NetworkTeleport.execute(player, pos.server, pos.world, pos.x, pos.y, pos.z, pos.yaw, pos.pitch)
        return if (res.isSuccess) {
            BehaviorResult.FINISH
        } else {
            BehaviorResult.FINISH_AND_CANCEL
        }
    }

    override fun handleUse(context: UseContext): InteractionResult {
        val player = context.player
        val itemstack = context.itemstack
        val teleportAnchor = itemstack.getProp(ItemPropTypes.TELEPORT_ANCHOR) ?: return InteractionResult.PASS
        if (itemstack.hasData(ItemDataTypes.NETWORK_POSITION)) {
            return InteractionResult.PASS // 已经绑定过坐标 - 不重复绑定
        }
        if (player.isSneaking.not()) return InteractionResult.PASS // 为防止误触, 只有在潜行时才可绑定坐标
        val server = NetworkTeleport.server().getOrNull() ?: return InteractionResult.FAIL_AND_CANCEL
        val loc = player.location
        val world = loc.world.name
        if (server !in teleportAnchor.allowedServers) {
            player.sendMessage(TranslatableMessages.MSG_ERR_CANNOT_SAVE_NETWORK_POS_IN_CURRENT_SERVER)
            return InteractionResult.FAIL_AND_CANCEL
        }
        if (world !in teleportAnchor.allowedDimensions) {
            player.sendMessage(TranslatableMessages.MSG_ERR_CANNOT_SAVE_NETWORK_POS_IN_CURRENT_DIMENSION)
            return InteractionResult.FAIL_AND_CANCEL
        }
        itemstack.setData(
            ItemDataTypes.NETWORK_POSITION,
            NetworkPosition(
                x = loc.x,
                y = loc.y,
                z = loc.z,
                yaw = loc.yaw,
                pitch = loc.pitch,
                world = world,
                server = server,
            )
        )
        // 一旦绑定了坐标, 则变为可消耗品
        itemstack.setData(DataComponentTypes.CONSUMABLE, teleportAnchor.buildConsumable())
        player.sendActionBar(TranslatableMessages.MSG_NETWORK_POS_SAVED)
        return InteractionResult.SUCCESS_AND_CANCEL
    }
}