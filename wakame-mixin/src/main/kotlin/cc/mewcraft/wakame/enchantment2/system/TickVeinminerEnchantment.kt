package cc.mewcraft.wakame.enchantment2.system

import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.ecs.component.EntityPlayer
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.enchantment2.component.Veinminer
import cc.mewcraft.wakame.enchantment2.component.VeinminerChild
import cc.mewcraft.wakame.util.adventure.playSound
import cc.mewcraft.wakame.util.serverTick
import com.github.quillraven.fleks.Entity
import net.kyori.adventure.sound.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent

/**
 * @see cc.mewcraft.wakame.enchantment2.effect.EnchantmentVeinminerEffect
 */
object TickVeinminerEnchantment : ListenableIteratingSystem(
    family = EWorld.family { all(EntityPlayer, BukkitPlayer, Veinminer, VeinminerChild) }
) {

    private val validAdjacentFaces: Array<BlockFace> = arrayOf(
        BlockFace.WEST,
        BlockFace.EAST,
        BlockFace.NORTH,
        BlockFace.SOUTH,
        BlockFace.UP,
        BlockFace.DOWN,
    )

    @JvmStatic
    val runningBlock: ThreadLocal<Block> = ThreadLocal()

    override fun onTickEntity(entity: Entity) {
        if (serverTick % 2 == 0) return // 慢一点儿, 避免音效过于密集

        val entityPlayer = entity[EntityPlayer]()
        if (entityPlayer == null || entityPlayer.has(Veinminer) == false) {
            entity.remove(); return // 玩家离线或失去该魔咒, 停止连锁采矿效果
        }

        // BFS

        val veinminerChild = entity[VeinminerChild]
        val queue = veinminerChild.queue
        if (queue.isEmpty() || veinminerChild.currentCount++ > veinminerChild.maximumCount) {
            entity.remove(); return // 整个矿脉已遍历完毕或达到遍历次数上限
        }

        val bukkitPlayer = entity[BukkitPlayer].unwrap()
        val bukkitBlock = queue.removeFirst()

        this.runningBlock.set(bukkitBlock)
        bukkitPlayer.breakBlock(bukkitBlock)
        // ... 这里将接着执行 BlockBreakEvent 的逻辑
        this.runningBlock.remove()

        if (!queue.isEmpty()) { // 不播放第一个方块的特效 (第一个方块是玩家自己破坏掉的方块, 特效已经播放)
            val veinminer = entity[Veinminer]
            bukkitPlayer.playSound(bukkitBlock.location) {
                type(veinminer.blockBreakSound)
                source(Sound.Source.BLOCK)
            }
        }

        val visited = veinminerChild.visited
        for (face in validAdjacentFaces) {
            val neighbor = bukkitBlock.getRelative(face)
            if (veinminerChild.sameType(neighbor) && visited.add(neighbor)) {
                queue.addLast(neighbor)
            }
        }
    }

    // 挖掘了特定方块后触发连锁效果
    @EventHandler
    fun on(event: BlockBreakEvent) {
        val block = event.block
        if (block == this.runningBlock.get()) return // 禁止 Veinminer 破坏的方块再次触发 Veinminer
        //if (block == BlastMiningEnchantmentHandler.runningBlock.get()) return // 禁止 BlastMining 破坏的方块触发 Veinminer
        val player = event.player
        if (player.isSneaking) return // 潜行时不触发该魔咒效果
        val playerEntity = player.koishify().unwrap()
        val veinminer = playerEntity.getOrNull(Veinminer) ?: return
        val allowedBlockTypes = veinminer.allowedBlockTypes
        if (block.type !in allowedBlockTypes) return

        Fleks.INSTANCE.createEntity {
            it += EntityPlayer(playerEntity)
            it += BukkitPlayer(player)
            it += veinminer
            it += VeinminerChild(veinminer.longestMiningChain, block)
        }
    }

}