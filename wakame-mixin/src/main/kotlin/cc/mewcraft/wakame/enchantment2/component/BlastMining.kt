package cc.mewcraft.wakame.enchantment2.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component
import org.bukkit.block.Block

class BlastMining(
    val explosionPower: Float,
    val minBlockHardness: Float,
) : Component<BlastMining> {

    companion object : EComponentType<BlastMining>()

    override fun type() = BlastMining

    fun isHardEnough(block: Block): Boolean {
        val hardness = block.type.getHardness()
        return hardness >= minBlockHardness
    }

}