@file:JvmName("CraftEngineUtils")
package cc.mewcraft.wakame.hook.impl.craftengine

import cc.mewcraft.wakame.item2.behavior.InteractionHand
import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.UseOnContext
import cc.mewcraft.wakame.util.Identifier
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors
import net.momirealms.craftengine.bukkit.util.DirectionUtils
import net.momirealms.craftengine.bukkit.util.LocationUtils
import net.momirealms.craftengine.core.world.BlockHitResult
import net.momirealms.craftengine.core.world.Vec3d
import org.bukkit.entity.Player

fun Identifier.toCraftEngine(): CEKey {
    return CEKey.of(namespace(), value())
}

fun Player.toCraftEngine(): CEPlayer {
    return BukkitAdaptors.adapt(player)
}

fun InteractionHand.toCraftEngine(): CEInteractionHand {
    return if (this == InteractionHand.MAIN_HAND) {
        CEInteractionHand.MAIN_HAND
    } else {
        CEInteractionHand.OFF_HAND
    }
}

fun UseOnContext.toCraftEngine(): CEUseOnContext {
    val cePlayer = player.toCraftEngine()
    val ceHand = hand.toCraftEngine()
    val direction = DirectionUtils.toDirection(interactFace)
    val pos = LocationUtils.toBlockPos(blockLocation)
    val vec3d = Vec3d(interactPoint.x, interactPoint.y, interactPoint.z)
    val hitResult = BlockHitResult(vec3d, direction, pos, false)
    return CEUseOnContext(cePlayer, ceHand, cePlayer.getItemInHand(ceHand), hitResult)
}

fun CEInteractionResult.toKoish(): InteractionResult {
    return when (this) {
        CEInteractionResult.PASS -> InteractionResult.PASS

        // 取消事件是为了不触发后续ce对交互的判定
        CEInteractionResult.FAIL -> InteractionResult.FAIL_AND_CANCEL
        CEInteractionResult.SUCCESS,
        CEInteractionResult.SUCCESS_AND_CANCEL -> InteractionResult.SUCCESS_AND_CANCEL

        // 在 Koish 的物品行为系统中不可能发生, 因为 Koish 物品行为触发时必定有物品
        CEInteractionResult.TRY_EMPTY_HAND -> InteractionResult.FAIL_AND_CANCEL
    }
}