package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.dungeon.DungeonBridge
import cc.mewcraft.wakame.item.behavior.*
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.util.adventure.BukkitSound
import com.destroystokyo.paper.ParticleBuilder
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.Registry
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 目的:
 * - 我们想做一个有意思的进入地牢/副本的方式
 * - 该方式在有限的地图空间内可以被无限次使用, 以避免: 1) 玩家无限探索地图区块 2) 玩家争夺地牢入场方式
 *
 * 方案:
 * - 在地图上生成一些遗迹, 充当地牢的入口
 * - 制作一些物品, 充当这些地牢入口的钥匙
 * - 要进入地牢入口, 玩家需要在入口附近消耗掉对应的钥匙物品
 * - 消耗钥匙物品后, 传送玩家进入地牢内部
 */
object DungeonEntry : ItemBehavior {

    private fun playStartingEffects(player: Player, itemstack: ItemStack) {
        // 添加药水效果
        itemstack.getProp(ItemPropTypes.DUNGEON_ENTRY)
            ?.useEffects
            ?.forEach(player::addPotionEffect)
        // 播放例子效果 (粒子效果暂不支持配置文件, 懒得写序列化)
        ParticleBuilder(Particle.PORTAL)
            .location(player.location)
            .offset(-2.0, 0.5, 2.0)
            .count(256)
            .receivers(32, false)
            .spawn()
        // 发送消息提示
        player.sendActionBar(TranslatableMessages.MSG_CHANNELING_STARTED)
    }

    private fun playFinishingEffects(player: Player, itemstack: ItemStack) {
        // 播放声音效果
        player.playSound(Sound.sound().type(BukkitSound.BLOCK_PORTAL_TRIGGER).source(Sound.Source.PLAYER).build(), player)
        // 发送消息提示
        player.sendActionBar(TranslatableMessages.MSG_CHANNELING_STOPPED)
    }

    override fun handleUse(context: UseContext): InteractionResult {
        playStartingEffects(context.player, context.itemstack)
        return InteractionResult.PASS
    }

    override fun handleUseOn(context: UseOnContext): InteractionResult {
        if (context.triggersBlockInteract) return InteractionResult.PASS
        playStartingEffects(context.player, context.itemstack)
        return InteractionResult.PASS
    }

    override fun handleStopUse(context: StopUseContext): BehaviorResult {
        playFinishingEffects(context.player, context.itemstack)
        return BehaviorResult.PASS
    }

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val player = context.player
        val itemstack = context.itemstack
        val dungeonEntry = itemstack.getProp(ItemPropTypes.DUNGEON_ENTRY) ?: return BehaviorResult.PASS
        val dungeonId = dungeonEntry.dungeon
        val structureId = dungeonEntry.structure
        val partyRadius = dungeonEntry.partyRadius
        if (DungeonBridge.inited().not()) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NOT_IN_DUNGEON_WORLD)
            return BehaviorResult.FINISH_AND_CANCEL
        }
        if (!DungeonBridge.hasDungeon(dungeonId).getOrDefault(false)) {
            player.sendMessage(TranslatableMessages.MSG_ERR_DUNGEON_NOT_FOUND.arguments(Component.text(dungeonId)))
            return BehaviorResult.FINISH_AND_CANCEL
        }
        if (DungeonBridge.isAwaitingDungeon(player).getOrDefault(true)) {
            player.sendMessage(TranslatableMessages.MSG_ERR_ALREADY_AWAITING_DUNGEON)
            return BehaviorResult.FINISH_AND_CANCEL
        }
        val placedStructure = player.chunk.structures.firstOrNull {
            val structure = it.structure
            val structureType = structure.structureType
            val structureId2 = Registry.STRUCTURE_TYPE.getKey(structureType)
            structureId2 == structureId
        }
        if (placedStructure == null) {
            player.sendMessage(TranslatableMessages.MSG_ERR_STRUCTURE_NOT_FOUND_IN_CHUNK.arguments(Component.text(structureId.asString())))
            return BehaviorResult.FINISH_AND_CANCEL
        }
        val location = player.location
        val insideStructure = placedStructure.boundingBox.contains(location.toVector()) ||
                placedStructure.pieces.any { piece -> piece.boundingBox.contains(location.toVector()) }
        if (!insideStructure) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NOT_INSIDE_STRUCTURE.arguments(Component.text(structureId.asString())))
            return BehaviorResult.FINISH_AND_CANCEL
        }
        val nearbySneakingPlayers = location.getNearbyPlayers(partyRadius) { p ->
            p != player && p.isSneaking
        }
        if (nearbySneakingPlayers.isEmpty()) {
            DungeonBridge.play(player, dungeonId).onFailure { error ->
                LOGGER.error("An error occurred while sending player ${player.name} to dungeon '$dungeonId'", error)
                return BehaviorResult.FINISH_AND_CANCEL
            }
        } else {
            val players = buildList {
                add(player)
                addAll(nearbySneakingPlayers)
            }
            DungeonBridge.play(players, dungeonId).onFailure { error ->
                LOGGER.error("An error occurred while sending players ${players.joinToString { it.name }} to dungeon '$dungeonId'", error)
                return BehaviorResult.FINISH_AND_CANCEL
            }
        }
        return BehaviorResult.FINISH
    }
}