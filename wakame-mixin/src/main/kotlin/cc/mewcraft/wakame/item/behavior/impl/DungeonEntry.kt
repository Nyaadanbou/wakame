package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.dungeon.DungeonBridge
import cc.mewcraft.wakame.item.behavior.*
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.DungeonEntry
import cc.mewcraft.wakame.util.adventure.BukkitSound
import cc.mewcraft.wakame.util.cooldown.Cooldown
import cc.mewcraft.wakame.util.metadata.ExpiringValue
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataCooldownKey
import cc.mewcraft.wakame.util.metadata.metadataKey
import com.destroystokyo.paper.ParticleBuilder
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.EntityEffect
import org.bukkit.Particle
import org.bukkit.Registry
import org.bukkit.craftbukkit.util.WeakCollection
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrElse

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

    const val ENTRY_COOLDOWN_SECONDS = 1L
    const val PARTY_MEMBER_LIFETIME_SECONDS = 10L // 注意 minecraft:consumable 的 consume_seconds 不能大于这个
    const val METADATA_NAMESPACE = "dungeon_entry"

    private val ENTRY_COOLDOWN_KEY = metadataCooldownKey("$METADATA_NAMESPACE:use_cooldown")
    private val PARTY_MEMBERS_KEY = metadataKey<Collection<Player>>("$METADATA_NAMESPACE:party_members")

    override fun handleUse(context: UseContext): InteractionResult {
        val dungeonEntry = context.itemstack.getProp(ItemPropTypes.DUNGEON_ENTRY) ?: return InteractionResult.FAIL_AND_CANCEL
        if (!handleStartUsing(context.player, dungeonEntry)) return InteractionResult.FAIL_AND_CANCEL
        return InteractionResult.PASS
    }

    override fun handleUseOn(context: UseOnContext): InteractionResult {
        if (context.triggersBlockInteract) return InteractionResult.PASS
        val dungeonEntry = context.itemstack.getProp(ItemPropTypes.DUNGEON_ENTRY) ?: return InteractionResult.FAIL_AND_CANCEL
        if (!handleStartUsing(context.player, dungeonEntry)) return InteractionResult.FAIL_AND_CANCEL
        return InteractionResult.PASS
    }

    override fun handleUseEntity(context: UseEntityContext): InteractionResult {
        if (context.triggersEntityInteract) return InteractionResult.PASS
        val dungeonEntry = context.itemstack.getProp(ItemPropTypes.DUNGEON_ENTRY) ?: return InteractionResult.FAIL_AND_CANCEL
        if (!handleStartUsing(context.player, dungeonEntry)) return InteractionResult.FAIL_AND_CANCEL
        return InteractionResult.PASS
    }

    override fun handleStopUse(context: StopUseContext): BehaviorResult {
        val dungeonEntry = context.itemstack.getProp(ItemPropTypes.DUNGEON_ENTRY) ?: return BehaviorResult.FINISH_AND_CANCEL
        handleStopUsing(context.player, dungeonEntry)
        return BehaviorResult.PASS
    }

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        // player 将作为队长进入地牢
        val player = context.player
        val itemstack = context.itemstack
        val dungeonEntry = itemstack.getProp(ItemPropTypes.DUNGEON_ENTRY) ?: return cleanupExistingMembersAndReturn(player, BehaviorResult.FINISH_AND_CANCEL)
        val dungeonId = dungeonEntry.dungeon
        val structureId = dungeonEntry.structure

        // 判断先决条件: 地牢是否存在, 结构是否正确
        if (DungeonBridge.inited().not()) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NOT_IN_DUNGEON_WORLD)
            return BehaviorResult.FINISH_AND_CANCEL
        }
        if (!DungeonBridge.hasDungeon(dungeonId).getOrElse { ex ->
                LOGGER.error("Failed to check existence of dungeon '$dungeonId'", ex)
                return BehaviorResult.FINISH_AND_CANCEL
            }
        ) {
            player.sendMessage(TranslatableMessages.MSG_ERR_DUNGEON_NOT_FOUND.arguments(Component.text(dungeonId)))
            return BehaviorResult.FINISH_AND_CANCEL
        }
        if (DungeonBridge.isAwaitingDungeon(player).getOrElse { ex ->
                LOGGER.error("Failed to check if player ${player.name} is awaiting dungeon", ex)
                return BehaviorResult.FINISH_AND_CANCEL
            }
        ) {
            player.sendMessage(TranslatableMessages.MSG_ERR_ALREADY_AWAITING_DUNGEON)
            return BehaviorResult.FINISH_AND_CANCEL
        }
        if (DungeonBridge.isInsideDungeon(player).getOrElse { ex ->
                LOGGER.error("Failed to check if player ${player.name} is inside dungeon", ex)
                return BehaviorResult.FINISH_AND_CANCEL
            }
        ) {
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

        // 无论一人还是多人, 都以组队的方式进入地牢 (1人就1人组队)
        // 因为按照 MythicDungeons 2.0.1-SNAPSHOT/291 的实现,
        // 只有创建了队伍, 才能够完整触发地牢的条件判断, 例如检查人数
        val members = player.metadata().get(PARTY_MEMBERS_KEY).getOrElse(::emptyList)
        val players = buildList {
            // 队长
            add(player)
            // 队员
            addAll(members)
        }
        val success = DungeonBridge.play(players, dungeonId).getOrElse { ex ->
            LOGGER.error("Failed to send player(s) ${players.joinToString(", ") { it.name }} to dungeon '$dungeonId'", ex)
            return cleanupExistingMembersAndReturn(player, BehaviorResult.FINISH_AND_CANCEL)
        }
        if (!success) {
            return cleanupExistingMembersAndReturn(player, BehaviorResult.FINISH_AND_CANCEL)
        }

        // 刷新冷却, 避免开启地牢后短时间内再次尝试开启
        testUseCooldown(player)

        return BehaviorResult.FINISH
    }

    /**
     * 获取玩家周围的队伍成员.
     */
    private fun getPartyMembers(player: Player, dungeonEntry: DungeonEntry): Collection<Player> {
        return player.location.getNearbyPlayers(dungeonEntry.partyRadius) { p ->
            p != player
                    && (!dungeonEntry.requireSneaking || p.isSneaking)
                    && !DungeonBridge.isAwaitingDungeon(p).getOrThrow()
                    && !DungeonBridge.isInsideDungeon(p).getOrThrow()
        }.toCollection(WeakCollection())
    }

    private fun sendMemberPrompts(leader: Player, members: Collection<Player>) {
        val players = listOf(leader) + members
        // 给队伍成员广播假的 EntityEffect
        for (p1 in players) {
            for (p2 in players) {
                if (p1 == p2) continue
                p1.sendEntityEffect(EntityEffect.PROTECTED_FROM_DEATH, p2)
            }
        }
    }

    private fun handleStartUsing(player: Player, dungeonEntry: DungeonEntry): Boolean {
        if (testUseCooldown(player).not()) {
            return false
        }
        if (DungeonBridge.isAwaitingDungeon(player).getOrElse { ex ->
                LOGGER.error("Failed to check if player ${player.name} is awaiting dungeon", ex)
                return false
            }
        ) {
            player.sendMessage(TranslatableMessages.MSG_ERR_ALREADY_AWAITING_DUNGEON)
            return false
        }
        if (DungeonBridge.isInsideDungeon(player).getOrElse { ex ->
                LOGGER.error("Failed to check if player ${player.name} is inside dungeon", ex)
                return false
            }
        ) {
            return false
        }
        val partyMembers = getPartyMembers(player, dungeonEntry)
        // 记录使用者的队友
        player.metadata().put(PARTY_MEMBERS_KEY, ExpiringValue.of(partyMembers, PARTY_MEMBER_LIFETIME_SECONDS, TimeUnit.SECONDS))
        // 将使用者的队友高亮
        sendMemberPrompts(player, partyMembers)
        // 给使用者添加药水效果, 增加使用物品时的代入感
        dungeonEntry.useEffects.forEach(player::addPotionEffect)
        // 播放例子效果 (粒子效果暂不支持配置文件, 懒得写序列化)
        ParticleBuilder(Particle.PORTAL)
            .location(player.location)
            .offset(-2.0, 0.5, 2.0)
            .count(256)
            .receivers(32, false)
            .spawn()
        // 发送消息提示
        player.sendActionBar(TranslatableMessages.MSG_CHANNELING_STARTED)
        player.sendMessage(TranslatableMessages.MSG_PARTY_MEMBER_LIST.arguments(Component.join(JoinConfiguration.commas(true), (listOf(player) + partyMembers).map(Player::name))))
        return true
    }

    private fun testUseCooldown(player: Player): Boolean {
        return player.metadata().getOrPutExpiring(ENTRY_COOLDOWN_KEY) {
            ExpiringValue.of(Cooldown.of(ENTRY_COOLDOWN_SECONDS, TimeUnit.SECONDS), ENTRY_COOLDOWN_SECONDS + 1L, TimeUnit.SECONDS)
        }.test()
    }

    private fun handleStopUsing(player: Player, dungeonEntry: DungeonEntry) {
        cleanupExistingMembers(player)
        // 播放声音效果
        player.playSound(Sound.sound().type(BukkitSound.ENTITY_SHULKER_BULLET_HURT).source(Sound.Source.PLAYER).build(), player)
        // 发送消息提示
        player.sendActionBar(TranslatableMessages.MSG_CHANNELING_STOPPED)
    }

    private fun cleanupExistingMembers(player: Player) {
        DungeonBridge.unqueue(player)
        DungeonBridge.leaveParty(player)
        val partyMembers = player.metadata().remove(PARTY_MEMBERS_KEY) ?: emptyList()
        partyMembers.forEach(DungeonBridge::unqueue)
        partyMembers.forEach(DungeonBridge::leaveParty)
    }

    private fun <T> cleanupExistingMembersAndReturn(player: Player, ret: T): T {
        cleanupExistingMembers(player)
        return ret
    }
}