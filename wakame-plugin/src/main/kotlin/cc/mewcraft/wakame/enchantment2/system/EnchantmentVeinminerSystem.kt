package cc.mewcraft.wakame.enchantment2.system

import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.ecs.component.EntityPlayer
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.enchantment2.component.Veinminer
import cc.mewcraft.wakame.enchantment2.component.VeinminerChild
import cc.mewcraft.wakame.util.adventure.sound
import cc.mewcraft.wakame.util.serverTick
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import net.kyori.adventure.sound.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent

/**
 * @see cc.mewcraft.wakame.enchantment2.effect.EnchantmentVeinminerEffect
 */
object EnchantmentVeinminerSystem : ListenableIteratingSystem(
    family = World.family { all(EntityPlayer, BukkitPlayerComponent, Veinminer, VeinminerChild) }
) {

    private val validAdjacentFaces: Array<BlockFace> = arrayOf(
        BlockFace.WEST,
        BlockFace.EAST,
        BlockFace.NORTH,
        BlockFace.SOUTH,
        BlockFace.UP,
        BlockFace.DOWN,
    )

    private val runningBlock: ThreadLocal<Block> = ThreadLocal()

    override fun onTickEntity(entity: Entity) {
        if (serverTick % 2 == 0) return // 慢一点儿, 避免音效过于密集

        val entityPlayer = entity[EntityPlayer].entityOrNull
        if (entityPlayer == null) {
            entity.remove(); return
        }
        if (!entityPlayer.has(Veinminer)) {
            entity.remove(); return
        }

        // BFS

        val veinminerChild = entity[VeinminerChild]
        val queue = veinminerChild.queue
        if (queue.isEmpty() || veinminerChild.currentCount++ > veinminerChild.maximumCount) {
            entity.remove(); return
        }

        val bukkitPlayer = entity[BukkitPlayerComponent]()
        val bukkitBlock = queue.removeFirst()

        this.runningBlock.set(bukkitBlock)
        bukkitPlayer.breakBlock(bukkitBlock)
        this.runningBlock.remove()

        if (!queue.isEmpty()) { // 不播放第一个方块的音效 (第一个方块是玩家自己破坏掉的方块, 音效已经播放)
            bukkitPlayer.playSound(sound {
                type(entity[Veinminer].blockBreakSound)
                source(Sound.Source.BLOCK)
            }, bukkitBlock.x + 0.5, bukkitBlock.y + 0.5, bukkitBlock.z + 0.5)
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
        if (block == this.runningBlock.get()) return

        val player = event.player
        val playerEntity = player.koishify().unwrap()
        val veinminer = playerEntity.getOrNull(Veinminer) ?: return

        val allowedBlockTypes = veinminer.allowedBlockTypes
        if (block.type !in allowedBlockTypes) return

        Fleks.createEntity {
            it += EntityPlayer(playerEntity)
            it += BukkitPlayerComponent(player)
            it += veinminer
            it += VeinminerChild(veinminer.longestMiningChain, block)
        }
    }

}