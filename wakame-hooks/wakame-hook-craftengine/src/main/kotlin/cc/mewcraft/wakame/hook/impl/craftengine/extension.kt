@file:JvmName("CraftEngineUtils")

package cc.mewcraft.wakame.hook.impl.craftengine

import cc.mewcraft.wakame.item.behavior.InteractionHand
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseOnContext
import net.kyori.adventure.key.Key
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors
import net.momirealms.craftengine.bukkit.util.DirectionUtils
import net.momirealms.craftengine.bukkit.util.LocationUtils
import net.momirealms.craftengine.core.world.BlockHitResult
import net.momirealms.craftengine.core.world.Vec3d
import org.bukkit.entity.Player

fun Key.toCraftEngine(): CKey = CKey.of(namespace(), value())
fun CKey.toAdventure(): Key = Key.key(namespace(), value())
fun Player.toCraftEngine(): CPlayer = BukkitAdaptors.adapt(player)
fun CPlayer.toBukkit(): Player = platformPlayer()

fun InteractionHand.toCraftEngine(): CInteractionHand {
    return if (this == InteractionHand.MAIN_HAND) {
        CInteractionHand.MAIN_HAND
    } else {
        CInteractionHand.OFF_HAND
    }
}

fun UseOnContext.toCraftEngine(): CUseOnContext {
    val cplayer = player.toCraftEngine()
    val chand = hand.toCraftEngine()
    val direction = DirectionUtils.toDirection(interactFace)
    val pos = LocationUtils.toBlockPos(blockLocation)
    val vec3d = Vec3d(interactPoint.x, interactPoint.y, interactPoint.z)
    val hitResult = BlockHitResult(vec3d, direction, pos, false)
    return CUseOnContext(cplayer, chand, cplayer.getItemInHand(chand), hitResult)
}

fun CInteractionResult.toKoish(): InteractionResult {
    return when (this) {
        CInteractionResult.PASS -> InteractionResult.PASS

        // 取消事件是为了不触发后续 CE 对交互的判定
        CInteractionResult.FAIL -> InteractionResult.FAIL_AND_CANCEL
        CInteractionResult.SUCCESS, CInteractionResult.SUCCESS_AND_CANCEL -> InteractionResult.SUCCESS_AND_CANCEL

        // 在 Koish 的物品行为系统中不可能发生, 因为 Koish 物品行为触发时必定有物品
        CInteractionResult.TRY_EMPTY_HAND -> InteractionResult.FAIL_AND_CANCEL
    }
}