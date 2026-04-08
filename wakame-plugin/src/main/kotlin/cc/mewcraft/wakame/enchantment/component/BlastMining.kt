package cc.mewcraft.wakame.enchantment.component

import org.bukkit.block.Block

class BlastMining(
    val explosionPower: Float,
    val minBlockHardness: Float,
) {
    fun isHardEnough(block: Block): Boolean {
        val hardness = block.type.getHardness()
        return hardness >= minBlockHardness
    }
}